(ns stackim.db
  (:require [clojure.java.jdbc :as jdbc])
  (:import [java.io File]
           [java.net URI]))


(def cwd
  (.getCanonicalPath (File. ".")))

(def default-database-url
  (str "postgres://stackim:stackim@localhost/stackim"))

(def database-url
  (or (System/getenv "DATABASE_URL") default-database-url))

(def database-uri
  (URI. database-url))

(def db
  database-url)
