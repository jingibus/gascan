(ns gascan.site
  (:require [clojure.string :as string]))

(defn all-posts
  []
  ((resolve 'gascan.posts/posts)))

(defn visible-to-session?
  [session post]
  (if (:public session)
    (boolean (#{:published} (:status post)))
    true))

(defn- update-if
  [m pred k f & xs]
  (if (pred m)
    (apply update (concat [m k f] xs))
    m))

(defn to-kebab-case
  [s]
  (let [pieces (some-> s
                       string/lower-case
                       string/trim
                       (string/split #" +"))]
    (when pieces
      (string/join "-" pieces))))

(defn title->locator-string
  [s]
  (string/replace (to-kebab-case s) #"[?/=&,]" ""))

(defn locator-matcher
  [locator]
  (let [locator (into {} (filter #(second %) locator))
        canonicalize (fn [post]
                       (-> post
                           (update-if :title :title title->locator-string)
                           (update-if :id :id string/lower-case)))]
    (fn [post]
      (= (select-keys (canonicalize post) (keys locator))
         (canonicalize locator)))))

(defn find-posts
  [locator]
  (->> (all-posts)
       (filter (locator-matcher locator))))

(defn find-post
  [locator]
  (first (find-posts locator)))

(defn criteria->set
  [criteria]
  (cond
    (string? criteria) (into #{} (map keyword (string/split criteria #"\.")))
    (nil? criteria) #{}
    :else criteria))

(defn matches-criteria?
  [criteria post]
  (let [criteria (criteria->set criteria)]
    (or (empty? criteria)
        (seq (clojure.set/intersection criteria (:filter post))))))

(defn visible-posts
  [session]
  (filter #(visible-to-session? session %) (all-posts)))

(defn visible-posts-by-criteria
  [session criteria]
  (filter #(matches-criteria? criteria %) (visible-posts session)))

(defn newest-visible-post-by-criteria
  [session criteria]
  (->> (visible-posts-by-criteria session criteria)
       (sort-by (comp - :timestamp))
       first))
