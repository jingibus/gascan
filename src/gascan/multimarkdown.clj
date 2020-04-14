(ns gascan.multimarkdown
  (:refer-clojure)
  (:import [com.vladsch.flexmark.formatter Formatter]
           [com.vladsch.flexmark.html HtmlRenderer]
           [com.vladsch.flexmark.parser Parser ParserEmulationProfile]
           [com.vladsch.flexmark.util.data MutableDataSet])
  (:require [clojure.java.io :refer [as-file]]
            [clojure.reflect :refer [reflect]])
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

(def flexmark-options 
  (-> (make-options Parser/HEADING_NO_ATX_SPACE true)
      (.setFrom ParserEmulationProfile/MULTI_MARKDOWN)))

(defn parse-multimarkdown-str
  "Parses a string into a parsed flexmark object"
  ([file-contents]
   (parse-multimarkdown-str flexmark-options file-contents))
  ([options file-contents]
   (-> (Parser/builder options)
         (.build)
         (.parse file-contents))))

(defn parse-multimarkdown-flat
  "Parses a valid input to reader into a parsed flexmark object"
  ([readable]
   (parse-multimarkdown-str flexmark-options (slurp readable)))
  ([options readable]
   (parse-multimarkdown-str options(slurp readable))))

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

(defn render-markdown
  [document]
  (let [renderer (-> (Formatter/builder flexmark-options) (.build))]
    (.render renderer document)))

(defn class-hierarchy
  ([class-instance]
   (class-hierarchy class-instance #{}))
  ([class-instance traversed]
   (if (traversed class-instance)
     #{}
     (let [traversed (conj traversed class-instance)
           subsequent (apply clojure.set/union 
                             (map #(class-hierarchy % traversed)
                                  (bases class-instance)))]
       (conj subsequent class-instance)))))

(defn all-methods
  [instance]
  (let [all-classes (class-hierarchy (class instance))
        total-reflection (apply merge-with 
                                clojure.set/union 
                                (map reflect all-classes))] 
    (->> total-reflection
         :members
         (filter :return-type)
         (map :name)
         sort)))

