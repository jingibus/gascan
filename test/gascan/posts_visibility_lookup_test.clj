(ns gascan.posts-visibility-lookup-test
  (:require [clojure.string :as string]
            [clojure.test :refer :all]
            [gascan.posts :as posts]
            [gascan.posts-view :as posts-view]
            [gascan.session :as session]))

(deftest visible-to-session-rules
  (let [published-post {:status :published}
        soft-published-post {:status :soft-published}]
    (testing "public sessions only see published posts in indexes"
      (is (true? (posts/visible-to-session? session/public-session published-post)))
      (is (false? (posts/visible-to-session? session/public-session soft-published-post))))
    (testing "private sessions see all posts"
      (is (true? (posts/visible-to-session? session/private-session published-post)))
      (is (true? (posts/visible-to-session? session/private-session soft-published-post))))))

(deftest title-locator-lookup-normalizes-to-slugs
  (let [sample-posts [{:title "Hello, World?"
                       :id (java.util.UUID/fromString
                            "11111111-1111-1111-1111-111111111111")
                       :timestamp 1700000000000
                       :status :published
                       :filter #{:technical}}
                      {:title "Another Post"
                       :id (java.util.UUID/fromString
                            "22222222-2222-2222-2222-222222222222")
                       :timestamp 1690000000000
                       :status :published
                       :filter #{:spiritual}}]]
    (with-redefs [posts/posts (fn [] sample-posts)]
      (testing "find-post matches a kebab-cased route slug against the title"
        (is (= "Hello, World?"
               (:title (posts/find-post {:title "hello-world"})))))
      (testing "find-post still works with the literal title"
        (is (= "Hello, World?"
               (:title (posts/find-post {:title "Hello, World?"})))))
      (testing "unknown slugs return nil"
        (is (nil? (posts/find-post {:title "not-here"})))))))

(deftest id-locator-lookup-matches-string-and-case-insensitively
  (let [target-id (java.util.UUID/fromString "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee")
        sample-posts [{:title "By Id"
                       :id target-id
                       :timestamp 1700000000000
                       :status :published
                       :filter #{}}
                      {:title "Someone Else"
                       :id (java.util.UUID/fromString
                            "ffffffff-1111-2222-3333-444444444444")
                       :timestamp 1690000000000
                       :status :published
                       :filter #{:technical}}]]
    (with-redefs [posts/posts (fn [] sample-posts)]
      (testing "string locators match UUID-backed post ids"
        (is (= "By Id"
               (:title (posts/find-post {:id (str target-id)})))))
      (testing "id lookup is case-insensitive"
        (is (= "By Id"
               (:title (posts/find-post {:id (string/upper-case (str target-id))}))))))))

(deftest criteria-filtering-respects-session-visibility
  (let [sample-posts [{:title "Newest Technical"
                       :id (java.util.UUID/fromString
                            "10101010-1111-2222-3333-444444444444")
                       :timestamp 1700000000000
                       :status :published
                       :filter #{:technical}}
                      {:title "Soft Technical"
                       :id (java.util.UUID/fromString
                            "20202020-1111-2222-3333-444444444444")
                       :timestamp 1695000000000
                       :status :soft-published
                       :filter #{:technical}}
                      {:title "Spiritual Post"
                       :id (java.util.UUID/fromString
                            "30303030-1111-2222-3333-444444444444")
                       :timestamp 1690000000000
                       :status :published
                       :filter #{:spiritual}}]]
    (with-redefs [posts/posts (fn [] sample-posts)]
      (let [public-html (posts-view/posts-by-date-view
                         session/public-session nil #{:technical})
            private-html (posts-view/posts-by-date-view
                          session/private-session nil #{:technical})]
        (testing "public criteria views include published matches only"
          (is (string/includes? public-html "Newest Technical"))
          (is (not (string/includes? public-html "Soft Technical")))
          (is (not (string/includes? public-html "Spiritual Post"))))
        (testing "private criteria views include soft-published matches too"
          (is (string/includes? private-html "Newest Technical"))
          (is (string/includes? private-html "Soft Technical"))
          (is (not (string/includes? private-html "Spiritual Post"))))))))
