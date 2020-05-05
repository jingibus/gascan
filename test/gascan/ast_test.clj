(ns gascan.ast-test
  (:require [clojure.zip :as z]
            [gascan.ast :as sut]
            [gascan.multimarkdown :as mm]
            [gascan.ast :as ast])
  (:use [clojure.test] [gascan.test-tools]))

(deftest scaffold-editing
  (let [testpath (:path test-case-basic)]
    (testing "If you scaffold and restitch, you can get the same scaffold out"
      (let [original-scaffold (-> testpath
                                  mm/parse-readable 
                                  sut/build-scaffold-ast)
            restitched (sut/restitch-scaffold-ast original-scaffold)
            new-scaffold (sut/build-scaffold-ast restitched)]
        (is (= original-scaffold new-scaffold))))
    (testing "The scaffold of an edited scaffold is identical to the edited scaffold"
      (let [testpath (:path test-case-basic)
            original-scaffold (-> testpath
                                  mm/parse-readable
                                  sut/build-scaffold-ast)
            edited-scaffold (-> original-scaffold
                                z/vector-zip
                                z/down
                                z/right
                                z/remove
                                z/root)
            restitched (sut/restitch-scaffold-ast edited-scaffold)
            new-scaffold (sut/build-scaffold-ast restitched)]
        (is (= (ast/stringify edited-scaffold) (ast/stringify new-scaffold)))))
    (testing "Split line breaks yields all paras, no line breaks"
      (let [example-markdown 
            "
Test: test project   
Markdown: markdown `code_span`
\tcode
"
            original-scaffold (-> example-markdown
                                  mm/parse-str
                                  sut/build-scaffold-ast)
            edited-scaffold (sut/split-line-breaks original-scaffold)
            expected-scaffold-stringified 
            ["Document{}" 
             ["Paragraph{}" "Text{text=Test: test project}"] 
             ["Paragraph{}" 
              "Text{text=Markdown: markdown }" 
              ["Code{}" "Text{text=code_span}"]] 
             ["Paragraph{}" "Text{text=code}"]]]
        (is (= (-> edited-scaffold sut/stringify) expected-scaffold-stringified))))))
