(ns gascan.ast
  (:require [clojure.zip :as z])
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


