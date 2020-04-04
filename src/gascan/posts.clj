(ns gascan.posts
  (:require
   [clojure.spec.alpha :as s]
   [clojure.zip :as z]
   [gascan.ast :as ast]
   [gascan.intern :refer [intern-edn! read-edn intern-file! readable-file]]
   [gascan.multimarkdown :as mm :refer [parse-multimarkdown-flat
                                        render]]
   [gascan.post-spec :as post-spec]
   [java-time :refer [local-date-time instant]])
  (:use [gascan.debug]))

(def toplevel-post-contents-folder "posts")
(def post-metadata-edn "metadata.edn")

(defrecord InternedPost 
    [
     title
     timestamp
     markdown-rel-path
     extra-resources-rel
     id
     ])

(def edn-readers {'gascan.posts.InternedPost map->InternedPost})

(defn fetch-posts
  []
  {:post [(every? #(s/assert post-spec/intern-post %) %)]}
  (or (read-edn {:readers edn-readers} post-metadata-edn) []))

(def posts-lazy (lazy-seq (list (fetch-posts))))

(defn posts
  []
  (first posts-lazy))

(defn put-posts!
  [posts]
  (intern-edn! post-metadata-edn posts))

(defn to-yyyy-mm-dd-mmmm
  [timestamp]
  (let [date-time (local-date-time (instant timestamp) "America/Los_Angeles")
        minute (.getMinute date-time)
        day-of-month (.getDayOfMonth date-time)
        month (.getMonthValue date-time)
        year (.getYear date-time)]
    (format "%04d/%02d/%02d/%04d" year month day-of-month minute)))

(defn strip-title-section!
  [document]
  (-> document 
      ast/build-scaffold-ast 
      z/vector-zip
      z/down z/right 
      z/remove
      z/root
      ast/restitch-scaffold-ast))

(defn import-post!
  "Imports a RemotePost into an InternedPost. Note that this mutates the parsed Markdown in the RemotePost."
  [remote-post]
  {:pre [(s/valid? post-spec/remote-post remote-post)]
   :post [(s/valid? post-spec/intern-post %)]}
  (let [{title :title 
         timestamp :timestamp
         parsed-markdown :parsed-markdown
         markdown-abs-path :markdown-abs-path
         extra-resources :extra-resources
         dir-depth :dir-depth} remote-post
        timestamp-subfolders (to-yyyy-mm-dd-mmmm timestamp)
        post-contents-folder (clojure.string/join "/" [toplevel-post-contents-folder timestamp-subfolders])
        intern-copied-file! #(intern-file! % post-contents-folder dir-depth)
        interned-resources (map intern-copied-file! extra-resources)
        rendered-markdown (do
                            (strip-title-section! parsed-markdown)
                            (render parsed-markdown))
        interned-markdown-path (intern-file! markdown-abs-path 
                                        post-contents-folder 
                                        dir-depth 
                                        rendered-markdown)]
    (map->InternedPost 
     {
      :title title
      :timestamp timestamp
      :markdown-rel-path interned-markdown-path
      :extra-resources-rel interned-resources
      :id (java.util.UUID/randomUUID)
      })
    ))

(defn as-parsed
  "Yields an interned record with parsed markdown."
  [interned-record]
  (if (:parsed-markdown interned-record)
    interned-record
    (let [relpath (:markdown-rel-path interned-record)
          readable-markdown (readable-file relpath)]
      (assoc interned-record 
             :parsed-markdown (parse-multimarkdown-flat readable-markdown)))))

