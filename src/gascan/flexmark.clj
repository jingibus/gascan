(ns gascan.flexmark
  (:import [com.vladsch.flexmark.ast Paragraph Reference]
           [com.vladsch.flexmark.formatter Formatter]
           [com.vladsch.flexmark.html HtmlRenderer]
           [com.vladsch.flexmark.parser Parser ParserEmulationProfile]
           [com.vladsch.flexmark.util.data MutableDataSet])
  (:require [clojure.java.io :refer [as-file]]
            [gascan.ast :as ast])
  (:use [gascan.debug]))

(defn make-options
  [& options-list]
  (let [options (new MutableDataSet)]
    (loop [[key value & options-list] options-list]
      (.set options key value)
      (if options-list
        (recur options-list)
        options))))

(defn parse-str
  "Parses a string into a parsed flexmark object"
  [options file-contents]
  (-> (Parser/builder options)
      (.build)
      (.parse file-contents)))

(defn parse-readable
  "Parses a valid input to reader into a parsed flexmark object"
  [options readable]
  (parse-str options (slurp readable)))

(defn render-html
  ([options flexmark-document]
   (-> (HtmlRenderer/builder options)
       (.build)
       (.render flexmark-document)))
  ([flexmark-document]
   (-> (HtmlRenderer/builder)
       (.build)
       (.render flexmark-document))))

(defn render-markdown
  [options document]
  (let [renderer (-> (Formatter/builder options) (.build))]
    (.render renderer document)))

(defn para
  []
  (Paragraph.))

(defn reference
" [ScreenShot2020-03-30at110216PM]: ScreenShot2020-03-30at110216PM.png"
  [reference-str url-str]
  (let [reference-prefix (str "[" reference-str "]: ")
        complete-fake-char-seq (ast/char-sequence 
                                (str reference-prefix url-str))
        label (.subSequence complete-fake-char-seq 
                            0 
                            (+ 3 (count reference-str)))
        url (.subSequence complete-fake-char-seq 
                          (count reference-prefix) 
                          (+ (count reference-prefix) (count url-str)))]
    (Reference.
     label
     url
     nil)))
