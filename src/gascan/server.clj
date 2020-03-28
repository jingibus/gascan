(ns gascan.server
  (:require [ring.adapter.jetty :as jty]
            [hiccup.core :as hc]))

(defn render-template
  [inner-html]
  (hc/html 
   [:html 
    [:head 
     [:title "The Gas Can"]]
    [:body
     [:h1 "The Blog Project"]
     (map (fn [x] [:p x])
          ["It all started one day when I wanted to write."
           "I wrote and I wrote and I wrote."
           "\"What shall I do with all this writing?\" I asked myself."
           "And so I decided to create a blog."])
     ]
    ]))

(defn render-success
  [request]
  (render-template "Hello World"))

(defn handler [request]
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (render-success request)})

(defn run
      [& {:keys [port join? repl?]
          :or {port 3000
               join? false}
          :as params}]
  (let [ring-params {:port port :join? join?}]
    (println "Starting jetty:" ring-params)
    (jty/run-jetty handler ring-params)))

(def lazy-server (lazy-seq (list (run))))

(defn server [] (first lazy-server))

(defn url-prefix [] 
  (let [port (-> (server) .getConnectors (get 0) .getPort)]
    (str "http://localhost:" port)))

(comment
  )
