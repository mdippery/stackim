(ns stackim.db
  (:require [clojure.java.jdbc :as jdbc])
  (:import [java.io File]
           [java.net URI]
           [java.sql BatchUpdateException]))


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

(def tag-ddl
  (jdbc/create-table-ddl :tags
                         [[:id :integer :primary :key :autoincrement]
                          [:name "varchar(256)" "NOT NULL"]
                          [:stack_id :integer "NOT NULL"]
                          [:created_at :timestamp "NOT NULL" "DEFAULT CURRENT_TIMESTAMP"]]))

(def hit-ddl
  (jdbc/create-table-ddl :hits
                         [[:id :integer :primary :key :autoincrement]
                          [:tag_id :integer "REFERENCES tags (id)"]
                          [:timestamp :timestamp "NOT NULL" "DEFAULT CURRENT_TIMESTAMP"]
                          [:referer "varchar(1024)" "NOT NULL"]]))

(defn create-tables! []
  (try
    (jdbc/db-do-commands db
                         [tag-ddl
                          hit-ddl
                          "CREATE INDEX tag_idx ON tags (name)"])
    (catch BatchUpdateException e)))
