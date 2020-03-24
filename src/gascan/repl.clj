(ns gascan.repl
  (:refer-clojure)
  (:import [com.vladsch.flexmark.formatter Formatter]
           [com.vladsch.flexmark.html HtmlRenderer]
           [com.vladsch.flexmark.parser Parser ParserEmulationProfile]
           [com.vladsch.flexmark.util.data MutableDataSet])
  (:require [clojure.java.io :refer [as-file]]
            [clojure.reflect :refer [reflect]]
            [clojure.string :refer [join]]
            [gascan.multimarkdown :refer [parse-multimarkdown-flat flexmark-options make-options]]
            [clojure.tools.trace :refer [trace-ns untrace-ns trace-forms]]
            [clojure.zip :as z])
  (:use [gascan.debug])
  (:gen-class))

(def project-folder (System/getProperty "user.dir"))

(def samples-folder (join "/" [project-folder "samples"]))

(defn sample-path
  [relpath]
  (join "/" [samples-folder relpath]))

(defn test-case
  [relpath]
  (let [absolute-path (sample-path relpath)
        parsed-contents (parse-multimarkdown-flat absolute-path)]
    {:path absolute-path
     :contents parsed-contents
     :raw-contents (clojure.string/split (slurp absolute-path) #"\n")}))

(def test-case-basic (test-case "Basic Test.md"))
(def test-case-image (test-case "Image Test.md/Image Test.md"))
(def test-case-no-header-space (test-case "Basic Test No Header Space.md"))
(def test-case-image-inline (test-case "Image Test Inline.md/Image Test Inline.md"))

(defn getNodeChildren
  [nodeable]
  (when (instance? com.vladsch.flexmark.util.ast.Node nodeable)
    (loop [iterator (-> nodeable (.getChildren) (.iterator))
           children []]
      (if (.hasNext iterator)
        (recur iterator (conj children (.next iterator))
               )
        children))))

(defn deep-map-vec
  ([f all-items]
   (cond (not (coll? all-items))
         (f all-items)
         (empty? all-items)
         all-items
         :else
         (let [[item & items] all-items
               mapped-item (if (vector? item)
                             (deep-map-vec f item)
                             (f item))]
           (vec (cons mapped-item (deep-map-vec f items)))))))

(defn build-scaffold-ast
  [nodeable]
  (letfn [(unlink-ast! [scaffold-ast] 
            (deep-map-vec 
             #(when % 
                (printlnv "unlinking" %) 
                (.unlink %) 
                %) 
             scaffold-ast))
          (build [nodeable]
            )]
    (let [scaffold-ast 
          (let [children (getNodeChildren nodeable)]
              (if (or (nil? children) (empty? children))
                nodeable
                (vec (cons nodeable (map build-scaffold-ast children)))))]
      scaffold-ast)))

(defn stringify
  [scaffold-ast]
  (deep-map-vec str scaffold-ast))

(defn decrapinate-flexmark
  [nodeable]
  (letfn [(simple-name [x] (-> x (class) (.getSimpleName)))
          (stringify 
            [structured-node]
            (if (vector? structured-node)
              (let [[nodeable & children] structured-node]
                (vec (cons (simple-name nodeable) 
                           (map stringify children))))
              (str structured-node)))]
    (-> nodeable
        build-scaffold-ast
        stringify)))

(defn test-parse-with-options
  [options test-case]
  (let [path (:path test-case)
        new-contents (parse-multimarkdown-flat options path)]
    (decrapinate-flexmark new-contents)))

(defn test-parse-with-postprocessor
  [postprocessor test-case]
  (let [path (:path test-case)
        new-contents (-> path (monitorv-> "path") parse-multimarkdown-flat (monitorv-> "parsed") postprocessor)]
    (assoc test-case 
           :path path 
           :contents new-contents)))

(defn split-line-breaks
  "Modifies scaffold so that all line breaks split into paragraphs"
  [scaffold-node]
  (letfn [(is-line-break?
            [node] 
            (or (instance? com.vladsch.flexmark.ast.SoftLineBreak node) 
                (instance? com.vladsch.flexmark.ast.HardLineBreak node)))
          (remove-while
            ;; Removes nodes while a condition on the node is true.
            [loc while?]
            (loop [loc loc]
                (if (while? (z/node loc))
                  (recur (z/remove loc))
                  loc)))
          (find-loc
            [loc stop?]
            (->> loc
                 (iterate z/up)
                 (filter stop?)
                 first))
          (show-node-and-right-siblings
            ;; For debugging.
            [node]
            (str (z/node node) ": " (-> node z/rights stringify)))
          (show-node-and-siblings
            [node]
            (str (-> node z/lefts stringify) " :"
             ;(->> node z/lefts (map str) list) " :" 
                 (stringify (z/node node)) 
                 ": " (-> node z/rights stringify)))]
    (loop [loc (z/vector-zip scaffold-node)]
      (cond (z/end? loc)
            ;; If we've finished walking the tree, yield the edited tree
            (z/root loc)
            (is-line-break? (z/node loc))
            ;; If this node is a line break, then create a new paragraph node
            ;; with all its rightmost siblings, remove those rightmost siblings, 
            ;; pop up to the parent of the enclosing Paragraph, and add the new node
            ;; after that.
            (let [new-node (vec (cons (new com.vladsch.flexmark.ast.Paragraph)
                                      (z/rights loc)))
                  left-sibling (-> loc z/left z/node)
                  edited (-> loc
                             (monitorv-> "Editing:" show-node-and-siblings)
                             z/rightmost
                             (remove-while #(not (= % (z/node loc))))
                             (monitorv-> "Removed right children of" show-node-and-right-siblings)
                             z/remove
                             (find-loc #(= left-sibling (z/node %)))
                             z/up
                             (monitorv-> "And now we insert a node to the right here:" show-node-and-siblings)
                             (z/insert-right new-node))]
              (recur (z/next edited)))
            :else
            (recur (z/next loc))))))

(defn restitch-scaffold-ast
  [scaffold-ast]
  (letfn [(leaf? [node] (not (vector? node)))
          (node-value [node]
            (if (leaf? node)
              node
              (first node)))
          (node-and-children-values [node]
            (vec (if (leaf? node)
                   [node]
                   (map node-value node))))
          (restitch [node]
            (when (not (leaf? node))
              (let [[value & children-values] 
                    (node-and-children-values node)
                    flexmark-children (getNodeChildren value)]
                (do
                  (run! #(.unlink %) flexmark-children)
                  (run! restitch (rest node))
                  (printlnv "Restitching node" [value] 
                            "\n\tchildren:" (map str children-values)
                            "\n\tflexmark children: " (map str (getNodeChildren value)))
                  (run! #(do (printlnv "\tAppend child" (str %) "to" (str value)) 
                            (.appendChild value %)) 
                       children-values))
                )))]
    (restitch scaffold-ast)
    (node-value scaffold-ast)))
