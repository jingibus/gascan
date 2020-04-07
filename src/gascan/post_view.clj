(ns gascan.post-view
  (:require [clojure.string :as string]
            [gascan.ast :as ast]
            [gascan.ast :as ast]
            [gascan.intern :as intern]
            [gascan.multimarkdown :as mm]
            [gascan.multimarkdown :as mm]
            [gascan.posts :as posts]
            [gascan.session :as session]
            [gascan.template :as tmpl]
            [clojure.zip :as z])
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
  (let [locator (into {} (filter #(second %) all))]
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
            (filter matches))))))

(defn link-entry
  [loc]
  (let [paragraph? (partial instance? com.vladsch.flexmark.ast.Paragraph)
        linkref? (partial instance? com.vladsch.flexmark.ast.LinkRef)
        text? (partial instance? com.vladsch.flexmark.ast.Text)
        ]
    (when (and (some-> loc z/down z/node paragraph?)
               ;(do (println "found a para!") true)
               (some-> loc z/down z/right z/down z/node linkref?)
               ;(do (println "found a linkref!") true)
               (some-> loc z/down z/right z/right z/node text?))
      (let [reference (some-> loc 
                              z/down z/right z/down z/node 
                              .getReference
                              .toString)
            link-string (some-> loc z/down z/right z/right z/node .getChars .toString)
            link (some-> link-string
                         (clojure.string/replace #"^: " "")
                         (clojure.string/replace #" width=.*" ""))]
        {reference link}))))

(defn extract-image-map
  [[tags scaffold-ast]]
  (loop [image-links {} 
         loc (z/vector-zip scaffold-ast)]
    (if (z/end? loc) [(assoc tags :image-map image-links) (z/root loc)]
        (let [curr-entry (link-entry loc)]
          (if curr-entry 
            (recur (into image-links curr-entry) (z/remove loc))
            (recur image-links (z/next loc)))))))

(defn new-image-link
  [referent]
  (new com.vladsch.flexmark.ast.HtmlInline 
       (com.vladsch.flexmark.util.sequence.CharSubSequence/of 
        (str "<img src=\"" referent "\"/>"))))

(defn apply-image-map
  [[tags scaffold-ast]]
  (let [image-map (:image-map tags)]
    (loop [loc (z/vector-zip scaffold-ast)]
      (cond (z/end? loc) [tags (z/root loc)]
            (instance? com.vladsch.flexmark.ast.ImageRef (z/node loc))
            (let [reference (some-> loc z/node .getReference .toString)
                  referent (image-map reference)]
              (if referent
                (recur (-> loc 
                           (z/replace (new-image-link referent))
                           z/next))
                (recur (z/next loc))))
            :else
            (recur (z/next loc))))))

(comment
  (->> (test-ast) 
       ast/scaffold->tagged-scaffold
       extract-image-map
       apply-image-map
       clojure.pprint/pprint)
  (->> (test-ast) 
       ast/scaffold->tagged-scaffold
       extract-image-map
       first
       :image-map
       clojure.pprint/pprint)
  (->> (test-ast) 
       ast/scaffold->tagged-scaffold
       extract-image-map
       first
       :image-map
       vals
       first
       ;show-methods
       clojure.pprint/pprint)
  (->> (test-ast)
       transform-ast
       ast/stringify
       clojure.pprint/pprint)
)
(defn transform-ast
  [ast]
  (-> ast 
      ast/split-line-breaks
      ast/scaffold->tagged-scaffold
      extract-image-map
      apply-image-map
      ast/tagged-scaffold->scaffold))

(defn render-markdown
  [relpath]
  (let [md-contents (some-> (intern/readable-file relpath) slurp)
        massaged-mm (some-> md-contents
                            mm/parse-multimarkdown-str
                            ast/build-scaffold-ast
                            transform-ast
                            ast/restitch-scaffold-ast)]
    (when md-contents
      (mm/render-multimarkdown massaged-mm))))

(comment
  (def image-test (first (filter #(= (:title %) "Image Test") (posts/fetch-posts))))
  (defn test-ast 
    []
    (-> (first (filter #(= (:title %) "Image Test") (posts/fetch-posts)))
        :markdown-rel-path
        intern/readable-file
        slurp
        mm/parse-multimarkdown-str
        ast/build-scaffold-ast))
  (defn find-node
    [pred ast]
    (loop [loc (z/vector-zip ast)]
      (cond (z/end? loc) nil
            (pred (z/node loc)) (z/node loc)
            :else (recur (z/next loc)))))

  (->  (find-node (partial instance? com.vladsch.flexmark.ast.LinkRef) (test-ast))
       .getReference)
  (->>  (find-node (partial instance? com.vladsch.flexmark.ast.LinkRef) (test-ast))
        .getClass
        .getMethods
        (sort-by #(.getName %))
        (map str)
        (map #(clojure.string/replace % #"com.vladsch.flexmark.(util.|)ast." ""))
      clojure.pprint/pprint)

  (->> (test-ast)
    transform-ast
    (ast/deep-map-vec #(some-> % type .getName))
    clojure.pprint/pprint)
  
  

  )

(defn post->title-path
  [post]
  (str "/posts/title/" (to-kebab-case (:title post))))

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

(defn post-resources-view
  [sess {:keys [id title] :as locator} res-name]
  (let [{resources :extra-resources-rel
         :as post} (find-post locator)]
    (when post
      (some->> resources
               (filter #(= res-name (last (clojure.string/split % #"/"))))
               first
               gascan.intern/readable-file))))

(defn post-view
  [sess {:keys [id title] :as locator}]
  (let [
        {title             :title
         timestamp         :timestamp
         path              :markdown-rel-path
         :as post} 
        (find-post locator)
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
  (post-view session/private-session {:title "blog-project"})
  (clojure.pprint/pprint (post-view session/private-session {:title "image-test"}))
  (find-post {:title "blog-project"})
  (flatten (seq) {1 2})
  (posts/posts)
  (post->title-path (first (posts/posts)))
  (update-in {:title "blog-project"} [:id] identity)
  (gascan.browser/look-at (post->title-path (first (posts/posts))))
  )
