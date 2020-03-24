(ns gascan.debug
  (:require [clojure.pprint :refer [pprint *print-right-margin*]])
 (:gen-class))

(def ^:dynamic *verbose* false)

(defmacro printlnv
  [& args]
  `(when *verbose*
     (println ~@args)))

(defn pprint-if-necessary 
  [value]
  (if (coll? value)
    (binding [*print-right-margin* 80]
      (with-out-str (pprint value)))
    (str value)))

(defn monitor->
  ([arg]
   (monitor-> arg ""))
  ([arg name]
   (monitor-> arg name identity))
  ([arg name txform]
   (do
     (println name (pprint-if-necessary (-> arg txform)))
     arg)))

(defn monitorv->
  ([arg]
   (monitorv-> arg ""))
  ([arg name]
   (monitorv-> arg name identity))
  ([arg name txform]
   (do
     (printlnv name (pprint-if-necessary (-> arg txform)))
     arg)))

(defn monitorv->>
  ([arg]
   (monitorv-> arg))
  ([name arg]
   (monitorv-> arg name)))
  
(defmacro with-verbose
  [& body]
  `(binding [*verbose* true] ~@body))

