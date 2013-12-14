(ns doctest.core
  (:require [instaparse.core :as insta]
            [doctest.input :refer :all]
            [doctest.util :refer :all]
            [reply.main :refer [launch-standalone]]
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
                   (map join-lines
                        [ds es os]))))
       tests))

(defn extract-examples
  ;; wouldn't raw strings be nice
  "Extract all example expressions from a docstring.
  Returns a list of {:desc description :expr expr :out output} maps:
    => (extract-examples
  #_=>  \"=> (+ 1 3)\\n4\")
  ({:out \"4\n\", :expr \" (+ 1 3)\", :desc \"\"})"

  [s]
  (let [parseresult (docparser s)]
    (if (insta/failure? parseresult)
      nil
      (convert-parse-result parseresult))))

(defn check-exception [e test]
  false)

(defn group-outs
  "Separate 'typed' input and the output from a repl log."
  [repl-output]
  (into {}
    (map (fn [[k v]] [k (join-lines v)])
         (group-by #(if (re-matches #"^\s*([a-zA-Z.*+!_?-]*|#_)=>.*" %)
                      :in
                      :out)
                   (string/split-lines repl-output)))))

(defn eval-expr [expr]
  (let [repl-output (->stdout)
        repl-input  (->stdin expr)]
    (binding [*ns* (find-ns 'user)
              *out* (->writer repl-output)
              *in*  repl-input]
      (with-redefs [reply.main/say-goodbye (fn [])]
        (launch-standalone {:input-stream repl-input
                            :output-stream repl-output
                            :blink-parens false
                            :skip-default-init true})))
    (let [outs (.toString repl-output)]
      (merge {:full outs}
             (group-outs outs)))))


(defn run-test [test]
  (let [result (eval-expr (:expr test))
        matches (= (:out test) (:out result))]
    (when-not matches
      (println "Failed doctest:")
      (println "---------------")
      (println (:full matches))
      (println "---------------")
      (println "does not match expected output:")
      (println (:out test)))))
