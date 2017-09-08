(ns stackim.core
  (:import [java.io File]))

(def cwd
  (.getCanonicalPath (File. ".")))

(def default-database-url
  (str "sqlite://" cwd "/stackim.db"))

(def database-url
  (or (System/getenv "DATABASE_URL") default-database-url))
