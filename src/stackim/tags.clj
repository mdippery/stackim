(ns stackim.tags
  (:require [taoensso.faraday :as far]
            [stackim.db :as db]))


(defn stack-id [tag]
  (:ProfileID (far/get-item db/db-options db/table {:Alias tag})))

(defn exists? [tag]
  (-> tag stack-id nil? not))

(defn valid? [tag]
  (not (nil? (re-matches #"[a-zA-Z0-9]+" tag))))

(defn record-visit [tag referer]
  ; (jdbc/execute! db/db ["INSERT INTO hits (tag_id, referer) VALUES ((SELECT id FROM tags WHERE name = ?), ?)" tag referer]))
  nil)

(defn insert [tag id]
  (if (exists? tag)
    false
    (nil? (far/put-item db/db-options db/table {:Alias tag :ProfileID id}))))
