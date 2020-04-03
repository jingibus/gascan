(ns gascan.posts-view
  (:require [gascan.browser :as browser]
            [gascan.post-view :as post-view]
            [gascan.posts :as posts]
            [gascan.template :as template]
            [hiccup.core :as hc])
  (:use gascan.debug))

(def sorting-formatter (java-time/formatter "YYYY/MM/dd"))

(defn day-key
  [timestamp zone]
  (let [as-date (-> timestamp
                    java-time/instant
                    (java-time/local-date zone))]
    (java-time/format sorting-formatter as-date)))

(defn post->link
  [post]
  (vec [:a {:href (post-view/post->title-path post)}
        (:title post)]))

(defn posts-by-date-path
  ([]
   (posts-by-date-path nil))
  ([criteria]
   "/posts"))

(defn posts-by-date-view
  ([]
   (posts-by-date-view (java-time/zone-id) nil))
  ([zone criteria]
   (let [key-fn #(day-key (:timestamp %) zone)
         ;; Create a list of ("YYYY/MM/dd" (posts...)) pairs 
         ;; ordered by day descending.
         posts-by-day (->> (posts/posts)
                           (sort-by #(- (:timestamp %)))
                           (partition-by key-fn)
                           (map (juxt #(key-fn (first %)) identity)))]
     (template/enframe
      "The Gas Can"
      (hc/html
       (map (fn [[a b]] 
              (list 
               [:h3 a] 
               (map 
                #(vec [:p (post->link %)]) b)))
            posts-by-day))))))

(comment
  (def some-post (first (posts/posts)))
  (java-time/instant (:timestamp some-post))
  (-> some-post :timestamp java-time/instant (java-time/local-date (java-time/zone-id)) (.getClass) (.getMethods) vec)
  (day-key (:timestamp some-post) (java-time/zone-id))
  (clojure.pprint/pprint (posts-by-date-view (java-time/zone-id) nil))

  (clojure.pprint/pprint (sort-and-group-by-key :a [{:a 1 :b 10} {:a 1 :b 7} {:a -1 :b 6}]))
  (use 'clojure.test)
  (require '[gascan.browser :as browser])
  (browser/look-at "posts")
  
  )
