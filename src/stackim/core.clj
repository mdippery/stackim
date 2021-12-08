(ns stackim.core
  (:gen-class
    :methods [^:static [handleHomepage [java.util.Map] java.util.Map]
              ^:static [handleCss [java.util.Map] java.util.Map]
              ^:static [handleGetTag [java.util.Map] java.util.Map]])
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [selmer.parser :as selmer]
            [stackim.db :as db]
            [stackim.tags :as tags])
  (:import (java.io FileNotFoundException)))


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


(defn get-tag [alias referer]
  (let [tag (tags/load-tag alias)]
    (if (tags/exists? tag)
        (do
          (tags/record-visit tag referer)
          (tags/save-tag tag)
          (-> tag tags/stack-id stack-url redirect))
        ({:status 404 :body (str "No tag for " tag)})))

(defn put-tag [alias id]
  (cond
    (nil? id)
      (oops 400 "PUT request without 'stackid' parameter")

    (not (tags/valid? alias))
      (oops 403 "Shortened URL may only contain alphanumeric characters")

    (tags/exists? alias)
      (oops 409 (str "Tag '" alias "' is already in use"))

    (not (int? id))
      (oops 403 (str "Invalid Stack Overflow ID: '" id "'"))

    :else
      (do
        (tags/save-tag (tags/tag alias (Integer/parseInt id)))
        (ok "OK"))))

(defn homepage []
  (selmer/render-file "templates/home.html" {}))

(defn load-resource [path]
  (try
    (slurp (io/resource path))
    (catch IllegalArgumentException ex
      nil)
    (catch FileNotFoundException ex
      nil)))

(defn json-response [status mimetype body]
  (java.util.HashMap.
    {"statusCode" status
     "headers" {"Content-Type" mimetype}
     "body" body
     "isBase64Encoded" false}))

(defn- path [o]
  (get-in o ["requestContext" "http" "path"]))

(defn- trim-slash [s]
  (str/replace s #"(^/+)|(/+$)" ""))


; Documentation for response formats for HTTP APIs can be found at
; <https://docs.aws.amazon.com/apigateway/latest/developerguide/http-api-develop-integrations-lambda.html>

(defn -handleHomepage [o]
  (json-response 200 "text/html" (homepage)))

(defn -handleCss [o]
  (let [css (load-resource (str "public" (path o)))]
    (case css
      nil (json-response 404 "text/plain" "Not found")
    (json-response 200 "text/css" css))))

(defn -handleGetTag [o]
  (let [tag (trim-slash (path o))
        referer (get-in o ["headers" "Referer"])
        res (get-tag tag referer)]
    (json-response (:status res) "text/plain" (:body res))))
