(ns gascan.session
  (:require [clojure.spec.alpha :as s]))

(s/def ::public boolean?)
(def session-spec (s/keys :req-un [::public]))

(defn make-session 
  [& {:keys [public]}]
  {:public public})

(s/fdef
    make-session
    :args (s/cat :kwargs (s/keys* :req-un [::public]))
    :ret session-spec)

(def private-session (make-session :public false))
(def public-session (make-session :public true))

