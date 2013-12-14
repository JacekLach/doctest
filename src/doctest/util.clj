(ns doctest.util)

(defn join-lines [lines]
  (apply str (map #(str % "\n") lines)))
