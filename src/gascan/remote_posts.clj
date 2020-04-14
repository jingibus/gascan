(ns gascan.remote-posts
  (:require [clojure.java.io :refer [as-file]]
            [clojure.spec.alpha :as s]
            [clojure.zip :as z]
            [gascan.ast :as ast]
            [gascan.multimarkdown :as mm :refer [parse-multimarkdown-flat md-filepath-from-dir]]
            [gascan.post-spec :as post-spec]))

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
    {:markdown-abs-path (.getAbsolutePath (as-file md-filepath))
     :title (get-title parsed-markdown)
     :timestamp (System/currentTimeMillis)
     :extra-resources extra-resources
     :dir-depth 1
     :parsed-markdown parsed-markdown
     :src-path dirpath}))

(s/fdef record-from-mm-dir
  :ret post-spec/remote-post)

(defn record-from-mm-flat
  [filepath]
  (let [parsed-markdown (parse-multimarkdown-flat filepath)]
    {:markdown-abs-path (.getAbsolutePath (as-file filepath))
     :title (get-title parsed-markdown)
     :timestamp (System/currentTimeMillis)
     :extra-resources []
     :dir-depth 0
     :parsed-markdown parsed-markdown
     :src-path filepath}))

(s/fdef record-from-mm-flat
  :ret post-spec/remote-post)

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

(s/fdef read-remote-post
  :ret post-spec/remote-post)

(defn read-from-samples-md
  [relpath]
  (let [filepath (str "samples/" relpath
                      (when-not (.endsWith relpath ".md") ".md"))]
    (read-remote-post filepath)))

(defn read-from-documents-md
  [relpath]
  (let [filepath (str "/Users/bphillips/Documents/" relpath
                      (when-not (.endsWith relpath ".md") ".md"))]
    (read-remote-post filepath)))
