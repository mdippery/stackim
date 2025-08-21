(ns stackim.core-test
  (:require [clojure.test :refer :all]
            [clojure.java.jdbc :as jdbc]
            [ring.mock.request :as ring]
            [stackim.core :as core]))


(defn- jdbc-query [opts query]
  (if (= (last query) "mpd") [{:name "mpd" :stack_id 28804}] {}))

(defn- jdbc-insert! [opts tbl row] true)

(defn- jdbc-execute! [opts query] true)

(deftest test-port-with-envvar
  (binding [core/*env* {"PORT" "8080"}]
    (is (= (core/port) 8080))))

(deftest test-port-wo-envvar
  (is (= (core/port) 5000)))

(deftest test-canonical-port-with-envvar
  (binding [core/*env* {"CANONICAL_PORT" "8080"}]
    (is (= (core/canonical-port) 8080))))

(deftest test-canonical-port-wo-envvar
  (is (= (core/canonical-port) 443)))

(deftest test-canonical-host-with-envvar
  (binding [core/*env* {"CANONICAL_HOST" "stack.im"}]
    (is (= (core/canonical-host) "stack.im:443"))))

(deftest test-canonical-host-wo-envvar
  (binding [core/*env* {"CANONICAL_PORT" "5000"}]
    (is (= (core/canonical-host) "localhost:5000"))))

(deftest test-canonical-proto-with-localhost
  (is (= (core/canonical-proto) "http")))

(deftest test-canonical-proto-with-other-host
  (binding [core/*env* {"CANONICAL_HOST" "stack.im"}]
    (is (= (core/canonical-proto) "https"))))

(deftest test-stack-url
  (is (= (core/stack-url 28804) "http://stackoverflow.com/users/28804")))

(deftest test-valid-int?
  (is (core/valid-int? "28804"))
  (is (not (core/valid-int? "-28804")))
  (is (not (core/valid-int? "100.5")))
  (is (not (core/valid-int? "")))
  (is (not (core/valid-int? "foo"))))

(deftest test-redirect
  (let [res (core/redirect "http://www.example.com/")]
    (is (= (:status res) 301))
    (is (= (get (:headers res) "Location") "http://www.example.com/"))
    (is (= (:body res) ""))))

(deftest test-oops
  (let [res (core/oops 409 "Tag 'foo' is already in use")]
    (is (= (:status res) 409))
    (is (= (:body res) "Tag 'foo' is already in use\n"))))

(deftest test-ok
  (let [res (core/ok "Success!")]
    (is (= (:status res) 200))
    (is (= (:body res) "Success!\n"))))

(deftest test-get-tag
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
        (is (= (:body res) "No tag for foo\n"))))))

(deftest test-put-tag
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
        (is (= (:body res) "OK\n"))))))

(deftest test-get-homepage
  (let [resp (core/stackim (ring/request :get "/"))]
    (is (= (:status resp) 200))))

(deftest test-get-tag
  (with-redefs [jdbc/query jdbc-query
                jdbc/insert! jdbc-insert!
                jdbc/execute! jdbc-execute!]
    (testing "tag exists"
      (let [resp (core/stackim (ring/request :get "/mpd"))]
        (is (= (:status resp) 301))
        (is (= (get (:headers resp) "Location") "http://stackoverflow.com/users/28804"))
        (is (= (:body resp) ""))))
    (testing "tag does not exist"
      (let [resp (core/stackim (ring/request :get "/foo"))]
        (is (= (:status resp) 404))
        (is (nil? (get (:headers resp) "Location")))
        (is (= (:body resp) "No tag for foo\n"))))))
