(ns gascan.index-view
  (:require [gascan.posts-view :as posts-view]
            [gascan.template :as template]
            [hiccup.core :as hc]
            [gascan.posts :as posts]
            [gascan.routing :as routing])
  )

(def post-filters
  [["technical" #{:technical}]
   ["spiritual" #{:spiritual}]
   ["music" #{:music}]
   ["audio" #{:audio}]
   ["clojure" #{:clojure}]
   ["meta" #{:meta}]
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
                                first)
                draft-warning (when-not (#{:published} (:status first-post)) 
                                [:font {:color "red"} " (DRAFT) "])]
            (when first-post
              [:p 
               title " - \""
               [:a {:href (routing/post->title-path first-post)}
                (:title first-post) ]
               "\""
               draft-warning
               " -[" [:a {:href (routing/posts-by-date-path post-filter)}
                      "index"] "]-"])))]
    (template/enframe
     "The Gas Can"
     (list
      (filter identity (map post-filter-para post-filters))
      [:div {:style "justify-content:flex-end; display: flex"}
       [:p
        "..."
        [:a {:href (routing/what-it-is-path)}
         "what is it?"]]]
      [:div {:style "justify-content:flex-end; display: flex"}
       [:p
        [:a {:href (routing/posts-rss)}
         "rss"]]]
)
     )))

(comment
  (gascan.browser/look-at "/")
)
