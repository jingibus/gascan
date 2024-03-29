(ns gascan.posts-view
  (:require [gascan.browser :as browser]
            [gascan.posts :as posts]
            [gascan.session :as session]
            [gascan.template :as template]
            [gascan.view-common :as view-common]
            [hiccup.core :as hc]
            [gascan.routing :as routing])
  (:import [java.time.format DateTimeFormatter])
  (:use gascan.debug))

(def sorting-formatter (java-time/formatter "YYYY/MM/dd"))

(defn rfc-1123-format
  [timestamp]
  (let [as-date-time (-> timestamp
                         java-time/instant
                         (java-time/zoned-date-time (java-time/zone-id "GMT")))]
    (java-time/format DateTimeFormatter/RFC_1123_DATE_TIME as-date-time)))

(defn day-key
  [timestamp zone]
  (let [as-date (-> timestamp
                    java-time/instant
                    (java-time/local-date zone))]
    (java-time/format sorting-formatter as-date)))

(defn post->link
  [post criteria from-post-id]
  (let [link (vec [:a {:href (routing/post->title-path post criteria)}
                   (:title post)])]
    (if (= (:id post) from-post-id)
      (list "#==" link "==#")
      link)))

(defn posts-view-rss-internal
  "Yields an RSS view of posts by date, possibly with criteria.

  (posts-by-date-rss-view ... #{:meta :programming}) is equivalent to
  (posts-by-date-rss-view ... \"meta-programming\").
"
  ([sess query-params]
   (let [{from-post-id :from-post-id} (routing/posts-query-params->map query-params)
         zone (java-time/zone-id "America/Los_Angeles")
         key-fn #(day-key (:timestamp %) zone)
         visible? (partial posts/visible-to-session? sess)
         route-to-self #(str "https://www.billjings.com" %)
         self-link (route-to-self (routing/posts-rss))
         posts (->> (posts/posts)
                    (filter visible?)
                    (sort-by #(- (:timestamp %))))]
     (when (seq posts)
       (str
        "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
        (hc/html
         {:mode :xml}
         [:rss
          {:version "2.0"
           :xmlns:atom "http://www.w3.org/2005/Atom"}

          [:channel
           [:atom:link
            {:href self-link
             :rel "self"
             :type "application/rss+xml"}]
           [:title "The Gas Can"]
           [:link self-link]
           [:description "Bill Phillips' personal blog"]
           (map #(vec [:item
                       [:title (:title %)]
                       [:pubDate (->> % (:timestamp) (rfc-1123-format))]
                       [:link
                        (route-to-self
                         (routing/post->title-path %))]])
                posts)]]))))))

(defn posts-view-rss
  "Indirection for dynamic debugging"
  ([& args]
   (apply posts-view-rss-internal args)))

(defn posts-by-date-view
  "Yields a view of posts by date, possibly with criteria.

  (posts-by-date-view ... #{:meta :programming}) is equivalent to
  (posts-by-date-view ... \"meta.programming\").
"
  ([sess query-params]
   (posts-by-date-view sess query-params #{}))
  ([sess query-params criteria]
   (let [{from-post-id :from-post-id} (routing/posts-query-params->map query-params)
         zone (java-time/zone-id "America/Los_Angeles")
         criteria (if (string? criteria)
                    (into #{} (map keyword (clojure.string/split criteria #".")))
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
         status-warning #(let [status (:status %)]
                           (cond (#{:draft} status) 
                                 [:font {:color "red"} " (DRAFT)"]
                                 (#{:soft-published} status)
                                 [:font {:color "orange"} " (SOFT)"]
                                 (not (#{:published} status))
                                 [:font {:color "purple"} 
                                  " (? " (name status) " ?)"]))]
     (when (seq posts-by-day)
       (template/enframe
        "The Gas Can"
        (hc/html
         (map (fn [[date-heading posts]] 
                (list 
                 [:h3 date-heading] 
                 (map 
                  #(vec [:p 
                         (post->link % criteria from-post-id) 
                         (status-warning %)]) posts)))
              posts-by-day))
        :up-link (view-common/up-link "/"))))))

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
