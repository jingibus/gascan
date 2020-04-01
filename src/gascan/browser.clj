(ns gascan.browser
  (:require [etaoin.api :as etaoin]
            [etaoin.keys :as k]))

(def lazy-browser (lazy-seq (list (etaoin/chrome))))

(defn browser [] (first lazy-browser))

(defn look-at [path]
  (let [url (do
              ;; Lazy load the server; we don't want to have a dependency loop when
              ;; we use this in view modules.
              (require '[gascan.server])
              (str (gascan.server/url-prefix) "/" path))]
    (etaoin/go (browser) url)
    path))

(comment
  (look-at "test.html"))
