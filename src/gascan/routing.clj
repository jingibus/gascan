(ns gascan.routing
  (:require [clojure.string :as string]
            [org.bovinegenius.exploding-fish :as uri]
            [gascan.posts :as posts]
            [org.bovinegenius.exploding-fish.query-string :as query-string])
  (:import [java.util UUID]))

(defn posts-by-date-path
  ([]
   "/posts")
  ([criteria]
   (let [criteria (if (string? criteria) #{(keyword criteria)} criteria)
         criteria-kebab (clojure.string/join "-" (map name criteria))]
     (str "/posts/criteria/" criteria-kebab))))

(defn posts-by-date-from-post-id-path
  [post-id & base-path-args]
  (let [base-uri (uri/uri (apply posts-by-date-path base-path-args))
        query-params (query-string/alist->query-string
                      [["from-post-id" post-id]])]
    (uri/map->string (assoc base-uri :query query-params))))

(defn post-by-title-path
  [title]
  (str "/posts/title/" title "/"))

(defn post-resources-by-title-path
  [title res-name]
  (str "/posts/title/" title "/" res-name))

(defn post-by-id
  [id]
  (str "/posts/id/" id "/"))

(defn post-resources-by-id
  [id res-name]
  (str "/posts/id/" id "/" res-name))

(defn post->title-path
  ([post]
   (post->title-path post nil))
  ([post parent-criteria]
   (let [uri (uri/uri 
              (str "/posts/title/" (posts/title->locator-string (:title post)) "/"))
         joined-criteria (some->> parent-criteria (map name) (string/join ","))
         query-params (when joined-criteria 
                        (query-string/alist->query-string 
                         [["up" joined-criteria]]))]
     
     (uri/map->string (assoc uri :query query-params)))))

(defn posts-query-params->map
  [{:keys [from-post-id]}]
  {:from-post-id (some-> from-post-id UUID/fromString)})

(defn post-query-params->map
  [{:keys [up]}]
  {:up (set (map keyword (some-> up (string/split #","))))})

(defn what-it-is-path
  ([]
   "/what-it-is")
  ([which-what]
   (-> (uri/uri "/what-it-is")
       (assoc :query (query-string/alist->query-string 
                      [["which-what" which-what]]))
       uri/map->string)))

(defn what-it-is-query-params->map
  [{:keys [which-what]}]
  {:which-what (keyword which-what)})
