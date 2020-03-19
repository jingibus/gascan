(ns gascan.debug (:gen-class))

(def ^:dynamic *verbose* false)

(defmacro printlnv
  [& args]
  `(when *verbose*
     (println ~@args)))

(defn monitorv->
  ([arg]
   (monitorv-> "" arg))
  ([arg name]
   (do
     (printlnv name "\"" (str arg) "\"")
     arg)))

(defn monitorv->>
  ([arg]
   (monitorv-> arg))
  ([name arg]
   (monitorv-> arg name)))
  
(defmacro with-verbose
  [& body]
  `(binding [*verbose* true] ~@body))

