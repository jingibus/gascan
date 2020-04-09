(ns gascan.ast
  (:require [clojure.zip :as z])
  (:import [com.vladsch.flexmark.util.sequence BasedSequence CharSubSequence]
           [com.vladsch.flexmark.ast Link])
  (:use [gascan.debug]))

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

(defn stringify
  [scaffold-ast]
  (deep-map-vec str scaffold-ast))

(defn scaffold->tagged-scaffold
  [scaffold-ast]
  [{} scaffold-ast])

(defn tagged-scaffold->scaffold
  [[tags scaffold-ast]]
  scaffold-ast)

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

(defn char-sequence
  [s]
  (CharSubSequence/of s))

(defn construct [klass & args]
    (clojure.lang.Reflector/invokeConstructor klass (into-array Object args)))

(defn link
  [text url]
  (let [all-chars (char-sequence (str "[" text "]" "(" url ")"))
        start-text-open 0
        start-text 1
        start-text-close (+ start-text (count text))
        start-link-open (+ start-text-close 1)
        start-link (+ start-link-open 1)
        start-link-close (+ start-link (count url))
        end-link-close (+ start-link-close 1)
        marks [start-text-open start-text start-text-close 
               start-link-open start-link start-link-close end-link-close]
        args (map #(.subSequence all-chars (key %) (val %)) 
             (zipmap (butlast marks) (rest marks)))
        text-subchars (.subSequence all-chars 1 (+ 1 (count text)))
        url-subchars (.subSequence all-chars (count text) (count (str text url)))
        new-link
;    public Link(BasedSequence textOpenMarker, BasedSequence text, BasedSequence textCloseMarker, BasedSequence linkOpenMarker, BasedSequence url, BasedSequence linkCloseMarker) {

        (doto 
            (apply construct (cons Link args))
             ; linkCloseMarker
          .setCharsFromContent
          (.setPageRef (.subSequence all-chars start-link start-link-close)))
        text (-> new-link .getText .toString)]
    (pprint-symbols new-link text args)
    new-link))
        (comment (new Link 
                      (.subSequence all-chars start-text-open) ; textOpenMarker
                      text-subchars                            ; text
                      BasedSequence/NULL  ; textCloseMarker
                      BasedSequence/NULL  ; linkOpenMarker
                      url-subchars        ; url
                      BasedSequence/NULL))
