(ns gascan.debug (:gen-class))

(def ^:dynamic *verbose* false)

(defmacro printlnv
  [& args]
  `(when *verbose*
     (printf ~@args)))

(defmacro with-verbose
  [& body]
  `(binding [*verbose* true] ~@body))

