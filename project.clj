(def project-version "2.0.0-SNAPSHOT")

(defproject stackim project-version
  :description "URL shortener for Stack Overflow profiles"
  :url "http://stack.im/"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [org.clojure/java.jdbc "0.7.12"]
                 [compojure "1.6.2"]
                 [http-kit "2.5.3"]
                 [org.postgresql/postgresql "42.3.1"]
                 [org.xerial/sqlite-jdbc "3.36.0.3"]
                 [selmer "1.12.45"]]
  :main ^:skip-aot stackim.core
  :target-path "target/%s"
  :jar-name "stackim.jar"
  :uberjar-name "stackim-standalone.jar"
  :manifest {"Implementation-Version" ~project-version}
  :profiles {:dev {:dependencies [[ring/ring-mock "0.4.0"]]}
             :uberjar {:aot :all}})
