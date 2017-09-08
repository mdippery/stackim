(ns stackim.tags
  (:require [clojure.java.jdbc :as jdbc]
            [stackim.db :as db]))


(defn insert [tag id]
  (jdbc/insert! db/db :tags {:name tag :stack_id id}))

(defn stack-id [tag]
  (-> (jdbc/query db/db ["SELECT stack_id FROM tags WHERE name = ?" tag]) first :stack_id))

(defn record-visit [tag hostname]
  (jdbc/execute! db/db ["INSERT INTO hits (tag_id, hostname) VALUES ((SELECT id FROM tags WHERE name = ?), ?)" tag hostname]))

(defn exists? [tag]
  (-> tag stack-id nil? not))

(defn valid? [tag]
  (not (nil? (re-matches #"[a-zA-Z0-9]+" tag))))
