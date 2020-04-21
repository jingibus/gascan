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
            [gascan.routing :as routing]
            [hiccup.core :as hc])
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

(defn apply-simple-text-replacements
  [[tags scaffold-ast] f]
  (loop [loc (z/vector-zip scaffold-ast)]
    (cond (z/end? loc) [tags (z/root loc)]
          ; Skip over any code blocks.
          (instance? com.vladsch.flexmark.ast.Code (z/node loc))
          (recur (ast/z-skip-subtree loc))
          (instance? com.vladsch.flexmark.ast.Text (z/node loc))
          (let [original (-> loc z/node .getChars str)
                txformed (f original)]
            (if (= original txformed)
              (recur (z/next loc))
              (do
                (.setChars (z/node loc) (ast/char-sequence txformed))
                (recur (z/next loc)))))
          :else
          (recur (z/next loc)))))

(defn- link-info->htmlinline
  [{text :text url :url location :location}]
  (let [audio-html
        (hc/html [:p {:style (str "display: flex; " 
                                  "align-items: center; "
                                  "flex-wrap: wrap; "
                                  "margin: 10px")}
                  [:p {:style "margin: 10px"}
                   text ":"]
                  [:audio {:controls ""}
                   [:source {:src url}]]])]
    (new com.vladsch.flexmark.ast.HtmlInline
         (ast/char-sequence audio-html))))

(defn- extract-audio-links-para
  [[tags scaffold-para]]
  (let [audio-link?
        (fn [loc]
          (let [possible-link (some->> loc z/down z/node)
                is-audio-file? #(or (.endsWith % ".mp3") (.endsWith % ".wav"))]
            (and (instance? com.vladsch.flexmark.ast.Link possible-link)
                 (-> possible-link .getUrl .toString is-audio-file?))))
        link-info
        (fn [link-loc]
          (let [link-node (-> link-loc z/down z/node)
                url (-> link-node .getUrl str)
                text (-> link-node .getText str)
                ]
            {:url url :text text}))
        loc (z/vector-zip scaffold-para)
        source-para (-> loc z/down z/node)]
    (loop [loc loc
           link-infos []]
      (cond (z/end? loc)
            [(assoc-in tags [:audio-links source-para] link-infos)
             (z/root loc)]
            (audio-link? loc)
            (recur (z/next loc) (conj link-infos (link-info loc)))
            :else
            (recur (z/next loc)
                   link-infos)))))

(defn- all-audio-controls
  [[tags scaffold-ast]]
  (->> tags
       :audio-links
       (map val)
       (mapcat link-info->htmlinline)))

(defn extract-audio-links
  [[tags scaffold-ast]]
  "Adds an :audio-links key that maps from paragraphs to audio controls."
  (let [para? #(instance? com.vladsch.flexmark.ast.Paragraph %)]
    (loop [tags tags
           loc (z/vector-zip scaffold-ast)]
      (cond (z/end? loc) 
            [tags (z/root loc)]
            (para? (some-> loc z/down z/node))
            (let [[tags _]
                  (extract-audio-links-para [tags (z/node loc)])]
              (recur tags (z/next loc)))
            :else
            (recur tags (z/next loc))))))

(defn apply-audio-links
  [[tags scaffold-ast]]
  (let [audio-links (:audio-links tags)
        links-for-para-branch
        (fn [loc]
          (get audio-links (some-> loc z/down z/node)))]
    (if-not (seq audio-links)
      [tags scaffold-ast]
      (loop [loc (z/vector-zip scaffold-ast)]
        (cond (z/end? loc)
              [tags (z/root loc)]
              (links-for-para-branch loc)
              (let [audio-controls
                    (map link-info->htmlinline 
                         (links-for-para-branch loc))]
                (recur (-> loc 
                           (ast/z-insert-rights audio-controls)
                           z/next)))
              :else
              (recur (z/next loc)))
        ))))

(comment
  (->> (subject-ast) 
       ast/scaffold->tagged-scaffold
       extract-image-map
       apply-image-map
       clojure.pprint/pprint)
  (->> (subject-ast) 
       ast/scaffold->tagged-scaffold
       extract-image-map
       first
       :image-map
       clojure.pprint/pprint)
  (->> (subject-ast) 
       ast/scaffold->tagged-scaffold
       extract-image-map
       first
       :image-map
       vals
       first
       ;show-methods
       clojure.pprint/pprint)
  (->> (subject-ast)
       transform-ast
       ast/stringify
       clojure.pprint/pprint)
  (->> (subject-ast)
       ast/stringify
       clojure.pprint/pprint)
  (->> (subject-ast)
       (ast/deep-map-vec type)
       clojure.pprint/pprint)
  (-> (subject-ast)
      (monitor-> "raw ast" ast/stringify)
      ast/scaffold->tagged-scaffold
      (apply-simple-text-replacements #(string/replace % #"---" "\u2014"))
      ast/tagged-scaffold->scaffold
      ast/stringify
      clojure.pprint/pprint)
  (-> (subject-ast)
      (monitor-> "raw ast" ast/stringify)
      ast/scaffold->tagged-scaffold
      ast/tagged-scaffold->scaffold
      ast/restitch-scaffold-ast
      mm/render-html
       )
  (-> (subject-ast)
      (monitor-> "raw ast" ast/stringify)
      ast/scaffold->tagged-scaffold
      ast/tagged-scaffold->scaffold
      ast/stringify
      clojure.pprint/pprint)
)
(defn transform-ast
  [ast]
  (let [text-replacements 
        (fn [s]
          (-> s 
              (string/replace #"---" "\u2014")
              (string/replace #"--" "\u2013")))]
    (-> ast 
        ast/split-line-breaks
        ast/scaffold->tagged-scaffold
        extract-image-map
        extract-audio-links
        apply-image-map
        (apply-simple-text-replacements text-replacements)
        apply-audio-links
        ast/tagged-scaffold->scaffold)))

(defn render-markdown-to-html
  [relpath]
  (let [md-contents (some-> (intern/readable-file relpath) slurp)
        massaged-mm (some-> md-contents
                            mm/parse-multimarkdown-str
                            ast/build-scaffold-ast
                            transform-ast
                            ast/restitch-scaffold-ast)]
    (when md-contents
      (mm/render-html massaged-mm))))

(comment
  (do
    (defn def-subject-title
      [title]
      (def subject-title title))

    (defn def-subject-from-post
      [criteria]
      (def subject
        (-> (posts/find-post criteria)
            :markdown-rel-path
            intern/readable-file
            slurp)))

    (defn subject-ast 
      []
      (let [md-line-filter (monitor->> "md-line-filter" (or (resolve 'test-case-md-filter) identity))
            filter-lines #(->> (string/split % #"\n")
                               (filter md-line-filter) 
                               (string/join "\n"))]
        (-> subject
            filter-lines
            (monitor-> "raw md:")
            mm/parse-multimarkdown-str
            ast/build-scaffold-ast)))

    (defn find-node
      [pred ast]
      (loop [loc (z/vector-zip ast)]
        (cond (z/end? loc) nil
              (pred (z/node loc)) (z/node loc)
              :else (recur (z/next loc))))))

  (some->  (find-node (partial instance? com.vladsch.flexmark.ast.LinkRef) (subject-ast))
           .getReference)
  (some->>  (find-node (partial instance? com.vladsch.flexmark.ast.LinkRef) (subject-ast))
            .getClass
            .getMethods
            (sort-by #(.getName %))
            (map str)
            (map #(clojure.string/replace % #"com.vladsch.flexmark.(util.|)ast." ""))
            clojure.pprint/pprint)

  (->> (subject-ast)
       (ast/deep-map-vec #(some-> % type .getName))
       clojure.pprint/pprint)

  (->> (subject-ast)
       transform-ast
       (ast/deep-map-vec #(some-> % type .getName))
       clojure.pprint/pprint)

  (->> (subject-ast)
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
         id                :id
         :as post} 
        (posts/find-post locator)
        {up-criteria :up} (routing/post-query-params->map query-params)
        visible? (posts/soft-visible-to-session? sess post) 
        rendered (and visible? (render-markdown-to-html path))
        title-warning (when (#{:draft} status) 
                        [:font {:color "red"} " (DRAFT)"])
        up-target (routing/posts-by-date-from-post-id-path id up-criteria)
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
  (gascan.browser/look-at "/")
  (gascan.browser/look-at (post->title-path (first (posts/posts))))
  (gascan.browser/look-at (post->title-path (posts/find-post {:title subject-title})))
  
  )
