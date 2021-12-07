(ns stackim.tags
  (:require [taoensso.faraday :as far]
            [stackim.db :as db])
  (:import [java.time Instant]))


(defn- now []
  (.toString (Instant/now)))

(defn- record [tag]
  (far/get-item db/db-options db/table {:Alias tag}))

(defn stack-id [tag]
  (:ProfileID (record tag)))

(defn exists? [tag]
  (-> tag stack-id nil? not))

(defn valid? [tag]
  (not (nil? (re-matches #"[a-zA-Z0-9]+" tag))))

(defn record-visit [tag referer]
  (let [rec (record tag)
        hits (get rec :Hits [])
        hit {:Timestamp (now) :Referer referer}]
    (assoc rec :Hits (conj hits hit))))

(defn insert-visit [tag referer]
  (far/put-item db/db-options db/table (record-visit tag referer)))

(defn insert [tag id]
  (if (exists? tag)
    false
    (nil? (far/put-item
            db/db-options
            db/table
            {:Alias tag
             :ProfileID id
             :CreatedAt (now)}))))
