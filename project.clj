(defproject gascan "0.1.0-SNAPSHOT"
  :description "Gascan is a blogging tool."
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [
                 [org.clojure/clojure "1.10.1"] 
                 [cli-matic "0.3.11"]
                 [com.vladsch.flexmark/flexmark-all "0.60.2"]]
  :main ^:skip-aot gascan.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
