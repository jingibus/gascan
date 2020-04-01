(ns gascan.nav-view
  (:require [hiccup.core :as hc]
            [gascan.posts :as posts])
  (:use gascan.debug))

(def sorting-formatter (java-time/formatter "YYYY/MM/dd"))

(defn day-key
  [post zone]
  (let [as-date (-> post
                    :timestamp
                    java-time/instant
                    (java-time/local-date zone))]
    (java-time/format sorting-formatter as-date)))

(defn index-view-by-date
  [zone criteria]
  (let [sorted-posts (sort 
                      (fn [a b] (apply compare (map :timestamp [b a])))
                      (posts/posts))
        posts-by-day (->> sorted-posts
                          (map #(vec [(day-key % zone) %]))
                          (reduce (fn [coll [k v]] 
                                    (update coll k #(cons v %)))
                                  {}))]
    posts-by-day)
  )

(comment
  (def some-post (first (posts/posts)))
  (java-time/instant (:timestamp some-post))
  (-> some-post :timestamp java-time/instant (java-time/local-date (java-time/zone-id)) (.getClass) (.getMethods) vec)
  (day-key some-post (java-time/zone-id))
  (clojure.pprint/pprint (index-view-by-date (java-time/zone-id) nil))
  )
