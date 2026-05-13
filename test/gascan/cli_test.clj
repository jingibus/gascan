(ns gascan.cli-test
  (:require [clojure.string :as string]
            [clojure.test :refer :all]
            [gascan.cli :as cli]
            [gascan.posts :as posts]
            [gascan.remote-posts :as remote-posts]
            [gascan.server :as server]
            [gascan.session :as session]))

(def published-post
  {:title "CLI Post"
   :id (java.util.UUID/fromString "11111111-2222-3333-4444-555555555555")
   :timestamp 1700000000000
   :status :published
   :filter #{}
   :src-path "/tmp/CLI Post.md"
   :markdown-rel-path "posts/2023/11/14/2213/CLI Post.md"
   :extra-resources-rel []})

(deftest publish-command-uses-existing-import-flow
  (let [calls (atom [])
        remote-post {:remote-post true}]
    (with-redefs [remote-posts/read-remote-post
                  (fn [source]
                    (swap! calls conj [:read source])
                    remote-post)
                  posts/import-and-add-post!
                  (fn [post]
                    (swap! calls conj [:import post])
                    published-post)]
      (let [out (with-out-str
                  (is (= 0 (cli/run! ["publish" "/tmp/CLI Post.md"]))))]
        (is (= [[:read "/tmp/CLI Post.md"]
                [:import remote-post]]
               @calls))
        (is (string/includes? out "Published \"CLI Post\""))
        (is (string/includes? out "URL: /posts/title/cli-post/"))))))

(deftest publish-command-requires-one-source
  (let [err (java.io.StringWriter.)]
    (binding [*err* err]
      (is (= 1 (cli/run! ["publish"]))))
    (is (string/includes? (str err) "Missing SOURCE"))))

(deftest unknown-command-reports-usage
  (let [err (java.io.StringWriter.)]
    (binding [*err* err]
      (is (= 1 (cli/run! ["wat"]))))
    (is (string/includes? (str err) "Unknown command: wat"))
    (is (string/includes? (str err) "lein run publish SOURCE"))))

(deftest serve-command-starts-public-server
  (let [run-args (atom nil)]
    (with-redefs [server/run
                  (fn [& args]
                    (reset! run-args (apply hash-map args))
                    :server)]
      (is (= 0 (cli/run! ["serve" "6000"])))
      (is (= 6000 (:port @run-args)))
      (is (= session/public-session (:sess @run-args))))))

(deftest numeric-argument-preserves-old-server-shortcut
  (let [run-args (atom nil)]
    (with-redefs [server/run
                  (fn [& args]
                    (reset! run-args (apply hash-map args))
                    :server)]
      (is (= 0 (cli/run! ["5001"])))
      (is (= 5001 (:port @run-args))))))
