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

(defn all-by-date-path
  []
  "/posts")

(defn all-by-date-view
  []
  (index-view-by-date (java-time/zone-id) nil))

(defn sort-and-group-by-key
  "(sort-and-group-by-key
     :a
     [{:a 1 :b 10} {:a 1 :b7} {:a -1 :b 6}])
   => {-1 ({:a -1 :b 6})), 1 ({:a 1 :b 10} {:a 1 :b 7})}"
  [kfn xs]
  (let [annotated-xs (map (fn [x] (list (kfn x) x)) xs)]
    (->> annotated-xs
         (sort-by first)
         (reduce (fn [coll [k v]]
                   (update coll k #(conj (or % []) v)))
                 {}))))

(defn post->link
  [post]
  (vec [:a {:href (post-view/post->title-path post)}
        (:title post)]))

(defn index-view-by-date
  [zone criteria]
  (let [posts-by-day (->> (posts/posts)
                          (sort-by :timestamp)
                          (sort-and-group-by-key #(day-key (:timestamp %) zone))
                          (sort-by first #(- (compare %1 %2)))
                          (monitor->> "Template input"))]
    (template/enframe
     "The Gas Can"
     (hc/html
      (map (fn [[a b]] 
             (list 
              [:h3 a] 
              (map 
               #(vec [:p (post->link %)]) b)))
           posts-by-day)))))

(comment
  (def some-post (first (posts/posts)))
  (java-time/instant (:timestamp some-post))
  (-> some-post :timestamp java-time/instant (java-time/local-date (java-time/zone-id)) (.getClass) (.getMethods) vec)
  (day-key (:timestamp some-post) (java-time/zone-id))
  (clojure.pprint/pprint (index-view-by-date (java-time/zone-id) nil))

  (clojure.pprint/pprint (sort-and-group-by-key :a [{:a 1 :b 10} {:a 1 :b 7} {:a -1 :b 6}]))
  (testing "sort and group preserves order in sublists"
      (is (= (sort-and-group-by-key :a [{:a 1 :b 10} {:a 1 :b 7} {:a -1 :b 6}])
             {-1 [{:a -1, :b 6}], 1 [{:a 1, :b 10} {:a 1, :b 7}]}))
    )
  (use 'clojure.test)
  (require '[gascan.browser :as browser])
  (browser/look-at "posts")
  
  )
