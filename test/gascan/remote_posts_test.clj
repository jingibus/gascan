(ns gascan.remote-posts-test
  (:require 
            [gascan.ast :as ast]
            [gascan.multimarkdown :as mm])
  (:use [clojure.test] [gascan.remote-posts]))

(deftest title-reading
    (let [example-multimarkdown-text "
Title: Blog Project  
Author: Bill Phillips

# Chapter 1

Call me Ishmael."
          ]
      (testing "Title can successfully be read from a multimarkdown example"
        (let [parsed-flexmark (mm/parse-str example-multimarkdown-text)]
          (is (= (get-title parsed-flexmark) "Blog Project"))))))


