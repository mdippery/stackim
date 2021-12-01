(def project-version "2.0.0-SNAPSHOT")

(defproject stackim project-version
  :description "URL shortener for Stack Overflow profiles"
  :url "http://stack.im/"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [selmer "1.11.0"]]
  :main ^:skip-aot stackim.core
  :target-path "target/%s"
  :jar-name "stackim.jar"
  :uberjar-name "stackim-standalone.jar"
  :manifest {"Implementation-Version" ~project-version}
  :profiles {:uberjar {:aot :all}})
