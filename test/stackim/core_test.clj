(ns stackim.core-test
  (:require [clojure.test :refer :all]
            [clojure.java.jdbc :as jdbc]
            [ring.mock.request :as mock]
            [stackim.core :as core]
            [stackim.env :as env]))


(defn- jdbc-query [opts query]
  (if (= (last query) "mpd") [{:name "mpd" :stack_id 28804}] {}))

(defn- jdbc-insert! [opts tbl row] true)

(defn- jdbc-execute! [opts query] true)

(deftest port-configuration
  (testing "port configuration"
    (testing "with environment variable"
      (binding [env/*env* {"PORT" "8080"}]
        (is (= (core/port) 8080))))

    (testing "without environment variable"
      (is (= (core/port) 5000)))))

(deftest stack-url
  (testing "stack-url"
    (is (= (core/stack-url 28804) "http://stackoverflow.com/users/28804"))))

(deftest valid-int?
  (testing "valid-int?"
    (testing "with positive integer"
      (is (core/valid-int? "28804")))
    (testing "with negative integer"
      (is (not (core/valid-int? "-28804"))))
    (testing "with floating point number"
      (is (not (core/valid-int? "100.5"))))
    (testing "with empty string"
      (is (not (core/valid-int? ""))))
    (testing "with any string"
      (is (not (core/valid-int? "foo"))))))

(deftest redirect
  (testing "redirect"
    (testing "redirects from tag"
      (let [res (core/redirect "http://www.example.com/")]
        (is (= (:status res) 301))
        (is (= (get (:headers res) "Location") "http://www.example.com/"))
        (is (= (:body res) ""))))))

(deftest oops
  (testing "oops"
    (testing "returns an error status and string"
      (let [res (core/oops 409 "Tag 'foo' is already in use")]
        (is (= (:status res) 409))
        (is (= (:body res) "Tag 'foo' is already in use\n"))))))

(deftest ok
  (testing "ok"
    (testing "returns successfully"
      (let [res (core/ok "Success!")]
        (is (= (:status res) 200))
        (is (= (:body res) "Success!\n"))))))

(deftest get-tag
  (testing "get-tag"
    (with-redefs [jdbc/query jdbc-query
                  jdbc/insert! jdbc-insert!
                  jdbc/execute! jdbc-execute!]
      (testing "tag exists"
        (let [res (core/get-tag "mpd")]
          (is (= (:status res) 301))
          (is (= (get (:headers res) "Location") "http://stackoverflow.com/users/28804"))
          (is (= (:body res) ""))))
      (testing "tag does not exist"
        (let [res (core/get-tag "foo")]
          (is (= (:status res) 404))
          (is (nil? (get (:headers res) "Location")))
          (is (= (:body res) "No tag for foo\n")))))))

(deftest put-tag
  (testing "put-tag"
    (with-redefs [jdbc/query jdbc-query
                  jdbc/insert! jdbc-insert!
                  jdbc/execute! jdbc-execute!]
      (testing "no stackid"
        (let [res (core/put-tag "mpd" nil)]
          (is (= (:status res) 400))
          (is (= (:body res) "PUT request without 'stackid' parameter\n"))))
      (testing "invalid tag"
        (let [res (core/put-tag "foo@bar" "28804")]
          (is (= (:status res) 403))
          (is (= (:body res) "Shortened URL may only contain alphanumeric characters\n"))))
      (testing "tag exists"
        (let [res (core/put-tag "mpd" "28805")]
          (is (= (:status res) 409))
          (is (= (:body res) "Tag 'mpd' is already in use\n"))))
      (testing "invalid integer"
        (let [res (core/put-tag "foo" "foo")]
          (is (= (:status res) 403))
          (is (= (:body res) "Invalid Stack Overflow ID: 'foo'\n"))))
      (testing "negative integer"
        (let [res (core/put-tag "foo" "-28804")]
          (is (= (:status res) 403))
          (is (= (:body res) "Invalid Stack Overflow ID: '-28804'\n"))))
      (testing "valid request"
        (let [res (core/put-tag "foo" "28805")]
          (is (= (:status res) 200))
          (is (= (:body res) "OK\n")))))))

(deftest get-homepage
  (testing "/"
    (testing "returns 200 OK"
      (let [resp (core/stackim (mock/request :get "/"))]
        (is (= (:status resp) 200))))))
