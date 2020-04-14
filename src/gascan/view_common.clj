(ns gascan.view-common)

(defn up-link
  [target]
  (list "-" [:a {:href target} "Up"] "-^"))
