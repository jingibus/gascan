(ns gascan.metadata-shape-test
  (:require [clojure.spec.alpha :as s]
            [clojure.test :refer :all]
            [gascan.intern :as intern]
            [gascan.post-spec :as post-spec]
            [gascan.posts :as posts])
  (:import [java.nio.file Files]))

(def sample-markdown-path
  "posts/2024/02/18/1755/Please Crash.md")

(def sample-resource-path
  "posts/2020/07/26/2132/My Heart's Slowing Down.mp3")

(def sample-timestamp
  1708307706041)

(defn persisted-post
  [& {:as overrides}]
  (merge {:id (java.util.UUID/fromString "12345678-1234-1234-1234-123456789abc")
          :title "Metadata Test Post"
          :timestamp sample-timestamp
          :markdown-rel-path sample-markdown-path
          :extra-resources-rel []
          :filter #{:technical}
          :status :published}
         overrides))

(deftest fetch-posts-loads-current-metadata
  (let [loaded-posts (posts/fetch-posts)]
    (testing "the current metadata file loads and validates"
      (is (seq loaded-posts))
      (is (every? #(s/valid? post-spec/persisted-intern-post %) loaded-posts))
      (is (some :src-path loaded-posts))
      (is (every? #(s/valid? post-spec/intern-post %) loaded-posts)))))

(deftest persisted-post-spec-covers-optional-src-path
  (testing "a minimal persisted post validates without src-path"
    (is (s/valid? post-spec/persisted-intern-post
                  (persisted-post))))
  (testing "a persisted post with src-path validates"
    (is (s/valid? post-spec/persisted-intern-post
                  (persisted-post :src-path "/Users/example/Documents/post.md"))))
  (testing "missing required fields are rejected"
    (is (not (s/valid? post-spec/persisted-intern-post
                       (dissoc (persisted-post) :status)))))
  (testing "src-path must be a string when present"
    (is (not (s/valid? post-spec/persisted-intern-post
                       (persisted-post :src-path 42)))))
  (testing "status must still be one of the allowed values"
    (is (not (s/valid? post-spec/persisted-intern-post
                       (persisted-post :status :scheduled))))))

(deftest metadata-round-trip-preserves-valid-posts
  (let [temp-dir (.toFile (Files/createTempDirectory "gascan-metadata-test"
                                                     (make-array java.nio.file.attribute.FileAttribute 0)))
        relpath "metadata-roundtrip.edn"
        posts-to-write [(persisted-post
                         :src-path "/Users/example/Documents/post.md")
                        (-> (persisted-post
                             :id (java.util.UUID/fromString "abcdefab-cdef-cdef-cdef-abcdefabcdef")
                             :title "Round Trip Without Src Path"
                             :status :soft-published
                             :filter #{:audio}
                             :extra-resources-rel [sample-resource-path])
                            (dissoc :src-path))]]
    (with-redefs [intern/resources-folder (.getAbsolutePath temp-dir)]
      (intern/intern-edn! relpath posts-to-write)
      (let [read-back (intern/read-edn {} relpath)]
        (testing "round-trip metadata is structurally preserved"
          (is (= posts-to-write read-back)))
        (testing "round-tripped posts still satisfy the persisted spec"
          (is (every? #(s/valid? post-spec/persisted-intern-post %) read-back)))))))
