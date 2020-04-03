(ns gascan.index-view
  (:require [gascan.posts-view :as posts-view]
            [gascan.template :as template]
            [hiccup.core :as hc]
            [gascan.posts :as posts]
            [gascan.post-view :as post-view])
  )

(defn index-view
  []
  (let [first-post (first (sort-by #(- (:timestamp %)) (posts/posts)))]
    (template/enframe
     "The Gas Can"
     (list
      [:p 
       "the firehose - \""
       [:a {:href (post-view/post->title-path first-post)}
        (:title first-post) ]
       "\""
       " -[" [:a {:href (posts-view/posts-by-date-path)}
             "index"] "]-"]
      )))
  )

(comment
  (gascan.browser/look-at "/")
)
