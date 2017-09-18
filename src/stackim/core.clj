(ns stackim.core
  (:gen-class)
  (:require [clojure.string :as str]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [org.httpkit.server :as http]
            [ring.middleware.params :as ring]
            [selmer.parser :as selmer]
            [stackim.db :as db]
            [stackim.tags :as tags]))


(def port
  (Integer/parseInt (or (System/getenv "PORT") "5000")))

(defn stack-url [id]
  (str "http://stackoverflow.com/users/" id))

(defn int? [n]
  (try
    (do
      (Integer/parseInt n)
      true)
    (catch NumberFormatException e false)))

(defn redirect [to]
  {:status 301 :headers {"Location" to} :body ""})

(defn oops [code body]
  {:status code :body body})

(defn ok [body]
  {:status 200 :body body})


(defn get-tag [tag]
  (if (tags/exists? tag)
      (do
        (tags/record-visit tag "-")
        (-> tag tags/stack-id stack-url redirect))
      ({:status 404 :body (str "No tag for " tag)})))

(defn put-tag [tag id]
  (cond
    (nil? id) (oops 400 "PUT request without 'stackid' parameter")
    (not (tags/valid? tag)) (oops 403 "Shortened URL may only contain alphanumeric characters")
    (tags/exists? tag) (oops 409 (str "Tag '" tag "' is already in use"))
    (not (int? id)) (oops 403 (str "Invalid Stack Overflow ID: '" id "'"))
    :else (do
            (tags/insert tag id)
            (ok "OK"))))

(defroutes stackim
  (GET "/" [] (selmer/render-file "templates/home.html" {}))
  (GET "/:tag" [tag] (get-tag tag))
  (PUT "/:tag" req (put-tag (:tag (:params req)) (get (:params req) "stackid")))
  (route/resources "/"))

(defn -main []
  (println "Starting server on port" port)
  (http/run-server (ring/wrap-params stackim) {:port port}))
