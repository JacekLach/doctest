(ns doctest.core
  (:require [instaparse.core :as insta]
            [clojure.string :as string]
            [clojure.java.io :refer [resource]]))

(def ^:private docparser (insta/parser (slurp (resource "doctest/doctest.grammar"))))

(defn deindent [indent line]
  (if (.startsWith line indent)
    (subs line (count indent))
    line))

(defn blanklines [line]
  (if (re-matches #"\s*<BLANKLINE>\s*" line)
    ""
    line))

(defn process-output [indent outstrings]
  (map (comp (partial deindent indent)
             blanklines)
       outstrings))

(defn convert-parse-result [[_ & tests]]
  ;; each test looks like: [:T [:DESC string+] [:EXPR string+] [:OUT string+]]
  (map (fn [[tag [dtag & ds] [etag [_ indent] & es] [otag & os]]]
         (assert (= tag :TEST))
         (assert (= dtag :DESC))
         (assert (= etag :EXPR))
         (assert (= otag :OUT))

         (let [os (process-output indent os)]
           (zipmap [:desc :expr :out]
                   (map #(string/join \newline %)
                        [ds es os]))))
       tests))

(defn extract-examples
  ;; wouldn't raw strings be nice
  "Extract all example expressions from a docstring.
  Returns a list of {:desc description :expr expr :out output} maps:
    => (extract-examples
  #_=>  \"=> (+ 1 3)\\n4\")
  ({:out \"4\", :expr \" (+ 1 3)\", :desc \"\"})"

  [s]
  (let [parseresult (docparser s)]
    (if (insta/failure? parseresult)
      nil
      (convert-parse-result parseresult))))

(defn check-exception [e test]
  false)

(defn eval-expr [expr]
  (with-out-str
    ((comp println eval read-string) expr)))

(defn run-test [test]
  (try
    (= (:out test)
       (eval-expr (:expr test)))
    (catch Exception e
      (check-exception e test))))
