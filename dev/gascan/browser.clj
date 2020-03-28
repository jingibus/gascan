(ns gascan.browser
  (:require [etaoin.api :as etaoin]
            [etaoin.keys :as k]))

(def lazy-browser (lazy-seq (list (etaoin/chrome))))

(defn browser [] (first lazy-browser))

