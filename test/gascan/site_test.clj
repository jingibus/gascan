(ns gascan.site-test
  (:require [clojure.test :refer :all]
            [gascan.session :as session]
            [gascan.site :as site]))

(def sample-posts
  [{:title "Newest Technical"
    :id (java.util.UUID/fromString "01010101-1111-2222-3333-444444444444")
    :timestamp 1700000000000
    :status :published
    :filter #{:technical}}
   {:title "Soft Technical"
    :id (java.util.UUID/fromString "02020202-1111-2222-3333-444444444444")
    :timestamp 1695000000000
    :status :soft-published
    :filter #{:technical}}
   {:title "Published Spiritual"
    :id (java.util.UUID/fromString "03030303-1111-2222-3333-444444444444")
    :timestamp 1690000000000
    :status :published
    :filter #{:spiritual}}])

(deftest site-model-wraps-read-only-post-access
  (with-redefs [site/all-posts (fn [] sample-posts)]
    (testing "all-posts returns the catalog"
      (is (= sample-posts (vec (site/all-posts)))))
    (testing "visible-posts applies the session visibility rule"
      (is (= ["Newest Technical" "Published Spiritual"]
             (mapv :title (site/visible-posts session/public-session))))
      (is (= ["Newest Technical" "Soft Technical" "Published Spiritual"]
             (mapv :title (site/visible-posts session/private-session)))))
    (testing "criteria normalization and filtering work for strings and sets"
      (is (= #{:technical} (site/criteria->set "technical")))
      (is (= #{:technical :spiritual} (site/criteria->set #{:technical :spiritual})))
      (is (= ["Newest Technical"]
             (mapv :title (site/visible-posts-by-criteria session/public-session "technical"))))
      (is (= ["Newest Technical" "Soft Technical"]
             (mapv :title (site/visible-posts-by-criteria session/private-session #{:technical})))))
    (testing "newest-visible-post-by-criteria returns the latest visible match"
      (is (= "Newest Technical"
             (:title (site/newest-visible-post-by-criteria session/public-session #{:technical}))))
      (is (= "Published Spiritual"
             (:title (site/newest-visible-post-by-criteria session/public-session #{:spiritual})))))
    (testing "find-post delegates locator-based lookup"
      (is (= "Newest Technical"
             (:title (site/find-post {:title "newest-technical"}))))
      (is (= "Published Spiritual"
             (:title (site/find-post {:id "03030303-1111-2222-3333-444444444444"})))))))
