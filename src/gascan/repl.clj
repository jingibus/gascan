(ns gascan.repl
  (:refer-clojure)
  (:import [com.vladsch.flexmark.formatter Formatter]
           [com.vladsch.flexmark.html HtmlRenderer]
           [com.vladsch.flexmark.parser Parser ParserEmulationProfile]
           [com.vladsch.flexmark.util.data MutableDataSet])
  (:require [clojure.java.io :refer [as-file]]
            [clojure.reflect :refer [reflect]]
            [clojure.string :refer [join]]
            [gascan.ast :refer [deep-map-vec
                                build-scaffold-ast
                                stringify]]
            [gascan.multimarkdown :refer [parse-multimarkdown-flat 
                                          flexmark-options 
                                          make-options
                                          render-multimarkdown]]
            [clojure.tools.trace :refer [trace-ns untrace-ns trace-forms]])
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


