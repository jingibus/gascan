(ns gascan.core
  (:require [environ.core :as environ]
            [gascan.server :as svr]
            [gascan.session :as session])
  (:require [gascan.debug])
  (:gen-class))

(defn -main [& [port]]
  (let [port (Integer. (or port (environ/env :port) 5000))]
    (svr/run :port port :session session/public-session)))
