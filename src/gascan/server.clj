(ns gascan.server
  (:require [compojure.route :as route]
            [gascan.index-view :as index-view]
            [gascan.post-view :as post-view]
            [gascan.posts-view :as posts-view]
            [gascan.session :as session]
            [gascan.template :as tmpl]
            [hiccup.core :as hc]
            [ring.adapter.jetty :as jty]
            [gascan.browser :as browser])
  (:use compojure.core gascan.debug))

(def content-not-found-page
  (tmpl/enframe 
   "The Gas Can - The Unknown"
   (cons [:h2 "The Content Was Not Found"]
         (map (fn [x] [:p x])
              ["It all started one day when I wanted to look at a page on The Gas Can."
               "I navigated to the page, and it was not there."
               "I was so disconsolate that I hung my head in despair."
               "My vision came to rest upon my right tennis shoe, which I had taken off earlier."
               "There was something peeking out of it."
               "What was it?"
               "It was a $404 bill."
               "Solent."]))))

(comment
  (do
    (require '[gascan.browser :as browser])
    (browser/look-at "posts/id/5000")
    (post-view/view-post {:id "5000"})
    content-not-found-page
    (browser/look-at "posts/title/blog-project")))

((GET "/:test1/route?parameter1=:param&parameter2=:param2" [test1 param param2]
       (println test1 param param2))
 {:request-method :get 
  :uri  "/abbleton/route?parameter2=fluff&parameter1=ernutterer"
  :scheme :http
  :server-name "localhost"})

;; Compojure routing
(defn all-routes
  [sess]
  (let [sess session/public-session]
    (routes
     (GET "/:path{|index.htm|index.html}" [path]
          (index-view/index-view sess))
     (GET (post-view/post-by-title-path ":title") [title]
          (println "route by title:" title)
          (post-view/post-view sess {:title title}))
     (GET (post-view/post-by-id ":id") [id]
          (println "route by id:" id)
          (post-view/post-view sess {:id id}))
     (GET (posts-view/posts-by-date-path ":criteria") [criteria]
          (println "route to posts matching criteria " criteria)
          (posts-view/posts-by-date-view sess criteria))
     (GET (posts-view/posts-by-date-path) []
          (println "route to all posts")
          (posts-view/posts-by-date-view sess))
     (GET (posts-view/posts-by-date-path "") []
          (println "route to all posts")
          (posts-view/posts-by-date-view sess))
     (GET "/favicon.ico" []
          (println "it's that favicon")
          {:status 200
           :headers {"Content-Type" "image/png"}
           :body (gascan.intern/readable-file "favicon.png")})
     (GET [":unknown-route", :unknown-route #".*"] [unknown-route]
          (println "Unknown path:" unknown-route)
          {:status 404
           :headers {"Content-Type" "text/html"}
           :body content-not-found-page}))))
(comment
  (defroutes all-routes
    (GET "/:path{|index.htm|index.html}" [path]
         (index-view/index-view))
    (GET (post-view/post-by-title-path ":title") [title]
         (println "route by title:" title)
         (post-view/post-view {:title title}))
    (GET (post-view/post-by-id ":id") [id]
         (println "route by id:" id)
         (post-view/post-view {:id id}))
    (GET (posts-view/posts-by-date-path ":criteria") [criteria]
         (println "route to posts matching criteria " criteria)
         (posts-view/posts-by-date-view criteria))
    (GET (posts-view/posts-by-date-path) []
         (println "route to all posts")
         (posts-view/posts-by-date-view))
    (GET (posts-view/posts-by-date-path "") []
         (println "route to all posts")
         (posts-view/posts-by-date-view))
    (GET "/favicon.ico" []
         (println "it's that favicon")
         {:status 200
          :headers {"Content-Type" "image/png"}
          :body (gascan.intern/readable-file "favicon.png")})
    (GET [":unknown-route", :unknown-route #".*"] [unknown-route]
         (println "Unknown path:" unknown-route)
         {:status 404
          :headers {"Content-Type" "text/html"}
          :body content-not-found-page})))

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
           "I wrote and I wrote and I wrote. Aaaaaand I wrote."
           "\"What shall I do with all this writing?\" I asked myself."
           "And so I decided to create a blog."])
     ]
    ]))

(defn render-success
  [request]
  (render-template "Hello World"))

(defn handler 
  []
  (all-routes (gascan.session/make-session :public false)))

(defn run
      [& {:keys [port join? repl?]
          :or {port 3000
               join? false}
          :as params}]
  (let [ring-params {:port port :join? join?}]
    (println "Starting jetty:" ring-params)
    (jty/run-jetty (handler) ring-params)))

(defonce lazy-server (lazy-seq (list (run))))
(comment
  ;; restart
  (do 
    (.stop (server))
    (def lazy-server (lazy-seq (list (run))))
    )
  )

(defn server [] (first lazy-server))

(comment
  (.stop (server))
  (require '[gascan.browser :as browser])
  (browser/look-at "favicon.ico")
  (browser/look-at "/posts")
  (browser/look-at "/index.html")
  (browser/look-at "/posts/title/blog-project")
  (browser/look-at "/posts/criteria/")
)

(defn url-prefix [] 
  (let [port (-> (server) .getConnectors (get 0) .getPort)]
    (str "http://localhost:" port)))

