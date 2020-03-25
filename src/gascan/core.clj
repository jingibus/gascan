(ns gascan.core
  (:require [cli-matic.core :refer [run-cmd]]
            [clojure.pprint :refer [pprint]]
            [clojure.tools.trace :refer [trace]]
            [gascan.debug :refer [printlnv monitorv->]]
            [gascan.multimarkdown :refer [render-multimarkdown]]
            [gascan.remote-posts :refer [read-remote-post]]
            [gascan.posts :refer [fetch-posts 
                                  put-posts! 
                                  import-post! 
                                  as-parsed]]
            ;;[hiccup.core :refer [html]]
            )
  (:require [gascan.debug])
  (:gen-class))

(defn import-remote-post-by-path!
  "Imports a remote post and saves it to the posts store."
  [filepath]
  (let [remote-post (-> (read-remote-post filepath) import-post!)
        posts (fetch-posts)
        conflicting-titles (filter #(= (:title %) (:title remote-post)) posts)]
    (printlnv "initial posts: " posts "\n new post: " remote-post)
    (->> remote-post 
        (conj posts)
        (put-posts!))
    remote-post))

(defn new-post-command
  [{:keys [file]}]
  (pprint (import-remote-post-by-path! file)))

(defn html
  [args]
  args)

(defn render-post
  [interned-parsed-post]
  (let [parsed-markdown (:parsed-markdown interned-parsed-post)]
    (-> interned-parsed-post
        :parsed-markdown
        render-multimarkdown
        html
        println)))

(defn render-post-command
  [args]
  (let [post (as-parsed (last (sort #(apply compare (map :timestamp [%1 %2])) (fetch-posts))))]
    (render-post post)))

(def CONFIGURATION
  {:app         {:command        "gascan"
                 :description    "A small blog content tool."}
   :commands    [{:command       "new"
                  :description   "Add a new post."
                  :opts          [{:option "file" :as "MultiMarkdown File" 
                                   :type :string
                                   :default :present}]
                  :runs          new-post-command}
                 {:command       "render"
                  :description   "Render a post."
                  :opts          []
                  :runs          render-post-command}]})

(defn -main
  [& args]
  (run-cmd args CONFIGURATION))
