(ns stackim.middleware
  (:require [clojure.string :as str]
            [ring.util.request :as request]
            [stackim.env :as env]))

(def hsts-age
  ; 365 days
  (* 60 60 24 365))

(def hsts-header-value
  (str "max-age=" hsts-age "; includeSubDomains; preload"))

(defn header [request name]
  (get-in request [:headers (str/lower-case name)]))

(defn canonical-proto [request]
  (or (header request "X-Forwarded-Proto") "http"))

(defn canonical-host [request]
  (env/getenv "CANONICAL_HOST" "localhost"))

(defn canonical-port [request]
  (let [default-port (if (= (canonical-proto request) "https") "443" "80")]
    (Integer/parseInt (env/getenv "CANONICAL_PORT" default-port))))

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

(defn add-header [response header-name header-value]
  (assoc-in response [:headers header-name] header-value))

(defn add-hsts [handler]
  (fn [request]
    (let [response (handler request)]
      (if (= (canonical-proto request) "https")
        (add-header response "Strict-Transport-Security" hsts-header-value)
        response))))
