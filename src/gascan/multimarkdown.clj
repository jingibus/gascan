(ns gascan.multimarkdown
  (:refer-clojure)
  (:import [com.vladsch.flexmark.parser Parser ParserEmulationProfile]
           [com.vladsch.flexmark.util.data MutableDataSet])
  ;(require clojure.reflect)
  (:gen-class))

(defn parse-multimarkdown-flat
  [filepath]
  (let [file-contents (slurp filepath)
        options (-> (new MutableDataSet)
                    (.setFrom ParserEmulationProfile/MULTI_MARKDOWN))]
    (-> (Parser/builder options)
        (.build)
        (.parse file-contents))))

(defn flatten-iterator
  [iterator]
  (let [iterator (if (instance? java.lang.Iterable iterator)
                   (.iterator iterator)
                   iterator)]
    (loop [values []]
      (if (.hasNext iterator)
        (recur (conj values (.next iterator)))
        values))))

(defn get-title
  [document]
  (let [child-iterator (-> document (.getChildren) (.iterator))
        title-page (if (.hasNext child-iterator) (.next child-iterator))]
    (if title-page
      (let 
          ;; Should extract something like "Title: Blog Project  \n"
          [title-line (-> title-page
                          (.getContentLines)
                          first
                          (.toString))
           title-text (-> title-line
                          (clojure.string/replace-first "Title:" "")
                          clojure.string/trim)]
        title-text))))

(defn class-hierarchy
  ([class-instance]
   (class-hierarchy class-instance #{}))
  ([class-instance traversed]
   (if (traversed class-instance)
     #{}
     (let [traversed (conj traversed class-instance)
           subsequent (apply clojure.set/union 
                             (map #(class-hierarchy % traversed)
                                  (bases class-instance)))]
       (conj subsequent class-instance)))))

(defn all-methods
  [instance]
  (let [all-classes (class-hierarchy (class instance))
        total-reflection (apply merge-with 
                                clojure.set/union 
                                (map reflect all-classes))] 
    (->> total-reflection 
         :members
         (filter :return-type)
         (map :name)
         sort)))


