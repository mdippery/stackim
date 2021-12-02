(ns stackim.core
  (:gen-class
    :methods [^:static [handleHomepage [Object] String]])
  (:require [clojure.data.json :as json]
            [selmer.parser :as selmer]
            [stackim.db :as db]
            [stackim.tags :as tags]))


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
  {:status code :body (str body "\n")})

(defn ok [body]
  {:status 200 :body (str body "\n")})


(defn get-tag [tag]
  (if (tags/exists? tag)
      (do
        (tags/record-visit tag "-")
        (-> tag tags/stack-id stack-url redirect))
      ({:status 404 :body (str "No tag for " tag)})))

(defn put-tag [tag id]
  (cond
    (nil? id)
      (oops 400 "PUT request without 'stackid' parameter")

    (not (tags/valid? tag))
      (oops 403 "Shortened URL may only contain alphanumeric characters")

    (tags/exists? tag)
      (oops 409 (str "Tag '" tag "' is already in use"))

    (not (int? id))
      (oops 403 (str "Invalid Stack Overflow ID: '" id "'"))

    :else
      (do
        (tags/insert tag (Integer/parseInt id))
        (ok "OK"))))

(defn homepage []
  (selmer/render-file "templates/home.html" {}))

(defn json-response [status headers body]
  (json/write-str
    {:statusCode status
     :headers (json/write-str headers)
     :body body
     :isBase64Encoded false}))


; Documentation for response formats for HTTP APIs can be found at
; <https://docs.aws.amazon.com/apigateway/latest/developerguide/http-api-develop-integrations-lambda.html>

(defn -handleHomepage [o]
  (json-response 200 {:Content-Type "text/html"} (homepage)))
