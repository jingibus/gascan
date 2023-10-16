(ns gascan.post-spec
  (:require [clojure.spec.alpha :as s]
            [java-time])
  )

(def cst (java-time/zone-id "America/Chicago"))

(def bills-birthday 
  (java-time/zoned-date-time (java-time/local-date-time 1981 10 7 1) cst))

(def bills-oblivion-guarantee-day
  (java-time/plus bills-birthday (java-time/years 200)))

(defn can-be-converted-to-instant?
  [n]
  (try 
    (java-time/instant n)
    (catch clojure.lang.ExceptionInfo e
      nil)))

(defn is-semi-plausibly-within-bills-lifespan?
  [n]
  (apply java-time/before? 
         (map java-time/instant 
              [bills-birthday n bills-oblivion-guarantee-day])))

(defn valid-resource-path?
  [s]
  (some-> (clojure.java.io/resource s)
          .toURI
          (clojure.java.io/file)
          .exists))

(defn markdown-filename?
  [s]
  (.endsWith s ".md"))

(defn valid-file?
  [filey-thing]
  (try (some-> filey-thing
               clojure.java.io/file
               .exists)
       (catch Exception e 
         false)))

(defn flexmark-document?
  [o]
  (instance? com.vladsch.flexmark.util.ast.Document o))


(defn valid-uuid?
  [v]
  (or (instance? java.util.UUID v)
      (try
        (java.util.UUID/fromString v)
        (catch Exception e false))))

(s/def ::filter (s/every #{:meta :technical :clojure :spiritual :music :audio :wphillips-weekly}))
(s/def ::status #{
                  :draft           ;; Drafts aren't visible at all publicly.
                  :published       ;; Published are totally visible.
                  :soft-published  ;; Soft published don't show up in indices.
                  })
(s/def ::id valid-uuid?)
(s/def ::title string?)
(s/def ::timestamp 
  (s/and int? 
         can-be-converted-to-instant? 
         is-semi-plausibly-within-bills-lifespan?))
(s/def ::markdown-rel-path
  (s/and string? markdown-filename? valid-resource-path?))
(s/def ::extra-resources-rel
  (s/every (s/and string? valid-resource-path?)))

(def intern-post
  "A post inside of gascan."
  (s/keys :req-un [::id 
                   ::title 
                   ::timestamp 
                   ::markdown-rel-path 
                   ::extra-resources-rel
                   ::filter
                   ::status]
          :opt-un [::parsed-markdown])) 

(s/def ::parsed-markdown flexmark-document?)
(s/def ::markdown-abs-path valid-file?)
(s/def ::extra-resources (s/every valid-file?))
(s/def ::dir-depth #(and (int? %) (<= 0 % 1)))

(def remote-post
  "Some markdown file outside of gascan that may be imported."
  (s/keys :req-un [::title
                   ::timestamp
                   ::parsed-markdown
                   ::markdown-abs-path
                   ::extra-resources
                   ::dir-depth]))

