(ns gascan.browser
  (:require [etaoin.api :as etaoin]
            [etaoin.keys :as k]))

(def lazy-browser (lazy-seq (list (etaoin/chrome))))

(defn browser [] (first lazy-browser))

(defn look-at [path]
  (let [url (do
              ;; Lazy load the server; we don't want to have a dependency loop when
              ;; we use this in view modules. 
              ;;
              ;; This dependency structure is super ugly, but it's the only
              ;; way to build look-at such that it's useful, since we have
              ;; to be able to use it from within a view or any other place
              ;; in the code.
              (require '[gascan.server])
              (str ((resolve 'gascan.server/url-prefix)) "/" path))]
    (etaoin/go (browser) url)
    path))

(comment
  (look-at "test.html"))
