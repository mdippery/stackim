(ns stackim.core
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [org.httpkit.server :refer [run-server]]
            [selmer.parser :refer [render-file]]))


(defroutes stackim
  (GET "/" [] (render-file "templates/home.html" {}))
  (route/resources "/"))


(defn -main []
  (run-server stackim {:port 5000}))
