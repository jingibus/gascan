(ns gascan.index-view
  (:require [gascan.posts-view :as posts-view]
            [gascan.template :as template]
            [hiccup.core :as hc]
            [gascan.posts :as posts]
            [gascan.post-view :as post-view])
  )

(def post-filters
  [["technical" #{:technical}]
   ["spiritual" #{:spiritual}]
   ["music" #{:music}]
   ["clojure" #{:clojure}]
   ["the firehose" #{}]])

(defn index-view
  [sess]
  (let [post-visible-in-session (partial posts/visible-to-session? sess)
        post-filter-para 
        (fn
          [[title post-filter]]
          (let [post-matches-filter #(or (empty? post-filter)
                                         (seq (clojure.set/intersection 
                                               post-filter (:filter %)))) 
                first-post (->> (posts/posts)
                                (filter (every-pred
                                         post-visible-in-session
                                         post-matches-filter))
                                (sort-by (comp - :timestamp))
                                first)]
            (when first-post
              [:p 
               title " - \""
               [:a {:href (post-view/post->title-path first-post)}
                (:title first-post) ]
               "\""
               " -[" [:a {:href (posts-view/posts-by-date-path post-filter)}
                      "index"] "]-"])))]
    (template/enframe
     "The Gas Can"
     (filter identity (map post-filter-para post-filters)))))

(comment
  (gascan.browser/look-at "/")
)
