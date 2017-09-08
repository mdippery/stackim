(ns stackim.core
  (:gen-class)
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [org.httpkit.server :as http]
            [selmer.parser :as selmer]
            [stackim.db :as db]
            [stackim.tags :as tags]))


(def port
  (Integer/parseInt (or (System/getenv "PORT") "5000")))

(defn stack-url [id]
  (str "http://stackoverflow.com/users/" id))

(defn redirect [to]
  {:status 301 :headers {"Location" to} :body ""})


(defn get-tag [tag]
  (if (tags/exists? tag)
      (do
        (tags/record-visit tag "-")
        (-> tag tags/stack-id stack-url redirect))
      ({:status 404 :body (str "No tag for " tag)})))

(defroutes stackim
  (GET "/" [] (selmer/render-file "templates/home.html" {}))
  (GET "/:tag" [tag] (get-tag tag))
  (route/resources "/"))

(defn -main []
  (db/create-tables!)
  (println "Starting server on port" port)
  (http/run-server stackim {:port port}))
