(defproject gascan "0.1.0-SNAPSHOT"
  :description "Gascan is a blogging tool."
  :url "https://github.com/jingibus/gascan"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [
                 [clojure.java-time "0.3.2"]
                 [com.vladsch.flexmark/flexmark-all "0.60.2"]
                 [compojure "1.6.1"]
                 [environ "1.1.0"]
                 [etaoin "0.3.6"]
                 [hiccup "1.0.5"]
                 [org.clojure/clojure "1.10.1"] 
                 [org.clojure/tools.trace "0.7.10"]
                 [ring/ring-core "1.6.3"]
                 [ring/ring-jetty-adapter "1.6.3"]]
  :main ^:skip-aot gascan.core
  :target-path "target/%s"
  :min-lein-version "2.9.2"
  :profiles {:uberjar {:aot :all}
             :dev {}})
