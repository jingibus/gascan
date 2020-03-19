(ns gascan.repl
  (:refer-clojure)
  (:import [com.vladsch.flexmark.formatter Formatter]
           [com.vladsch.flexmark.html HtmlRenderer]
           [com.vladsch.flexmark.parser Parser ParserEmulationProfile]
           [com.vladsch.flexmark.util.data MutableDataSet])
  (:require [clojure.java.io :refer [as-file]]
            [clojure.reflect :refer [reflect]]
            [clojure.string :refer [join]]
            [gascan.multimarkdown :refer [parse-multimarkdown-flat]])
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
     :contents parsed-contents}))

(def test-case-basic (test-case "Basic Test.md"))
(def test-case-image (test-case "Image Test.md/Image Test.md"))
