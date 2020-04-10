(ns gascan.rxn
  (:require [clojure.pprint :as pp]))

(defn show-methods
  [instance]
  (->>  instance
        .getClass
        .getMethods
        (sort-by #(.getName %))
        (map str)
        distinct))

(defn find-field
  [klass field-name]
  (let [pull-field (fn [k] (try (.getDeclaredField k field-name)
                                (catch Exception e nil)))]
    (loop [superklass (.getSuperclass klass)
           field (pull-field klass)]
      (cond field
            field
            (or (nil? superklass) (= Object superklass))
            nil
            :else
            (recur (.getSuperclass superklass) (pull-field superklass)))      )))

(defn get-private-field
  [instance field-name]
  (let [method (find-field (.getClass instance) field-name)]
    (.setAccessible method true)
    (.get method instance)))

(defn show-fields
  [s instance field-names]
  (pp/pprint [s instance (map #(vector % (get-private-field instance %)) field-names)]))

(defn dyn-construct [klass & args]
  "Dynamically invokes constructor for klass. I expect this will get messy if 
there are multiple constructor overloads with the same arity."
  (clojure.lang.Reflector/invokeConstructor klass (into-array Object args)))
