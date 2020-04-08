(ns gascan.debug
  (:require [clojure.pprint :refer [pprint *print-right-margin*]])
 (:gen-class))

(def ^:dynamic *verbose* false)

(defmacro names-and-vals
  ([] nil)
  ([x]
   `(list (~str (~name (quote ~x)) ":") ~x))
  ([x & xs] 
   `(concat (~list (~str (~name (quote ~x)) ":") ~x)
          (names-and-vals ~@xs))))

(defmacro pprint-symbols
  [& args]
  (list 
'clojure.pprint/pprint `(names-and-vals ~@args)))

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

(defn monitor->>
  ([arg]
   (monitor-> arg ""))
  ([name arg]
   (monitor-> arg name identity))
  ([name txform arg]
   (monitor-> arg name txform)))

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

(defn show-methods
  [instance]
  (->>  instance
        .getClass
        .getMethods
        (sort-by #(.getName %))
        (map str)
        distinct))
