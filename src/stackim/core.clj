(ns stackim.core
  (:gen-class)
  (:require [clojure.string :as str]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [org.httpkit.server :as http]
            [ring.middleware.params :as ring]
            [selmer.parser :as selmer]
            [stackim.db :as db]
            [stackim.env :as env]
            [stackim.middleware :as middleware]
            [stackim.tags :as tags]))

(defn port []
  (Integer/parseInt (env/getenv "PORT" "5000")))

(defn stack-url [id]
  (str "http://stackoverflow.com/users/" id))

(defn valid-int? [n]
  (try
    (let [i (Integer/parseInt n)]
      (>= i 0))
    (catch NumberFormatException e false)))

(defn redirect [to]
  {:status 301 :headers {"Location" to} :body ""})

(defn oops [code body]
  {:status code :body (str body "\n")})

(defn ok [body]
  {:status 200 :body (str body "\n")})


(defn get-tag [tag]
  (if (tags/exists? tag)
      (do
        (tags/record-visit tag "-")
        (-> tag tags/stack-id stack-url redirect))
      {:status 404 :body (str "No tag for " tag "\n")}))

(defn put-tag [tag id]
  (cond
    (nil? id)
      (oops 400 "PUT request without 'stackid' parameter")

    (not (tags/valid? tag))
      (oops 403 "Shortened URL may only contain alphanumeric characters")

    (tags/exists? tag)
      (oops 409 (str "Tag '" tag "' is already in use"))

    (not (valid-int? id))
      (oops 403 (str "Invalid Stack Overflow ID: '" id "'"))

    :else
      (do
        (tags/insert tag (Integer/parseInt id))
        (ok "OK"))))

(defroutes stackim
  (GET "/" [] (selmer/render-file "templates/home.html" {}))
  (GET "/:tag" [tag] (get-tag tag))
  (PUT "/:tag" req (put-tag (:tag (:params req)) (get (:params req) "stackid")))
  (route/resources "/"))

(def app
  (-> stackim
      middleware/add-hsts
      middleware/redirect-canonical-host
      ring/wrap-params))

(defn -main []
  (println "Starting server on port" (port))
  (http/run-server app {:port (port)}))
