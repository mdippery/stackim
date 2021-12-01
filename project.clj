(def project-version "3.0.0-SNAPSHOT")

(defproject stackim project-version
  :description "URL shortener for Stack Overflow profiles"
  :url "http://stack.im/"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [com.amazonaws/aws-lambda-java-core "1.2.1"]
                 [selmer "1.11.0"]]
  :target-path "target/%s"
  :jar-name "stackim-%s.jar"
  :uberjar-name "stackim-standalone-%s.jar"
  :manifest {"Implementation-Version" ~project-version}
  :profiles {:uberjar {:aot :all}})
