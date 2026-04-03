(ns gascan.site
  (:require [clojure.string :as string]
            [gascan.posts :as posts]))

(defn all-posts
  []
  (posts/posts))

(defn visible-posts
  [session]
  (filter #(posts/visible-to-session? session %) (all-posts)))

(defn find-posts
  [locator]
  (posts/find-posts locator))

(defn find-post
  [locator]
  (posts/find-post locator))

(defn criteria->set
  [criteria]
  (cond
    (string? criteria) (into #{} (map keyword (string/split criteria #"\.")))
    (nil? criteria) #{}
    :else criteria))

(defn matches-criteria?
  [criteria post]
  (let [criteria (criteria->set criteria)]
    (or (empty? criteria)
        (seq (clojure.set/intersection criteria (:filter post))))))

(defn visible-posts-by-criteria
  [session criteria]
  (filter #(matches-criteria? criteria %) (visible-posts session)))

(defn newest-visible-post-by-criteria
  [session criteria]
  (->> (visible-posts-by-criteria session criteria)
       (sort-by (comp - :timestamp))
       first))
