(def project-version "2.0.0-SNAPSHOT")

(defproject stackim project-version
  :description "URL shortener for Stack Overflow profiles"
  :url "http://stack.im/"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/java.jdbc "0.7.1"]
                 [compojure "1.6.0"]
                 [http-kit "2.2.0"]
                 [org.xerial/sqlite-jdbc "3.20.0"]
                 [selmer "1.11.0"]]
  :main ^:skip-aot stackim.core
  :target-path "target/%s"
  :jar-name "stackim.jar"
  :uberjar-name "stackim-standalone.jar"
  :manifest {"Implementation-Version" ~project-version}
  :profiles {:uberjar {:aot :all}})
