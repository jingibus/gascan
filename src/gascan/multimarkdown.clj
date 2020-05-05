(ns gascan.multimarkdown
  (:refer-clojure)
  (:import [com.vladsch.flexmark.formatter Formatter]
           [com.vladsch.flexmark.html HtmlRenderer]
           [com.vladsch.flexmark.parser Parser ParserEmulationProfile]
           [com.vladsch.flexmark.util.data MutableDataSet])
  (:require [clojure.java.io :refer [as-file]]
            [clojure.reflect :refer [reflect]]
            [gascan.flexmark :as fm])
  (:use [gascan.debug])
  (:gen-class))

(def multimarkdown-options
  (-> (fm/make-options Parser/HEADING_NO_ATX_SPACE true)
      (.setFrom ParserEmulationProfile/MULTI_MARKDOWN)))

(def parse-str (partial fm/parse-str multimarkdown-options))

(def parse-readable (partial fm/parse-readable multimarkdown-options))

(defn render-html
  ([options flexmark-document]
   (-> (HtmlRenderer/builder options)
       (.build)
       (.render flexmark-document)))
  ([flexmark-document]
   (-> (HtmlRenderer/builder)
       (.build)
       (.render flexmark-document))))

(def render-markdown (partial fm/render-markdown multimarkdown-options))
