(ns gascan.posts-view
  (:require [gascan.browser :as browser]
            [gascan.post-view :as post-view]
            [gascan.posts :as posts]
            [gascan.session :as session]
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
  [post criteria]
  (vec [:a {:href (post-view/post->title-path post criteria)}
        (:title post)]))

(defn posts-by-date-path
  ([]
   "/posts")
  ([criteria]
   (let [criteria (if (string? criteria) #{(keyword criteria)} criteria)
         criteria-kebab (clojure.string/join "-" (map name criteria))]
     (str "/posts/criteria/" criteria-kebab))))

(defn posts-by-date-view
  "Yields a view of posts by date, possibly with criteria.

  (posts-by-date-view #{:meta :programming}) is equivalent to
  (posts-by-date-view \"meta-programming\").
"
  ([sess]
   (posts-by-date-view sess #{}))
  ([sess criteria]
   (let [zone (java-time/zone-id "America/Los_Angeles")
         criteria (if (string? criteria)
                    (into #{} (map keyword (clojure.string/split criteria #"-")))
                    criteria)
         key-fn #(day-key (:timestamp %) zone)
         visible? (partial posts/visible-to-session? sess)
         matches-criteria #(or (empty? criteria)
                               (seq (clojure.set/intersection
                                     criteria
                                     (:filter %))))
         ;; Create a list of ("YYYY/MM/dd" (posts...)) pairs 
         ;; ordered by day descending.
         posts-by-day (->> (posts/posts)
                           (filter (every-pred visible? matches-criteria))
                           (sort-by #(- (:timestamp %)))
                           (partition-by key-fn)
                           (map (juxt #(key-fn (first %)) identity)))
         draft-warning #(when-not (#{:published} (:status %)) 
                          [:font {:color "red"} " (DRAFT)"])]
     (when (seq posts-by-day)
       (template/enframe
        "The Gas Can"
        (hc/html
         (map (fn [[date-heading posts]] 
                (list 
                 [:h3 date-heading] 
                 (map 
                  #(vec [:p (post->link % criteria) (draft-warning %)]) posts)))
              posts-by-day)))))))

(comment
  (def some-post (first (posts/posts)))
  (java-time/instant (:timestamp some-post))
  (-> some-post :timestamp java-time/instant (java-time/local-date (java-time/zone-id)) (.getClass) (.getMethods) vec)
  (day-key (:timestamp some-post) (java-time/zone-id))
  (clojure.pprint/pprint (posts-by-date-view session/private-session nil))

  (clojure.pprint/pprint (sort-and-group-by-key :a [{:a 1 :b 10} {:a 1 :b 7} {:a -1 :b 6}]))
  (use 'clojure.test)
  (require '[gascan.browser :as browser])
  (browser/look-at "posts")
  
  )
