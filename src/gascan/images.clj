(ns gascan.images
  (:require [org.bovinegenius.exploding-fish.query-string :as query-string]
            [gascan.debug :refer :all]
            [org.bovinegenius.exploding-fish :as uri]
            [image-resizer.format :as image-format]
            [image-resizer.core :as image-resizer]))

(defn scale-image
  [image-stream width ext]
  (-> image-stream
      (image-resizer/resize width width)
      (image-format/as-stream ext)))

(defn scale-request-if-necessary
  [{query-string :query-string uri :uri :as request} response]
  (let [query-params (into {} 
                           (query-string/query-string->alist query-string))
        width (get query-params "width")
        width (and width (min 1000 (Integer/parseInt width)))
        uri (uri/uri uri)
        content-type (get-in response [:headers "Content-Type"])
        ext (second (re-find #"\.([^.]*)$" (:path uri)))]
    (pprint-symbols query-params response uri ext content-type width)
    (if (and width (.startsWith content-type "image"))
      (let [new-body (scale-image (:body response) width ext)]
        (-> response
            (assoc :body new-body)
            (monitor-> "body:" :body)
            (assoc-in [:headers "Content-Length"] 
                      (str (.available new-body)))
            (monitor->)))
      response)))

(defn wrap-image-scale 
  [handler]
  (fn [request]
    (scale-request-if-necessary request (handler request))))

(comment
  (defn serve-image
    [image-path width]
    (let [image-stream (gascan.intern/readable-file (str "images/" image-path))
          ext (second (re-find #"\.([^.]*)$" image-path))
          ;; If there's a width, parse it and limit it so that we aren't
          ;; serving any ginormous images.
          width (and width (min 500 (Integer/parseInt width)))]
      (pprint-symbols ext width image-path)
      (if width
        (-> image-stream
            (image-resizer/resize width width)
            (monitor-> "image" #(str (.getHeight %) " " (.getWidth %)))
            (image-format/as-stream ext))
        image-stream))))
