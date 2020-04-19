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


    (let [mp3-input "
Here's a [song](audio.mp3) that I wrote."
          wav-input "
[Here](audio.wav) it is in another format."]
      (let [[mp3-output wav-output] (map #(rerender-with-transform 
                                           %
                                           add-inline-audio)
                                         [mp3-input wav-input])]
        (testing (str "mp3: \n" mp3-input "\n -> \n" mp3-output)
          (testing "Includes audio link"
            (is (re-find #"<audio controls>" mp3-output)))
          (testing "It's after the end of the link"
            (is (re-find #"</a> *<audio" mp3-output))))
        (testing wav-output
          (testing "Includes audio link"
            (is (re-find #"<audio controls>" wav-output)))
          (testing "It's after the end of the link"
            (is (re-find #"</a> *<audio" wav-output))))))))
