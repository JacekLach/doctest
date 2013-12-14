(ns doctest.input
  (:import java.io.ByteArrayInputStream
           java.io.ByteArrayOutputStream))

(defn ->stdin [lines]
  (java.io.ByteArrayInputStream. (.getBytes lines "UTF-8")))

(defn ->stdout []
  (java.io.ByteArrayOutputStream.))

(defn ->writer [stdout]
  ;; FIXME: bufferred? Bad, jline writes directly to the stream
  (java.io.OutputStreamWriter. stdout "UTF-8"))
