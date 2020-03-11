(ns gascan.multimarkdown
  (:refer-clojure)
  (:import [com.vladsch.flexmark.parser Parser ParserEmulationProfile]
           [com.vladsch.flexmark.util.data MutableDataSet]
           [com.vladsch.flexmark.formatter Formatter])
  (:require [clojure.reflect :refer [reflect]]
            [clojure.java.io :refer [as-file]])
  (:gen-class))

(def ^:dynamic *verbose* false)

(defmacro printlnv
  [& args]
  `(when *verbose*
     (printf ~@args)))

(defmacro with-verbose
  [& body]
  `(binding [*verbose* true] ~@body))


(defrecord RemotePost 
    [
     title
     timestamp
     parsed-markdown
     markdown-abs-path 
     extra-resources
     dir-depth
     ])

(defrecord InternedPost 
    [
     title
     timestamp
     parsed-markdown
     markdown-rel-path
     extra-resources-rel
     ])

(def flexmark-options 
  (-> (new MutableDataSet)
      (.setFrom ParserEmulationProfile/MULTI_MARKDOWN)))

(defn parse-multimarkdown-flat
  [filepath]
  (let [file-contents (slurp filepath)
        options flexmark-options]
    (-> (Parser/builder options)
        (.build)
        (.parse file-contents))))

(defn md-filepath-from-dir
  [dirpath]
  (as-> dirpath x
    (clojure.string/split x #"/")
    (filter #(> (count (clojure.string/trim %)) 0) x)
    (last x)
    (clojure.string/join "/" [dirpath x])))

(defn parse-multimarkdown-directory
  [filepath]
  (parse-multimarkdown-flat (md-filepath-from-dir filepath)))

(defn parse-multimarkdown
  [filepath]
  (let [file-obj (as-file filepath)]
    (cond (.isDirectory file-obj)
          (parse-multimarkdown-directory filepath)
          (.isFile file-obj)
          (parse-multimarkdown-flat filepath)
          :else
          nil)))

(defn get-title
  [document]
  (let [child-iterator (-> document (.getChildren) (.iterator))
        title-page (if (.hasNext child-iterator) (.next child-iterator))]
    (if title-page
      (let 
          ;; Should extract something like "Title: Blog Project  \n"
          [title-line (-> title-page
                          (.getContentLines)
                          first
                          (.toString))
           title-text (-> title-line
                          (clojure.string/replace-first "Title:" "")
                          clojure.string/trim)]
        title-text))))

(defn record-from-mm-dir
  [dirpath]
  (let [file-obj (as-file dirpath)
        md-filepath (md-filepath-from-dir dirpath)
        md-file-obj (as-file md-filepath)
        parsed-markdown (parse-multimarkdown-flat md-filepath)
        extra-resources (->> (as-file dirpath)
                             (.listFiles)
                             (filter #(not (.equals md-file-obj %))))]
    (map->RemotePost {:markdown-abs-path (.getAbsolutePath (as-file md-filepath))
                      :title (get-title parsed-markdown)
                      :timestamp (System/currentTimeMillis)
                      :extra-resources extra-resources
                      :dir-depth 1
                      :parsed-markdown parsed-markdown})))

(defn record-from-mm-flat
  [filepath]
  (let [parsed-markdown (parse-multimarkdown-flat filepath)]
    (map->RemotePost {:markdown-abs-path (.getAbsolutePath (as-file filepath))
                      :title (get-title parsed-markdown)
                      :timestamp (System/currentTimeMillis)
                      :extra-resources []
                      :dir-depth 0
                      :parsed-markdown parsed-markdown})))

(defn read-remote-post
  [filepath]
  (let [file-obj (as-file filepath)]
    (cond (.isDirectory file-obj)
          (record-from-mm-dir filepath)
          (.isFile file-obj)
          (record-from-mm-flat filepath))))

(defn flatten-iterator
  [iterator]
  (let [iterator (if (instance? java.lang.Iterable iterator)
                   (.iterator iterator)
                   iterator)]
    (loop [values []]
      (if (.hasNext iterator)
        (recur (conj values (.next iterator)))
        values))))

(defn strip-title-section!
  [document]
  (let [iterator (-> document (.getChildren) (.iterator))]
    (.next iterator)
    (.remove iterator)))

(defn render
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


