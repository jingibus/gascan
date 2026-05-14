(ns gascan.cli
  (:refer-clojure :exclude [run!])
  (:require [clojure.string :as string]
            [clojure.tools.cli :refer [parse-opts]]
            [environ.core :as environ]
            [gascan.posts :as posts]
            [gascan.remote-posts :as remote-posts]
            [gascan.routing :as routing]
            [gascan.server :as server]
            [gascan.session :as session]))

(def usage
  (string/join
   "\n"
   ["Usage:"
    "  lein run publish SOURCE"
    "  lein run serve [PORT]"
    "  lein run [PORT]"
    ""
    "Commands:"
    "  publish SOURCE   Import SOURCE and add it to the published post catalog."
    "  serve [PORT]     Start the Gascan web server."
    "  help             Show this help."]))

(def help-options
  [["-h" "--help" "Show help."]])

(defn- numeric-string?
  [s]
  (boolean (re-matches #"\d+" (str s))))

(defn- port-number
  [port]
  (Integer. (or port (environ/env :port) 5000)))

(defn- print-error
  [& lines]
  (binding [*out* *err*]
    (doseq [line lines]
      (println line))))

(defn- print-help
  []
  (println usage)
  0)

(defn- print-command-help
  [summary]
  (println usage)
  (when-not (string/blank? summary)
    (println)
    (println "Options:")
    (println summary))
  0)

(defn- print-parse-errors
  [errors]
  (apply print-error (concat errors ["" usage]))
  1)

(defn- print-publish-summary
  [{:keys [title id status src-path markdown-rel-path extra-resources-rel] :as post}]
  (println (str "Published \"" title "\""))
  (println "ID:" id)
  (println "Status:" status)
  (println "Source:" src-path)
  (println "Markdown:" markdown-rel-path)
  (println "URL:" (routing/post->title-path post))
  (when (seq extra-resources-rel)
    (println "Resources:" (count extra-resources-rel))))

(defn publish-source!
  [source]
  (let [post (-> source
                 remote-posts/read-remote-post
                 posts/import-and-add-post!)]
    (print-publish-summary post)
    post))

(defn- publish-command!
  [args]
  (let [{:keys [options arguments errors summary]} (parse-opts args help-options)
        [source & extra-args] arguments]
    (cond
      (:help options)
      (print-command-help summary)

      (seq errors)
      (print-parse-errors errors)

      (nil? source)
      (do
        (print-error "Missing SOURCE for publish." "" usage)
        1)

      (seq extra-args)
      (do
        (print-error "Too many arguments for publish." "" usage)
        1)

      :else
      (do
        (publish-source! source)
        0))))

(defn- serve!
  [port]
  (server/run :port (port-number port) :sess session/public-session)
  0)

(defn- serve-command!
  [args]
  (let [{:keys [options arguments errors summary]} (parse-opts args help-options)]
    (cond
      (:help options)
      (print-command-help summary)

      (seq errors)
      (print-parse-errors errors)

      (> (count arguments) 1)
      (do
        (print-error "Too many arguments for serve." "" usage)
        1)

      :else
      (serve! (first arguments)))))

(defn run!
  [args]
  (let [[command & command-args] (vec args)]
    (cond
      (or 
        (nil? command) 
        (#{"help" "--help" "-h"} command))
      (print-help)

      (#{"serve" "run"} command)
      (serve-command! command-args)

      (= "publish" command)
      (publish-command! command-args)

      :else
      (do
        (print-error (str "Unknown command: " command) "" usage)
        1))))
