(ns gascan.posts
  (:require
   [clojure.spec.alpha :as s]
   [clojure.zip :as z]
   [gascan.ast :as ast]
   [gascan.intern :as intern :refer [read-edn intern-file! readable-file]]
   [gascan.multimarkdown :as mm :refer [parse-readable]]
   [gascan.post-spec :as post-spec]
   [gascan.remote-posts :as remote]
   [gascan.site :as site]
   [java-time :refer [local-date-time instant]]
   [clojure.string :as string]
   [gascan.intern :as intern]
   [clojure.java.io :as io])
  (:use [gascan.debug])
  (:import [java.util.regex Pattern]
           ))

(def toplevel-post-contents-folder "posts")
(def ^:private post-metadata-edn "metadata.edn")

(defn- spec-error-data
  [spec value]
  (with-out-str
    (s/explain spec value)))

(defn- validate-post!
  [spec post context]
  (when-not (s/valid? spec post)
    (throw (ex-info (str "Invalid post metadata while " context)
                    {:context context
                     :post post
                     :explain (spec-error-data spec post)})))
  post)

(defn- validate-posts!
  [spec posts context]
  (doall
   (map-indexed
    (fn [idx post]
      (validate-post! spec post (str context " (entry " idx ")")))
    posts))
  posts)

(defn fetch-posts
  []
  (let [loaded-posts (or (read-edn {} post-metadata-edn) [])]
    (validate-posts! post-spec/persisted-intern-post
                     loaded-posts
                     (str "reading " post-metadata-edn))))

(s/fdef fetch-posts
  :ret (s/every post-spec/persisted-intern-post))

(defn reset-posts
  []
  (def posts-lazy (lazy-seq (list (fetch-posts)))))

(reset-posts)

(defn posts
  []
  (first posts-lazy))

(defn put-posts!
  [posts]
  (validate-posts! post-spec/persisted-intern-post
                   posts
                   (str "writing " post-metadata-edn))
  (intern/intern-edn! post-metadata-edn posts)
  (reset-posts))

(s/fdef put-posts!
  :args (s/cat :posts (s/every post-spec/persisted-intern-post)))

(defn to-yyyy-mm-dd-mmmm
  [timestamp]
  (let [date-time (local-date-time (instant timestamp) "America/Los_Angeles")
        minute (.getMinute date-time)
        hour (.getHour date-time)
        day-of-month (.getDayOfMonth date-time)
        month (.getMonthValue date-time)
        year (.getYear date-time)]
    (format "%04d/%02d/%02d/%02d%02d" year month day-of-month hour minute)))

(defn strip-title-section
  [[tags scaffold-ast]]
  [tags (-> scaffold-ast
            z/vector-zip
            z/down z/right 
            z/remove
            z/root)])

(s/fdef strip-title-section
  :args (s/cat :tagged-scaffold (s/cat :tags map? :scaffold vector?))
  :ret (s/cat :tags map? :scaffold-ast vector?))

(defn transformed-link
  [link-node url transformed-url]
  (let [wrap-parens #(str "(" % ")")
        original-chars (-> link-node .getChars .toString)
        url-pattern (re-pattern (str (Pattern/quote 
                                      (wrap-parens url))
                                     "$"))
        new-chars (-> original-chars
                      (string/replace url-pattern (wrap-parens transformed-url)))
        new-node (ast/link (-> link-node .getText .toString) transformed-url)
        new-text (ast/char-sequence (str "[" (-> link-node .getText .toString) "]"))
        old-text (-> link-node .getText .toString)
        field-names ["url" "urlOpeningMarker" "pageRef" "anchorMarker" 
                     "anchorRef" "urlClosingMarker" "titleOpeningMarker"
                     "title" "titleClosingMarker"]]
    ;(.setUrl new-node (ast/char-sequence transformed-url))
    ;(.setTextChars new-node new-text)
    new-node))

(defn translate-links!
  [[tags scaffold-ast] f]
  (monitor->> 
   "translated links result: "
   
   (loop [loc (z/vector-zip scaffold-ast)
          translations {}]
     (cond (z/end? loc)
           [(assoc tags :link-translations translations) (z/root loc)]
           (instance? com.vladsch.flexmark.ast.Link (z/node loc))
           (let [node (z/node loc)
                 url (.toString (.getUrl node))
                 transformed-url (f url)
                 new-link #(transformed-link node url transformed-url)]
             (if (= transformed-url url)
               (recur (z/next loc) translations)
               (recur (-> loc (z/replace (new-link)) z/next)
                      (assoc translations url transformed-url))))
           :else
           (recur (z/next loc) translations)))))

(s/fdef translate-links!
  :args (s/cat :tagged-scaffold (s/cat :tags map? :scaffold vector?) :translator fn?)
  :ret (s/cat :tags (s/keys :req-un [::link-translations])
              :scaffold-ast vector?))

(comment
  (def test-title "Basic Test")
  (defn test-ast 
    []
    (-> (find-post {:title test-title})
        :markdown-rel-path
        intern/readable-file
        slurp
        (monitor-> "raw md")
        mm/parse-str
        ast/build-scaffold-ast))
  (-> (test-ast)
      ast/scaffold->tagged-scaffold
      (translate-links! 
       (fn [path]
         (if (.startsWith path "file://")
           (last (string/split path #"/"))
           path)))
      clojure.pprint/pprint)
  (defn find-node
    [pred ast]
    (loop [loc (z/vector-zip ast)]
      (cond (z/end? loc) nil
            (pred (z/node loc)) (z/node loc)
            :else (recur (z/next loc)))))
  (as-> (test-ast) x
       (find-node (partial instance? com.vladsch.flexmark.ast.Link) x)
       (.getChars x)
       (.toString x)
       )
  (with-verbose 
    (-> (remote/read-from-samples-md test-title)
        (remote-post->processed-tagged-scaffold)))
  )

(defn valid-against-spec? [spec args]
  (if-not (s/valid? spec args)
    (s/explain spec args)
    true))

(defn remote-post->processed-tagged-scaffold
  [remote-post]
  (let [map-file-url-to-relative-url 
        (fn [path]
          (if (.startsWith path "file://")
            (last (string/split path #"/"))
            path))]
    (-> remote-post
        :parsed-markdown
        (monitorv-> "rendered source:" mm/render-markdown)
        ast/build-scaffold-ast
        (monitorv-> "source AST:" ast/stringify)
        ast/scaffold->tagged-scaffold
        strip-title-section
        (translate-links! map-file-url-to-relative-url)
        (monitorv-> "processed AST:" (comp ast/stringify second)))))

(let [args-spec (s/or :no-filters (s/cat :remote-post post-spec/remote-post)
                      :with-filters (s/cat :remote-post post-spec/remote-post 
                             :filters :post-spec/filters))]
  (defn import-post!
    "Imports a remote-post into an intern-post. Note that this mutates the parsed Markdown in the remote-post."
    ([remote-post]
     (import-post! remote-post #{}))
    ([remote-post filters]
     (when (valid-against-spec? args-spec [remote-post])
       (let [{title :title 
              timestamp :timestamp
              parsed-markdown :parsed-markdown
              markdown-abs-path :markdown-abs-path
              extra-resources :extra-resources
              directory-post? :directory-post?
              src-path :src-path} remote-post
             timestamp-subfolders (to-yyyy-mm-dd-mmmm timestamp)
             [tags scaffold-ast] (remote-post->processed-tagged-scaffold remote-post)
             other-files-to-import (->> tags 
                                        :link-translations 
                                        keys
                                        (map (comp io/as-file io/as-url)))
             rendered-markdown (-> scaffold-ast
                                   (monitorv-> "processed AST: " ast/stringify)
                                   ast/restitch-scaffold-ast
                                   mm/render-markdown
                                   (monitorv-> "rendered md:"))
             post-contents-folder (string/join "/" [toplevel-post-contents-folder 
                                                    timestamp-subfolders])
             intern-copied-file! #(intern-file! % post-contents-folder directory-post?)
             interned-resources (map intern-copied-file! (concat extra-resources
                                                                 other-files-to-import))
             interned-markdown-path (intern-file! markdown-abs-path 
                                                  post-contents-folder 
                                                  directory-post? 
                                                  rendered-markdown)]
         {
          :title title
          :timestamp timestamp
          :markdown-rel-path interned-markdown-path
          :extra-resources-rel interned-resources
          :id (java.util.UUID/randomUUID)
          :status :published
          :filter #{}
          :src-path src-path
          }
         ))))

  (s/fdef import-post!
    :args args-spec
    :ret  post-spec/intern-post))

(defn update-if
  "Updates only if pred is truthy."
  [m pred k f & xs]
  (if (pred m)
    (apply update (concat [m k f] xs))
    m))

(defn assoc-if
  "Assocs only if pred is truthy."
  [m pred k v & xs]
  (if (pred m)
    (apply assoc (concat [m k v] xs))
    m))

(defn update-posts-check
  "Works like update-posts, but only yields the posts modified."
  [locator & xs]
  (map #(apply update (cons % xs)) (site/find-posts locator)))

(defn assoc-posts-check
  "Works like assoc-posts, but only yields the posts modified."
  [locator & xs]
  (let [updated-posts (map #(apply assoc (cons % xs)) (site/find-posts locator))
        check-post (fn [post] (when-not (s/valid? post-spec/intern-post post)
                                (println "Invalid post")
                                (s/explain post-spec/intern-post post)))]
    (doall (map check-post updated-posts))
    updated-posts))

(defn assoc-posts
  "For posts matching locator, applies the function to the given key value."
  [locator k f & xs]
  (let [matcher (site/locator-matcher locator)]
    (map #(apply assoc-if % (concat [matcher k f] xs)) 
         (site/all-posts))))

(defn assoc-posts!
  [& xs]
  (put-posts! (apply assoc-posts xs)))

(defn update-posts
  "For posts matching locator, applies the function to the given key value."
  [locator k f & xs]
  (let [matcher (site/locator-matcher locator)]
    (map #(apply update-if % (concat [matcher k f] xs)) 
         (site/all-posts))))

(defn update-posts!
  [& xs]
  (put-posts! (apply update-posts xs)))

(defn clean-posts-folder!
  []
  (let [files (fn [] (-> toplevel-post-contents-folder
                         io/resource
                         io/as-file
                         file-seq))
        clean (fn [f]
                (when (and (.exists f)
                           (.isDirectory f)
                           (empty? (.list f)))
                  (println "cleaning" f)
                  (.delete f)))]
    (while 
        (some identity (map clean (files))))))

(defn remove-posts!
  [locator]
  (let [matcher (site/locator-matcher locator)
        posts (site/all-posts)
        posts-to-remove (filter matcher posts)
        remaining-posts (filter (complement matcher) posts)
        post-resources (fn [post]
                         (conj (:extra-resources-rel post)
                               (:markdown-rel-path post)))
        all-resources (->> posts-to-remove
                           (map post-resources)
                           flatten)]
    (println "removing posts" locator)
    (doall (map intern/delete-file all-resources))
    (clean-posts-folder!)
    (put-posts! remaining-posts)
    ))

(defn import-and-add-post!
  [remote-post]
  (let [interned-post (import-post! remote-post)]
    (put-posts! (conj (site/all-posts) interned-post))))

(defn publish-post
  [post]
  (-> post
      (assoc :timestamp (System/currentTimeMillis))
      (assoc :status :published)))

(defn publish-posts!
  [locator]
  (let [matcher (site/locator-matcher locator)
        publish-if-matches #(if (matcher %)
                              (publish-post %)
                              %)]
    (->> (site/all-posts)
         (map publish-post)
         put-posts!)))

(defn replace-post-contents 
  [old-post new-post]
  (let [relevant-contents (select-keys new-post 
                                       [:title 
                                        :markdown-rel-path 
                                        :extra-resources-rel])]
    (conj old-post relevant-contents)))

(defn refresh-post!
  [locator]
  (let [{src-path :src-path id :id :as old-post} (site/find-post locator)]
    (when src-path
      (let [new-remote-post (remote/read-remote-post src-path)]
        (when new-remote-post
          (remove-posts! {:id id})
          (let [imported-new-post (import-post! new-remote-post)]
            (put-posts! 
             (conj (site/all-posts) 
                   (replace-post-contents old-post imported-new-post)))))))))

(defn as-parsed
  "Yields an interned record with parsed markdown."
  [interned-record]
  (if (:parsed-markdown interned-record)
    interned-record
    (let [relpath (:markdown-rel-path interned-record)
          readable-markdown (readable-file relpath)]
      (assoc interned-record 
             :parsed-markdown (parse-readable readable-markdown)))))

(comment
  ;; Oh god, have you forgotten everything?
  ;; M-x cider-jack-in
  ;; https://docs.cider.mx/cider/usage/cider_mode.html
  ;; Here's how to import a post from the Documents folder:
  (import-and-add-post! 
   (remote/read-from-documents-md "Sample Projects.md"))

  ;; Here's how to refresh it once it's been tweaked.
  (refresh-post! {:title "sample projects"})

  ;; Update check; this will validate the change, 
  ;; use assoc-posts! to commit it
  (assoc-posts-check {:title "The Problem of Technical Society"} 
                     :status :published 
                     :filter #{:technical})

  ;; And here's how to validate that it's up.
  ;; Check and make sure that your chromedriver is up-to-date!
  (gascan.browser/look-at "/")
  )
