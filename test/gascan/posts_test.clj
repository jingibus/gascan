(ns gascan.posts-test
  (:require [gascan.ast :as ast]
            [gascan.multimarkdown :as mm])
  (:use [gascan.posts] [clojure.test] [gascan.posts]))

(deftest strips-title-section
  (testing "Initial paragraph with title info is stripped out"
    (let [example-markdown 
          "
Test: test project   
Markdown: markdown `code_span`

\tcode
"
          parsed-document (mm/parse-str example-markdown)
          original-scaffold-ast (ast/build-scaffold-ast parsed-document)
          expected-scaffold-stringified ["Document{}" "IndentedCodeBlock{}"]
          ]
      (-> original-scaffold-ast
          ast/scaffold->tagged-scaffold
          strip-title-section
          ast/tagged-scaffold->scaffold
          ast/restitch-scaffold-ast)
      (is (= expected-scaffold-stringified
             (-> parsed-document ast/build-scaffold-ast ast/stringify))))))
