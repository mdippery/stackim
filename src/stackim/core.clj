(ns stackim.core
  (:gen-class)
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [org.httpkit.server :as http]
            [selmer.parser :as selmer]
            [stackim.db :as db]))


(def port
  (Integer/parseInt (or (System/getenv "PORT") "5000")))


(defroutes stackim
  (GET "/" [] (selmer/render-file "templates/home.html" {}))
  (route/resources "/"))

(defn -main []
  (db/create-tables!)
  (println "Starting server on port" port)
  (http/run-server stackim {:port port}))
