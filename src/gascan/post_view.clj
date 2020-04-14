(ns gascan.post-view
  (:require [clojure.string :as string]
            [clojure.string :as string]
            [clojure.zip :as z]
            [gascan.ast :as ast]
            [gascan.ast :as ast]
            [gascan.intern :as intern]
            [gascan.multimarkdown :as mm]
            [gascan.multimarkdown :as mm]
            [gascan.posts :as posts]
            [gascan.session :as session]
            [gascan.template :as tmpl]
            [org.bovinegenius.exploding-fish :as uri]
            [org.bovinegenius.exploding-fish.query-string :as query-string]
            [gascan.view-common :as view-common]
            [gascan.routing :as routing])
  (:use [gascan.debug]))

(defn link-entry
  [loc]
  (let [paragraph? (partial instance? com.vladsch.flexmark.ast.Paragraph)
        linkref? (partial instance? com.vladsch.flexmark.ast.LinkRef)
        text? (partial instance? com.vladsch.flexmark.ast.Text)
        zip-to-linkref #(some-> % z/down z/right z/down z/node)
        zip-to-linktext #(some-> % z/down z/right z/right z/node)
        ]
    (when (and (some-> loc z/down z/node paragraph?)
               (some-> loc zip-to-linkref linkref?)
               (some-> loc zip-to-linktext text?))
      (let [reference (some-> loc 
                              zip-to-linkref
                              .getReference
                              .toString)
            link-string (some-> loc 
                                zip-to-linktext
                                .getChars .toString)
            link (some-> link-string
                         (string/replace #"^: " "")
                         (string/replace #" width=.*" ""))
            width-and-height (some-> link-string
                                     (string/replace #"^.*? (width=|height=)"
                                                     "$1")
                                     string/trim)]
        {reference {:link link :attrs width-and-height}}))))

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
  [{:keys [link attrs]}]
  (new com.vladsch.flexmark.ast.HtmlInline 
       (com.vladsch.flexmark.util.sequence.CharSubSequence/of 
        (str "<img src=\"" link "\" " attrs " />"))))

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
  (->> (test-ast)
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
  (defn def-test-case
    [title]
    (def test-case-title title))
  (def-test-case "PDF Link Test")

  (def image-test (first (filter #(= (:title %) "Image Test") (posts/fetch-posts))))
  (defn test-ast 
    []
    (-> (posts/find-post {:title test-case-title})
        :markdown-rel-path
        intern/readable-file
        slurp
        (monitor-> "raw md:")
        mm/parse-multimarkdown-str
        ast/build-scaffold-ast))
  (defn find-node
    [pred ast]
    (loop [loc (z/vector-zip ast)]
      (cond (z/end? loc) nil
            (pred (z/node loc)) (z/node loc)
            :else (recur (z/next loc)))))

  (some->  (find-node (partial instance? com.vladsch.flexmark.ast.LinkRef) (test-ast))
           .getReference)
  (some->>  (find-node (partial instance? com.vladsch.flexmark.ast.LinkRef) (test-ast))
            .getClass
            .getMethods
            (sort-by #(.getName %))
            (map str)
            (map #(clojure.string/replace % #"com.vladsch.flexmark.(util.|)ast." ""))
            clojure.pprint/pprint)

  (->> (test-ast)
       (ast/deep-map-vec #(some-> % type .getName))
       clojure.pprint/pprint)

  (->> (test-ast)
       transform-ast
       (ast/deep-map-vec #(some-> % type .getName))
       clojure.pprint/pprint)

  (->> (test-ast)
       ast/stringify
       clojure.pprint/pprint)
  )

(defn post-resources-view
  [sess {:keys [id title] :as locator} res-name]
  (let [{resources :extra-resources-rel
         :as post} (posts/find-post locator)]
    (when post
      (some->> resources
               (filter #(= res-name (last (clojure.string/split % #"/"))))
               first
               gascan.intern/readable-file))))

(defn post-view
  [sess locator & {:keys [query-params]}]
  (let [
        {title             :title
         timestamp         :timestamp
         path              :markdown-rel-path
         status            :status
         :as post} 
        (posts/find-post locator)
        {up-criteria :up} (routing/post-query-params->map query-params)
        visible? (posts/visible-to-session? sess post) 
        rendered (and visible? (render-markdown path))
        title-warning (when-not (#{:published} status) 
                        [:font {:color "red"} " (DRAFT)"])
        up-target (routing/posts-by-date-path up-criteria)
        ]
    (when rendered
      (tmpl/enframe (list title title-warning) rendered
                    :up-link (view-common/up-link up-target)))))

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
      (is (nil? (post-view session/private-session {:title "not-there"}))))
    nil))

(comment
  (post-view session/private-session {:title "blog-project"})
  (clojure.pprint/pprint (post-view session/private-session {:title "image-test"}))
  (posts/find-post {:title "blog-project"})
  (flatten (seq) {1 2})
  (posts/posts)
  (post->title-path (first (posts/posts)) #{:meta :technical})
  (update-in {:title "blog-project"} [:id] identity)
  (gascan.browser/look-at (post->title-path (first (posts/posts))))
  (gascan.browser/look-at (post->title-path (posts/find-post {:title test-case-title})))
  
  )
