(ns gascan.posts
  (:require
   [gascan.intern :refer [intern-edn! read-edn intern-file!]]
   [gascan.multimarkdown :refer [map->InternedPost strip-title-section! render]]
   [java-time :refer [local-date-time instant]])
  (:import [gascan.multimarkdown RemotePost InternedPost]))

(def toplevel-post-contents-folder "posts")
(def post-metadata-edn "metadata.edn")

(def edn-readers {'gascan.multimarkdown.InternedPost map->InternedPost})

(defn fetch-posts
  []
  (or (read-edn {:readers edn-readers} post-metadata-edn) []))

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

(defn import-post!
  "Imports a RemotePost into an InternedPost. Note that this mutates the parsed Markdown in the RemotePost."
  [remote-post]
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
      :parsed-markdown nil
      :markdown-rel-path interned-markdown-path
      :extra-resources-rel interned-resources
      })
    ))
