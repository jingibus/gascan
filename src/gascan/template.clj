(ns gascan.template
  (:require [hiccup.core :as hc]))

(defn enframe
  [title body]
  (hc/html
   [:html
    [:head
     [:link {:href "https://fonts.googleapis.com/css2?family=Domine&display=swap" :rel "stylesheet"}]
     [:style "h1, h2, h3 { font-family: 'Domine', serif; }"]
     [:title title]]
    [:body 
     [:div {:style "padding: 100px; margin 50px; background-color: #dddddd;"}
      [:h1 title]
      body]]]))
