(ns stackim.core
  (:gen-class)
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [org.httpkit.server :refer [run-server]]
            [selmer.parser :refer [render-file]]))


(def port
  (Integer/parseInt (or (System/getenv "PORT") "5000")))


(defroutes stackim
  (GET "/" [] (render-file "templates/home.html" {}))
  (route/resources "/"))

(defn -main []
  (run-server stackim {:port port}))
