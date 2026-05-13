(ns gascan.cli
  (:refer-clojure :exclude [run!])
  (:require [clojure.string :as string]
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
  [[source & extra-args]]
  (cond
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
      0)))

(defn- serve!
  [port]
  (server/run :port (port-number port) :sess session/public-session)
  0)

(defn run!
  [args]
  (let [[command & command-args] (vec args)]
    (cond
      (nil? command)
      (serve! nil)

      (numeric-string? command)
      (serve! command)

      (#{"help" "--help" "-h"} command)
      (print-help)

      (#{"serve" "run"} command)
      (if (> (count command-args) 1)
        (do
          (print-error "Too many arguments for serve." "" usage)
          1)
        (serve! (first command-args)))

      (= "publish" command)
      (publish-command! command-args)

      :else
      (do
        (print-error (str "Unknown command: " command) "" usage)
        1))))
