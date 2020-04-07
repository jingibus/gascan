;; Tools for managing interned files.
(ns gascan.intern
  (:refer-clojure)
  (:require 
   [clojure.edn :as edn]
   [clojure.reflect :refer [reflect]]
   [clojure.java.io :refer [as-file 
                            file 
                            make-parents
                            copy
                            resource
                            reader]]
   [clojure.string :as string])
  (:gen-class))

(def project-folder (System/getProperty "user.dir"))

(def resources-folder (string/join "/" [project-folder "resources"]))

(defn interned-filepath
  [filepath reldest folder-depth]
  (let [fileobj (as-file filepath)
        trimmed-filepath (string/replace filepath #"/*$" "")
        components (string/split trimmed-filepath #"/")
        intern-components (concat (list reldest) (take-last (+ folder-depth 1) components))
        intern-rel-filepath (string/join "/" intern-components)
        ]
    intern-rel-filepath))

(defn intern-abs-filepath
  [intern-rel-filepath]
  (string/join "/" [resources-folder intern-rel-filepath]))

(defn intern-file!
  ([filepath reldest folder-depth]
   (let [fileobj (as-file filepath)]
     (if (.isFile fileobj)
       (intern-file! 
        filepath
        reldest
        folder-depth
        (clojure.java.io/input-stream fileobj))
       (throw (new java.io.IOException "Cannot interna a non-file")))))
  ([filepath reldest folder-depth contents]
   (let [intern-rel-filepath (interned-filepath filepath reldest folder-depth)
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
  "Reads in EDN from the given path. Opts is passed in to edn/read."
  [opts relpath]
  (some->>
   relpath
   resource
   (.openStream)
   reader
   (java.io.PushbackReader.)
   (edn/read opts)))

(defn readable-file
  "Yields a readable file at relative filepath."
  [relpath]
  (some->>
   relpath
   resource
   (.openStream)))
