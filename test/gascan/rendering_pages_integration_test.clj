(ns gascan.rendering-pages-integration-test
  (:require [clojure.string :as string]
            [clojure.test :refer :all]
            [gascan.index-view :as index-view]
            [gascan.post-view :as post-view]
            [gascan.posts-view :as posts-view]
            [gascan.routing :as routing]
            [gascan.session :as session]
            [gascan.site :as site]))

(def los-angeles-zone
  (java-time/zone-id "America/Los_Angeles"))

(defn public-posts
  []
  (site/visible-posts session/public-session))

(defn newest-public-post
  [pred]
  (->> (public-posts)
       (filter pred)
       (sort-by #(- (:timestamp %)))
       first))

(deftest homepage-renders-navigation-and-featured-links
  (let [html (index-view/index-view session/public-session)
        newest-technical (newest-public-post #(contains? (:filter %) :technical))
        newest-firehose (newest-public-post (constantly true))]
    (testing "homepage shell and navigation are present"
      (is (string/includes? html "<title>The Gas Can</title>"))
      (is (string/includes? html "what is it?"))
      (is (string/includes? html (routing/what-it-is-path)))
      (is (string/includes? html (routing/posts-rss))))
    (testing "featured links come from the current published catalog"
      (is (some? newest-technical))
      (is (string/includes? html (:title newest-technical)))
      (is (string/includes? html (routing/post->title-path newest-technical)))
      (is (some? newest-firehose))
      (is (string/includes? html (:title newest-firehose)))
      (is (string/includes? html (routing/post->title-path newest-firehose))))))

(deftest public-posts-index-renders-current-posts
  (let [html (posts-view/posts-by-date-view session/public-session nil)
        newest-post (newest-public-post (constantly true))
        soft-published-title "What I Like About Views"]
    (testing "index shell includes latest published post and navigation"
      (is (some? newest-post))
      (is (string/includes? html (:title newest-post)))
      (is (string/includes? html (routing/post->title-path newest-post)))
      (is (string/includes? html
                            (posts-view/day-key (:timestamp newest-post)
                                                los-angeles-zone)))
      (is (string/includes? html "<a href=\"/\">Up</a>")))
    (testing "soft-published posts stay out of the public index"
      (is (not (string/includes? html soft-published-title))))))

(deftest published-post-detail-renders-title-date-and-body
  (let [post (site/find-post {:title "Please Crash!"})
        html (post-view/post-view session/public-session {:title "Please Crash!"})
        expected-date (posts-view/day-key (:timestamp post) los-angeles-zone)
        expected-up-target (routing/posts-by-date-from-post-id-path (:id post) nil)]
    (testing "published post renders the page shell and content"
      (is (some? post))
      (is (some? html))
      (is (string/includes? html "Please Crash!"))
      (is (string/includes? html "errors that cannot be observed cannot be fixed."))
      (is (string/includes? html expected-date))
      (is (string/includes? html expected-up-target)))))

(deftest audio-post-renders-inline-player
  (let [post (site/find-post {:title "My Heart's Slowing Down"})
        html (post-view/post-view session/public-session {:title "My Heart's Slowing Down"})]
    (testing "audio post includes the inline audio control and asset link"
      (is (some? post))
      (is (some? html))
      (is (string/includes? html "My Heart's Slowing Down"))
      (is (string/includes? html "<audio controls"))
      (is (or (string/includes? html "My%20Heart's%20Slowing%20Down.mp3")
              (string/includes? html "My%20Heart&apos;s%20Slowing%20Down.mp3"))))))

(deftest rss-renders-current-public-posts
  (let [html (posts-view/posts-view-rss session/public-session nil)
        newest-post (newest-public-post (constantly true))
        soft-published-title "What I Like About Views"]
    (testing "rss includes channel metadata and the newest published post"
      (is (some? newest-post))
      (is (string/includes? html "<?xml version=\"1.0\" encoding=\"utf-8\"?>"))
      (is (string/includes? html "<title>The Gas Can</title>"))
      (is (string/includes? html "<link>https://www.billjings.com/rss</link>"))
      (is (string/includes? html (str "<title>" (:title newest-post) "</title>")))
      (is (string/includes? html
                            (str "https://www.billjings.com"
                                 (routing/post->title-path newest-post)))))
    (testing "soft-published posts stay out of the public rss feed"
      (is (not (string/includes? html (str "<title>" soft-published-title "</title>")))))))
