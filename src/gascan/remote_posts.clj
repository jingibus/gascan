(ns gascan.remote-posts
  (:require [clojure.java.io :refer [as-file]]
            [clojure.zip :as z]
            [gascan.ast :as ast]
            [gascan.multimarkdown :as mm :refer [parse-multimarkdown-flat md-filepath-from-dir]]))

(defrecord RemotePost 
    [
     title
     timestamp
     parsed-markdown
     markdown-abs-path 
     extra-resources
     dir-depth
     ])

(defn get-title
  [document]
  (let [scaffold-ast (ast/build-scaffold-ast document)
        ;; [Document [Paragraph ...] ...]
        title-text (-> scaffold-ast z/vector-zip z/down z/right z/down z/right z/node)]
    (some-> title-text
            (.getChars)
            (clojure.string/replace-first "Title:" "")
            clojure.string/trim)))

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
          (record-from-mm-flat filepath)
          :else
          (throw (new java.io.IOException (str "Unknown file type: " 
                                               filepath "::" file-obj))))))
