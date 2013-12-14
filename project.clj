(defproject doctest "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [instaparse "1.2.11"]
                 [reply "0.3.0"]]
  :aot [doctest.input]
  :profiles {:dev {:dependencies [[midje "1.6-beta1"]]
                   :plugins      [[lein-midje "3.0.0"]]}})
