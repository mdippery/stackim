(ns stackim.middleware
  (:require [clojure.string :as str]
            [ring.util.request :as request]
            [stackim.core :as core]))

(defn header [request name]
  (get-in request [:headers (str/lower-case name)]))

(defn canonical-proto [request]
  (or (header request "X-Forwarded-Proto") "http"))

(defn canonical-host [request]
  (core/getenv "CANONICAL_HOST" "localhost"))

(defn canonical-port [request]
  (let [default-port (if (= (canonical-proto request) "https") "443" "80")]
    (Integer/parseInt (core/getenv "CANONICAL_PORT" default-port))))

(defn- canonical-port-if-not-default [request]
  (let [proto (canonical-proto request)
        port (canonical-port request)
        default-port {"http" 80 "https" 443}]
    (when-not (= port (get default-port proto)) (str ":" port))))

(defn canonical-redirect-url [request]
  (let [path (request/path-info request)
        proto (canonical-proto request)
        host (canonical-host request)
        maybe-port (canonical-port-if-not-default request)]
    (str proto "://" host maybe-port path)))
