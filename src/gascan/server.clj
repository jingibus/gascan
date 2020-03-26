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
               join? false
               repl? false}
          :as params}]
  (let [repl-params (if repl?
                      {:nrepl {:start? true :port 9000 :host "localhost"}}
                      {})
        ring-params (merge {:port port :join? join?}
                           repl-params)]
    (println "Starting jetty:" ring-params)
    (jty/run-jetty handler ring-params)))


