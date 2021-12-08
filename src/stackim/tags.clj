(ns stackim.tags
  (:require [taoensso.faraday :as far]
            [stackim.db :as db])
  (:import [java.time Instant]))


(defrecord Visit [timestamp referer])

(defrecord Tag [alias profile-id created-at visits])

(defn visit [referer]
  (Visit. (Instant/now) referer))

(defn tag [alias profile-id]
  ; TODO: Convert profile-id to Integer
  (Tag. alias profile-id (Instant/now) []))

(defn tag->dynamodb [tag]
  {:Alias (:alias tag)
   :ProfileID (:profile-id tag)
   :CreatedAt (-> tag :CreatedAt .toString)})

(defn dynamodb->tag [doc]
  (Tag. (:Alias doc) (:ProfileID doc) (Instant/parse (:CreatedAt doc)) (get doc :Hits [])))

(defn record-visit [tag visit]
  (assoc tag :visits (conj (:visits tag) visit)))

(defn load-tag [alias]
  (let [doc (far/get-item db/db-options db/table {:Alias alias})]
    (if (nil? doc) nil (dynamodb->tag doc))))

(defn save-tag [tag]
  (far/put-item db/db-options db/table (tag->dynamodb tag)))

(defmulti exists? type)

(defmethod exists? String [alias]
  (-> alias load-tag nil? not))

(defmethod exists? Tag [tag]
  (-> tag nil? not))

(defmethod exists? nil [_nil]
  false)

(defn stack-id [alias]
  (:profile-id (load-tag alias)))

(defn valid? [alias]
  (not (nil? (re-matches #"[a-zA-Z0-9]+" alias))))
