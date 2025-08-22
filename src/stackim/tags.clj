(ns stackim.tags
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.string :as str]
            [stackim.db :as db]))


(defn insert [tag id]
  (jdbc/insert! db/db :tags {:name (str/lower-case tag) :stack_id id}))

(defn stack-id [tag]
  (-> (jdbc/query db/db ["SELECT stack_id FROM tags WHERE name = ?" (str/lower-case tag)]) first :stack_id))

(defn record-visit [tag referer]
  (jdbc/execute! db/db ["INSERT INTO hits (tag_id, referer) VALUES ((SELECT id FROM tags WHERE name = ?), ?)" (str/lower-case tag) referer]))

(defn exists? [tag]
  (-> tag stack-id nil? not))

(defn valid? [tag]
  (not (nil? (re-matches #"[a-zA-Z0-9]+" tag))))
