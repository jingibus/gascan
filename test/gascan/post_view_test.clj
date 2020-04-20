(ns gascan.post-view-test
  (:require [gascan.post-view :refer :all]
            [gascan.multimarkdown :as mm]
            [clojure.test :refer :all]
            [gascan.ast :as ast]))

(deftest render-transformations
  (let [rerender-with-transform
        (fn [text txform]
          (-> text
              mm/parse-multimarkdown-str
              ast/build-scaffold-ast
              ast/scaffold->tagged-scaffold
              txform
              ast/tagged-scaffold->scaffold
              ast/restitch-scaffold-ast
              mm/render-html)
          )]
    (let [blockquote-md "
> God, I'm asking for forgiveness
> I've been faithless
> 
> You gave me a mission
> and you showed me not to be afraid
"
          blockquoted-output (rerender-with-transform 
                              blockquote-md 
                              ast/split-line-breaks)]
      (testing (str "blockquote: \n" blockquote-md "\n ->\n" blockquoted-output)
        (testing "number of para breaks"
          (is (= 2 (count (re-seq #"<p>" blockquoted-output)))))
        (testing "number of br breaks"
          (is (= 2 (count (re-seq #"<br>" blockquoted-output)))))))


    (let [[mp3-input 
           wav-input 
           other-input
           :as inputs] ["Here's a [song](audio.mp3) that I wrote."
                        "[Here](audio.wav) it is in another format."
                        "[Here](score.pdf) is another thing."]]
      (let [[mp3-output wav-output other-output] 
            (map #(rerender-with-transform % add-inline-audio)
                 inputs)
            type-test
            (fn [name input output]
              (testing (str name ": \n" input "\n -> \n" output)
                (testing "Includes audio link"
                  (is (re-find #"<audio controls>" output)))
                (testing "It comes after a close para"
                  (is (re-find #"(?s)</a>.*</p>.*<audio" output)))))]
        (type-test "mp3" mp3-input mp3-output)
        (type-test "wav" wav-input wav-output)
        
        (testing (str "other: \n " other-input "\n -> \n" other-output)
          (testing "Includes no link"
            (is (not (re-find #"<audio controls>" other-output)))))))))
