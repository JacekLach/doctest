(ns doctest.core-test
  (:require [clojure.java.io :refer [resource]]
            [clojure.string :as string]
            [doctest.core :refer :all]
            [midje.sweet :refer :all]))

(def examples (slurp (resource "examples")))

(def results
  [{:desc "A single line description"
    :expr " (+ 1 2 3)"
    :out  "6"}
   {:desc (string/join "\n"
                       ["Multi line description"
                        "followed by multi line expression"
                        "followed by multi line result"
                        "followed by another expression and result"])
    :expr (string/join "\n"
                       [" (do (println"
                        "      \"very magic\")"
                        "     1)"])
    :out  (string/join "\n"
                       ["very magic"
                        "1"])}
   {:desc ""
    :expr " (- 4 3)"
    :out  "1"}
   {:desc "Blanklines and EOF:"
    :expr " (do (println))"
    :out  ""}
   {:desc "Unindented example"
    :expr "1"
    :out  "1"}
   {:desc "Unindented multiline example"
    :expr "(do\n (println 2))"
    :out  "2\nnil"}])

(fact "Example extraction"
  (doseq [[given expected] (map vector
                                (extract-examples examples)
                                results)]
    given => expected))
