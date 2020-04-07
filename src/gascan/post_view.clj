(ns gascan.post-view
  (:require [clojure.string :as string]
            [gascan.ast :as ast]
            [gascan.ast :as ast]
            [gascan.intern :as intern]
            [gascan.multimarkdown :as mm]
            [gascan.multimarkdown :as mm]
            [gascan.posts :as posts]
            [gascan.session :as session]
            [gascan.template :as tmpl])
  (:use [gascan.debug]))

(defn to-kebab-case
  [s]
  (let [pieces (some-> s
                       string/lower-case
                       string/trim
                       (string/split #" +"))]
    (when pieces 
      (string/join "-" pieces))))

(defn find-post
  [locator]
  (letfn [(route-entitled [post]
            (update-in post [:title] to-kebab-case))
          (normalized-id [post]
            (if (:id post)
              (update-in post [:id] #(when % (string/lower-case %)))
              post))
          (matches [post]
            (let [matching-post (normalized-id (route-entitled post))]
              (= (select-keys matching-post (keys locator))
                 (normalized-id locator))))]
    (first  
     (->> (posts/posts)
          (filter matches)))))

(defn render-markdown
  [relpath]
  (let [md-contents (some-> (intern/readable-file relpath) slurp)
        massaged-mm (some-> md-contents
                            mm/parse-multimarkdown-str
                            ast/build-scaffold-ast
                            ast/split-line-breaks
                            ast/restitch-scaffold-ast)]
    (when md-contents
      (mm/render-multimarkdown massaged-mm))))

(defn post->title-path
  [post]
  (str "/posts/title/" (to-kebab-case (:title post))))

(defn post-by-title-path
  [title]
  (str "/posts/title/" title))

(defn post-by-id
  [id]
  (str "/posts/id/" id))

(defn post-view
  [sess {:keys [id title] :as all}]
  (let [non-null-args (into {} (filter #(second %) all))
        {title             :title
         timestamp         :timestamp
         path              :markdown-rel-path
         :as post} 
        (find-post non-null-args)
        visible? (posts/visible-to-session? sess post) 
        rendered (and visible? (render-markdown path))
        ]
    (when rendered
      (tmpl/enframe title rendered))))

(comment
  (do
    (use '[clojure.test])

    (defn example
      [desc f & args]
      (println desc "args:" args "\n\tresult: " (apply f args)))

    (testing "valid post yields some sort of HTML filled with <p>s"
      (let [number-of-paras (some-> (post-view session/private-session {:title "blog-project"}) 
                                    (string/split #"<p>")
                                    count)]
        (is (> number-of-paras 5))))
    (testing "invalid post yields nil"
      (is (nil? (view-post {:title "not-there"}))))
    nil))

(comment
  (view-post {:title "blog-project"})
  (find-post {:title "blog-project"})
  (flatten (seq) {1 2})
  (posts/posts)
  (post->title-path (first (posts/posts)))
  (update-in {:title "blog-project"} [:id] identity)
  (gascan.browser/look-at (post->title-path (first (posts/posts))))
  )
