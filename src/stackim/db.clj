(ns stackim.db
  (:require [clojure.java.jdbc :as sql])
  (:import [java.io File]
           [java.net URI]))


(def cwd
  (.getCanonicalPath (File. ".")))

(def default-database-url
  (str "sqlite://" cwd "/stackim.db"))

(def database-url
  (or (System/getenv "DATABASE_URL") default-database-url))

(def database-uri
  (URI. database-url))

(def db
  {:classname "org.sqlite.JDBC"
   :subprotocol (.getScheme database-uri)
   :subname (.getPath database-uri)
   })
