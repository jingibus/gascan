(ns gascan.session
  (:require [clojure.spec.alpha :as s]))

(s/def ::public boolean?)

(defn make-session 
  [& {:keys [public]}]
  {:public public})

(s/fdef
    :args (s/cat :kwargs (s/keys* :req-un [::public]))
    :ret (s/keys :req-un [::public]))
