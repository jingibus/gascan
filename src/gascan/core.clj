(ns gascan.core
  (:require [cli-matic.core :refer [run-cmd]]
            [gascan.multimarkdown :refer [parse-multimarkdown-flat]])
  (:gen-class))

(defn new-post
  [{:keys [file]}]
  (println "Contents of MultiMarkdown file:" file))

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
