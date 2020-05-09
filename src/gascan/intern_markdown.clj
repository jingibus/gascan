(ns gascan.intern-markdown
  (:require [gascan.flexmark :as fm]
            [gascan.ast :as ast]
            [clojure.zip :as z]
            [clojure.pprint :as pprint]
            [clojure.pprint :as pp])
  (:import [com.vladsch.flexmark.ext.attributes AttributesExtension AttributesNode AttributeNode]
           [com.vladsch.flexmark.formatter Formatter]
           [com.vladsch.flexmark.html HtmlRenderer]
           [com.vladsch.flexmark.parser Parser ParserEmulationProfile]
           [com.vladsch.flexmark.util.data MutableDataSet]))

  (-> (fm/make-options Parser/HEADING_NO_ATX_SPACE true)
      (.setFrom ParserEmulationProfile/MULTI_MARKDOWN))

(def intern-markdown-options
  (-> (fm/make-options 
       Parser/EXTENSIONS [(AttributesExtension/create)])
      (.setFrom ParserEmulationProfile/COMMONMARK)))

(def parse-str (partial fm/parse-str intern-markdown-options))

(def parse-readable (partial fm/parse-readable intern-markdown-options))

(def render-markdown (partial fm/render-markdown intern-markdown-options))

(def render-html (partial fm/render-html intern-markdown-options))

(defn attrs-node
  []
  (let [[openingMarker text closingMarker] (map ast/char-sequence ["{" "" "}"])]
    (AttributesNode. openingMarker text closingMarker)))

(defn attr-node
  [name-str value-str]
  (let [[name value blank] (map ast/char-sequence [name-str value-str ""])]
    (AttributeNode.
     name
     (ast/char-sequence "=")    ; attributeSeparator
     blank    ; openingMarker
     value
     blank    ; closingMarker
     )))

(comment
  (do 
    (require ['gascan.ast :as 'ast])
    (require ['clojure.inspector :as 'inspect])
    (require ['clojure.pprint :as 'pp])
    (require ['clojure.zip :as 'z])
    (require ['clojure.reflect :as 'rflct]))
  (-> "
Paragraph 1.1
![][post-creation-workflow]
Paragraph 1.2
![][ScreenShot2020-03-30at110216PM]

[post-creation-workflow]: post-creation-workflow.jpg width=421px height=329px

[ScreenShot2020-03-30at110216PM]: ScreenShot2020-03-30at110216PM.png
{width=495px height=227px}
"
      parse-str
      ast/build-scaffold-ast
      )

  
  (->> "
Paragraph 1.1
![][post-creation-workflow]
Paragraph 1.2
![][ScreenShot2020-03-30at110216PM]

[post-creation-workflow]: post-creation-workflow.jpg width=421px height=329px

[ScreenShot2020-03-30at110216PM]: ScreenShot2020-03-30at110216PM.png
{width=495px height=227px}
"
      parse-str
      ast/build-scaffold-ast
      z/vector-zip
      z/down z/right z/right z/right
      z/node
      rflct/reflect
      :members
      (sort-by :name)
      (filter #(instance? clojure.reflect.Constructor %))
      (map #(select-keys % #{:name :parameter-types}))
      pp/print-table)

(->> "
Paragraph 1.1
![][post-creation-workflow]
Paragraph 1.2
![][ScreenShot2020-03-30at110216PM]

[post-creation-workflow]: post-creation-workflow.jpg width=421px height=329px

[ScreenShot2020-03-30at110216PM]: ScreenShot2020-03-30at110216PM.png
{width=495px height=227px}
"
      parse-str
      ast/build-scaffold-ast
      z/vector-zip
      z/down z/right z/right z/right z/node)

)
