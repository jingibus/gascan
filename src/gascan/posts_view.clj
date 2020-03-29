(ns gascan.posts-view
  (:require [clojure.string :as string]
            [gascan.posts :as posts])
  (:use [gascan.debug]))

(defn to-kebab-case
  [s]
  (let [pieces (some-> s
                       string/lower-case
                       string/trim
                       (string/split #" +"))]
    (when pieces 
      (string/join "-" pieces))))

(defn find-post
  [search-map]
  (letfn [(route-entitled [post]
            (update-in post [:title] to-kebab-case))
          (normalized-id [post]
            (if (:id post)
              (update-in post [:id] #(when % (string/lower-case %)))
              post))
          (matches [post]
            (let [matching-post (normalized-id (route-entitled post))]
              (= (select-keys matching-post (keys search-map))
                 (normalized-id search-map))))]
    (println "finding:" search-map)
    (first  
     (->> (posts/posts)
          (filter matches)))))

(defn route-post
  [{:keys [id title] :as all}]
  (apply find-post 
         (monitor-> (list (into {} (filter #(second %) all))))))

(comment
  (route-post {:title "blog-project"})
  (find-post {:title "blog-project"})
  (flatten (seq) {1 2})
  (posts/posts)
  (update-in {:title "blog-project"} [:id] identity))
