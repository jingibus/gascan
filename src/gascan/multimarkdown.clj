(ns gascan.multimarkdown
  (:refer-clojure)
  (:import [com.vladsch.flexmark.formatter Formatter]
           [com.vladsch.flexmark.html HtmlRenderer]
           [com.vladsch.flexmark.parser Parser ParserEmulationProfile]
           [com.vladsch.flexmark.util.data MutableDataSet])
  (:require [clojure.java.io :refer [as-file]]
            [clojure.reflect :refer [reflect]]
            [gascan.flexmark :as fm])
  (:use [gascan.debug])
  (:gen-class))

(defn make-options
  [& options-list]
  (let [options (new MutableDataSet)]
    (loop [[key value & options-list] options-list]
      (.set options key value)
      (if options-list
        (recur options-list)
        options))))

(def multimarkdown-options
  (-> (make-options Parser/HEADING_NO_ATX_SPACE true)
      (.setFrom ParserEmulationProfile/MULTI_MARKDOWN)))

(def parse-multimarkdown-str (partial fm/parse-str multimarkdown-options))

(def parse-multimarkdown-flat (partial fm/parse-readable multimarkdown-options))

(defn render-html
  ([options flexmark-document]
   (-> (HtmlRenderer/builder options)
       (.build)
       (.render flexmark-document)))
  ([flexmark-document]
   (-> (HtmlRenderer/builder)
       (.build)
       (.render flexmark-document))))

(defn md-filepath-from-dir
  "Within a Markdown directory, yields the markdown file within it.
  ```
  user=> (md-filepath-from-dir \"src/sample/Basic Test.md\")
  \"src/sample/Basic Test.md/Basic Test.md\"
  ```"
  [dirpath]
  (as-> dirpath x
    (clojure.string/split x #"/")
    (filter #(not (empty? (clojure.string/trim %))) x)
    (last x)
    (clojure.string/join "/" [dirpath x])))

(defn parse-multimarkdown-directory
  [filepath]
  (parse-multimarkdown-flat (md-filepath-from-dir filepath)))

(defn parse-multimarkdown
  "Yields a parsed flexmark Document instance."
  [filepath]
  (let [file-obj (as-file filepath)]
    (cond (.isDirectory file-obj)
          (parse-multimarkdown-directory filepath)
          (.isFile file-obj)
          (parse-multimarkdown-flat filepath)
          :else
          nil)))

(defn flatten-iterator
  [iterator]
  (let [iterator (if (instance? java.lang.Iterable iterator)
                   (.iterator iterator)
                   iterator)]
    (loop [values []]
      (if (.hasNext iterator)
        (recur (conj values (.next iterator)))
        values))))

(def render-markdown (partial fm/render-markdown multimarkdown-options))
