(ns gascan.flexmark
  (:import [com.vladsch.flexmark.formatter Formatter]
           [com.vladsch.flexmark.html HtmlRenderer]
           [com.vladsch.flexmark.parser Parser ParserEmulationProfile]
           [com.vladsch.flexmark.util.data MutableDataSet])
  (:require [clojure.java.io :refer [as-file]])
  (:use [gascan.debug]))

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
