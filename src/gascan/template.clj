(ns gascan.template
  (:require [hiccup.core :as hc]))

(defn enframe
  [title body]
  (hc/html
   [:html
    [:head
     [:title title]]
    [:body body]]))
