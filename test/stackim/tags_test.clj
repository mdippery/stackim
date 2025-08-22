(ns stackim.tags-test
  (:require [clojure.test :refer :all]
            [clojure.java.jdbc :as jdbc]
            [stackim.tags :as tags]))


(defn- jdbc-query [opts query]
  (if (= (last query) "mpd") [{:name "mpd" :stack_id 28804}] {}))

(defn- jdbc-insert! [opts tbl row] true)

(defn- jdbc-execute! [opts query] true)

(deftest insert
  (testing "insert"
    (with-redefs [jdbc/insert! jdbc-insert!]
      (is (tags/insert "mpd" 28804)))))

(deftest record-visit
  (testing "record-visit"
    (with-redefs [jdbc/execute! jdbc-execute!]
      (is (tags/record-visit "mpd" "http://www.example.com/")))))

(deftest exists?
  (testing "exists?"
    (with-redefs [jdbc/query jdbc-query]
      (is (tags/exists? "mpd"))
      (is (not (tags/exists? "foo"))))))

(deftest valid?
  (testing "valid?"
    (testing "is valid"
      (testing "an alphabetic string"
        (is (tags/valid? "foo")))
      (testing "a numeric string"
        (is (tags/valid? "123")))
      (testing "an alphanumeric string"
        (testing "beginning with alpha"
          (is (tags/valid? "foo123")))
        (testing "beginning with numbers"
          (is (tags/valid? "123foo")))
        (testing "with alternating character classes"
          (testing "beginning with an alpha"
            (is (tags/valid? "f1o2o3")))
          (testing "beginning with a number"
            (is (tags/valid? "1f2o3o"))))))
    (testing "is not valid"
      (testing "an empty string"
        (is (not (tags/valid? ""))))
      (testing "a string with symbols"
        (is (not (tags/valid? "foo@1")))))))
