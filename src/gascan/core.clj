(ns gascan.core
  (:require [cli-matic.core :refer [run-cmd]]
            [gascan.posts :refer [fetch-posts put-posts! import-post!]]
            [gascan.multimarkdown :refer [read-remote-post]]
            [gascan.debug :refer [printlnv]]
            [clojure.tools.trace :refer [trace]])
  (:require [gascan.debug])
  (:gen-class))

(defn import-remote-post-by-path!
  "Imports a remote post and saves it to the posts store."
  [filepath]
  (-> (read-remote-post filepath)
      import-post!
      ((fn [post] 
         (let [posts (fetch-posts)]
           (printlnv "initial posts: " posts "\n new post: " post)
           (conj posts post))))
      ((fn [posts] 
         (println "posts:" posts)
         (put-posts! posts)))))

(defn new-post
  [{:keys [file]}]
  (import-remote-post-by-path! file))

(def CONFIGURATION
  {:app         {:command        "gascan"
                 :description    "A small blog content tool."}
   :commands    [{:command       "new"
                  :description   "Add a new post."
                  :opts          [{:option "file" :as "MultiMarkdown File" 
                                   :type :string
                                   :default :present}]
                  :runs          new-post}]})

(defn -main
  [& args]
  (run-cmd args CONFIGURATION))
