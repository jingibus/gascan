(ns gascan.core
  (:require [gascan.cli :as cli])
  (:require [gascan.debug])
  (:gen-class))

(defn -main [& args]
  (let [exit-code (cli/run! args)]
    (when-not (zero? exit-code)
      (System/exit exit-code))))
