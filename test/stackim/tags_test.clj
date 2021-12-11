(ns stackim.tags-test
  (:require [clojure.test :refer :all]
            [clojure.java.jdbc :as jdbc]
            [stackim.tags :as tags]))


(defn- jdbc-query [opts query]
  (if (= (last query) "mpd") [{:name "mpd" :stack_id 28804}] {}))

(defn- jdbc-insert! [opts tbl row] true)

(defn- jdbc-execute! [opts query] true)

(deftest test-insert
  (with-redefs [jdbc/insert! jdbc-insert!]
    (is (tags/insert "mpd" 28804))))

(deftest test-record-visit
  (with-redefs [jdbc/execute! jdbc-execute!]
    (is (tags/record-visit "mpd" "http://www.example.com/"))))

(deftest test-exists?
  (with-redefs [jdbc/query jdbc-query]
    (is (tags/exists? "mpd"))
    (is (not (tags/exists? "foo")))))

(deftest test-valid?
  (is (tags/valid? "foo"))
  (is (tags/valid? "123"))
  (is (tags/valid? "foo123"))
  (is (tags/valid? "123foo"))
  (is (tags/valid? "f1o2o3"))
  (is (tags/valid? "1f2o3o"))
  (is (not (tags/valid? "")))
  (is (not (tags/valid? "foo@1"))))
