(ns gascan.repl
  (:refer-clojure)
  (:import [com.vladsch.flexmark.formatter Formatter]
           [com.vladsch.flexmark.html HtmlRenderer]
           [com.vladsch.flexmark.parser Parser ParserEmulationProfile]
           [com.vladsch.flexmark.util.data MutableDataSet])
  (:require [clojure.java.io :refer [as-file]]
            [clojure.reflect :refer [reflect]]

            [gascan.ast :refer [deep-map-vec
                                build-scaffold-ast
                                stringify]]
            [gascan.multimarkdown :refer [parse-multimarkdown-flat 
                                          flexmark-options 
                                          make-options
                                          render-multimarkdown]]
            [clojure.tools.trace :refer [trace-ns untrace-ns trace-forms]])
  (:use [gascan.debug] [gascan.test-tools])
  (:gen-class))


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


