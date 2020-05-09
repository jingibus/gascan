(ns gascan.multi-to-intern-markdown
  (:require [clojure.zip :as zip]
            [gascan.ast :as ast]
            [gascan.intern-markdown :as im]
            [gascan.multimarkdown :as mm]
            [clojure.zip :as z]
            [clojure.string :as string]
            [gascan.flexmark :as fm])
  (:import [com.vladsch.flexmark.ext.attributes 
            AttributeNode AttributesNode])
  (:use [gascan.debug]))

(defn- multimarkdown-attrs-loc->attribute-nodes
  "Converts:
[\"Paragraph{}\" 
 [\"LinkRef{text=, reference=post-creation-workflow}\" 
  \"Text{text=post-creation-workflow}\"] 
 \"Text{text=: post-creation-workflow.jpg width=421px height=329px}\"]

into:
[\"Reference{reference=ScreenShot2020-03-30at110216PM, url=ScreenShot2020-03-30at110216PM.png}\"
 [\"Paragraph{}\"
  [\"AttributesNode{}\" \"AttributeNode{}\" \"AttributeNode{}\"]]]
"
  [loc]
  (let [linkref-node (-> loc z/down z/right z/down z/node)
        urltext-node (-> loc z/down z/right z/right z/node)
        reference-str (-> linkref-node .getChars str (string/replace #"(^\[|\]$)" ""))
        [_ url attrs-raw] (-> urltext-node .getChars str 
                              (string/split #" " 3))
        attrs-alist (->> (string/split attrs-raw #"(=|[ \t\n]+)") (partition 2))
        attr-nodes (map #(apply im/attr-node %) attrs-alist)
        reference (fm/reference reference-str url)]
    (-> loc
        (z/replace reference)
        (z/insert-right 
         [(doto (fm/para) (.setLineIndents (int-array [0])))
          (vec (cons (im/attrs-node) attr-nodes))]))))

(defn multimarkdown->internmarkdown
  [scaffold-ast]
  (let [is-mm-imageref
         (fn [loc]
           (and (some-> loc z/down z/node ast/paragraph?)
                (some-> loc z/down z/right z/down z/node ast/linkref?)
                (some-> loc z/down z/right z/down z/right z/node ast/text?)
                (some-> loc z/down z/right z/right z/node ast/text?)))
        reparse-as-intern-markdown
        (fn [scaffold-ast]
          (-> scaffold-ast
              ast/restitch-scaffold-ast
              im/render-markdown
              im/parse-str))]
    (loop [loc (-> scaffold-ast zip/vector-zip)]
      (cond (z/end? loc)
            (-> loc z/root reparse-as-intern-markdown)
            (is-mm-imageref loc)
            (recur (multimarkdown-attrs-loc->attribute-nodes loc))
            :else
            (recur (z/next loc))))))

(comment
  (require ['gascan.ast :as 'ast])
  (-> "
Paragraph 1.1
![][post-creation-workflow]
Paragraph 1.2
![][ScreenShot2020-03-30at110216PM]

[post-creation-workflow]: post-creation-workflow.jpg width=421px height=329px

[ScreenShot2020-03-30at110216PM]: ScreenShot2020-03-30at110216PM.png width=495px height=227px
"
      mm/parse-str
      ast/build-scaffold-ast
      multimarkdown->internmarkdown
      ast/restitch-scaffold-ast
      im/render-markdown
      im/parse-str
      im/render-html
      print)

  (-> "
Paragraph 1.1
![][post-creation-workflow]
Paragraph 1.2
![][ScreenShot2020-03-30at110216PM]

[post-creation-workflow]: post-creation-workflow.jpg width=421px height=329px

[ScreenShot2020-03-30at110216PM]: ScreenShot2020-03-30at110216PM.png width=495px height=227px
"
      mm/parse-str
      ast/build-scaffold-ast
      multimarkdown->internmarkdown
      ast/restitch-scaffold-ast
      im/render-html
      print)

  (-> "
Paragraph 1.1
![][post-creation-workflow]
Paragraph 1.2
![][ScreenShot2020-03-30at110216PM]

[post-creation-workflow]: post-creation-workflow.jpg width=421px height=329px

[ScreenShot2020-03-30at110216PM]: ScreenShot2020-03-30at110216PM.png width=495px height=227px
"
      mm/parse-str
      ast/build-scaffold-ast
      multimarkdown->internmarkdown
      ast/restitch-scaffold-ast
      ast/build-scaffold-ast)

  (-> "
Paragraph 1.1
![][post-creation-workflow]
Paragraph 1.2
![][ScreenShot2020-03-30at110216PM]

[post-creation-workflow]: post-creation-workflow.jpg width=421px height=329px

[ScreenShot2020-03-30at110216PM]: ScreenShot2020-03-30at110216PM.png width=495px height=227px
"
      mm/parse-str
      ast/build-scaffold-ast
      z/vector-zip
      z/down z/right z/right z/right
      multimarkdown-attrs-loc->attribute-nodes)

  (-> "
Paragraph 1.1
![][post-creation-workflow]
Paragraph 1.2
![][ScreenShot2020-03-30at110216PM]

[post-creation-workflow]: post-creation-workflow.jpg width=421px height=329px

[ScreenShot2020-03-30at110216PM]: ScreenShot2020-03-30at110216PM.png width=495px height=227px
"
      mm/parse-str
      ast/build-scaffold-ast
      z/vector-zip
      z/down z/right z/right z/right z/node
      ast/stringify
      clojure.pprint/pprint
      )
)
