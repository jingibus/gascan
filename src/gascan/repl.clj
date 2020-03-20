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
            [clojure.tools.trace :refer [trace-ns untrace-ns]])
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

(defn getNodeChildren
  [nodeable]
  (when (instance? com.vladsch.flexmark.util.ast.Node nodeable)
    (loop [iterator (-> nodeable (.getChildren) (.iterator))
           children []]
      (if (.hasNext iterator)
        (recur iterator (conj children (.next iterator))
               )
        children))))

(defn decrapinate-flexmark
  [nodeable]
  (let [children (getNodeChildren nodeable)
        simple-name (some-> nodeable class (.getSimpleName))]
    (if (or (nil? children) (empty? children))
      (str nodeable)
      (vec (cons simple-name (map decrapinate-flexmark children))))))


(defn test-parse-with-options
  [options test-case]
  (let [path (:path test-case)
        new-contents (parse-multimarkdown-flat options path)]
    (decrapinate-flexmark new-contents)))
