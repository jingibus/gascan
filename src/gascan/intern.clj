;; Tools for managing interned files.
(ns gascan.intern
  (:refer-clojure)
  (:require 
   [clojure.edn :refer [read]]
   [clojure.reflect :refer [reflect]]
   [clojure.java.io :refer [as-file 
                            file 
                            make-parents
                            copy
                            resource
                            reader]]
   [clojure.string :refer [join split replace]])
  (:gen-class))

(def project-folder (System/getProperty "user.dir"))

(def resources-folder (join "/" [project-folder "resources"]))

(defn interned-filepath
  [filepath folder-depth]
  (let [fileobj (as-file filepath)
        trimmed-filepath (replace filepath #"/*$" "")
        components (split trimmed-filepath #"/")
        intern-components (take-last (+ folder-depth 1) components)
        intern-rel-filepath (join "/" intern-components)
        ]
    intern-rel-filepath))

(defn intern-abs-filepath
  [intern-rel-filepath]
  (join "/" [resources-folder intern-rel-filepath]))

(defn intern-file!
  ([filepath folder-depth]
   (let [fileobj (as-file filepath)]
     (if (.isFile fileobj)
       (intern-file! filepath folder-depth (slurp fileobj))
       (throw (new java.io.IOException "Cannot interna a non-file")))))
  ([filepath folder-depth contents]
   (let [intern-rel-filepath (interned-filepath filepath folder-depth)
         intern-abs-filepath (intern-abs-filepath intern-rel-filepath)
         intern-fileobj (as-file intern-abs-filepath)]
     (do 
       (make-parents intern-fileobj)
       (copy contents intern-fileobj)
       intern-rel-filepath))))

(defn intern-edn!
  [relpath structure]
  (let [abspath (intern-abs-filepath relpath)
        edn-repr (prn-str structure)]
    (spit abspath edn-repr)))

(defn read-edn
  [relpath]
  (read (java.io.PushbackReader. (reader (.openStream (resource relpath))))))
