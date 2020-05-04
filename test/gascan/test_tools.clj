(ns gascan.test-tools
  (:require [clojure.string :refer [join]] 
            [gascan.multimarkdown :refer [parse-multimarkdown-flat 
                                          make-options]]
))

(def project-folder (System/getProperty "user.dir"))

(def samples-folder (join "/" [project-folder "samples"]))

(defn sample-path
  [relpath]
  (join "/" [samples-folder relpath]))

(defn test-case
  [relpath]
  (let [absolute-path (sample-path relpath)
        parsed-contents (parse-multimarkdown-flat absolute-path)]
    {:path absolute-path
     :contents parsed-contents
     :raw-contents (clojure.string/split (slurp absolute-path) #"\n")}))

(def test-case-basic (test-case "Basic Test.md"))
(def test-case-image (test-case "Image Test.md/Image Test.md"))
(def test-case-no-header-space (test-case "Basic Test No Header Space.md"))
(def test-case-image-inline (test-case "Image Test Inline.md/Image Test Inline.md"))

