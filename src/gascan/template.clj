(ns gascan.template
  (:require [hiccup.core :as hc]))

(defn enframe
  [title body]
  (hc/html
   [:html
    [:head
     [:title title]]
    [:body 
     [:div {:style "padding: 100px; margin 50px; background-color: #dddddd;"}
      [:h1 title]
      body]]]))
