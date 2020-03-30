(ns gascan.posts-view
  (:require [clojure.string :as string]
            [gascan.ast :as ast]
            [gascan.ast :as ast]
            [gascan.intern :as intern]
            [gascan.multimarkdown :as mm]
            [gascan.multimarkdown :as mm]
            [gascan.posts :as posts])
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
    (println "massaged-mm" massaged-mm "md-contents" md-contents)
    (when md-contents
      (mm/render-multimarkdown massaged-mm))))

(defn view-post
  [{:keys [id title] :as all}]
  (let [non-null-args (into {} (filter #(second %) all))
        {title             :title
         timestamp         :timestamp
         path              :markdown-rel-path} 
        (find-post non-null-args)
        ]
    (render-markdown path)))
(comment
  (do
    (use '[clojure.test])

    (defn example
      [desc f & args]
      (println desc "args:" args "\n\tresult: " (apply f args)))

    (testing "valid post yields some sort of HTML filled with <p>s"
      (let [number-of-paras (some-> (view-post {:title "blog-project"}) 
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
  (update-in {:title "blog-project"} [:id] identity))
