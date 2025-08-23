(ns stackim.middleware-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [stackim.core :as core]
            [stackim.middleware :as middleware]))

(deftest canonical-port
  (testing "canonical port configuration"
    (testing "with environment variable"
      (binding [core/*env* {"CANONICAL_PORT" "8080"}]
        (let [request (-> (mock/request :get "/")
                          (mock/header "Content-Type" "text/html"))]
          (is (= (middleware/canonical-port request) 8080)))))

    (testing "without environment variable"
      (testing "as http"
        (testing "without header"
          (let [request (-> (mock/request :get "/")
                            (mock/header "Content-Type" "text/html"))]
            (is (= (middleware/canonical-port request) 80))))

        (testing "with header"
          (let [request (-> (mock/request :get "/")
                            (mock/header "X-Forwarded-Proto" "http")
                            (mock/header "Content-Type" "text/html"))]
            (is (= (middleware/canonical-port request) 80)))))

      (testing "as https"
        (let [request (-> (mock/request :get "/")
                          (mock/header "X-Forwarded-Proto" "https")
                          (mock/header "Content-Type" "text/html"))]
          (is (= (middleware/canonical-port request) 443)))))))

(deftest canonical-host
  (testing "canonical host configuration"
    (testing "without environment variable"
      (binding [core/*env* {"CANONICAL_HOST" "stack.im"}]
        (let [request (-> (mock/request :get "/")
                          (mock/header "Content-Type" "text/html"))]
          (is (= (middleware/canonical-host request) "stack.im"))))

      (testing "with header"
        (binding [core/*env* {"CANONICAL_HOST" "stack.im"}]
          (let [request (-> (mock/request :get "/")
                            (mock/header "X-Forwarded-Proto" "https")
                            (mock/header "Content-Type" "text/html"))]
            (is (= (middleware/canonical-host request) "stack.im"))))))

    (testing "with environment variable"
      (let [request (-> (mock/request :get "/")
                        (mock/header "Content-Type" "text/html"))]
        (is (= (middleware/canonical-host request) "localhost"))))))

(deftest canonical-protocol
  (testing "canonical protocol"
    (testing "with header set"
      (let [request (-> (mock/request :get "/")
                        (mock/header "X-Forwarded-Proto" "https")
                        (mock/header "Content-Type" "text/html"))]
        (is (= (middleware/canonical-proto request) "https"))))

    (testing "without header set"
      (let [request (-> (mock/request :get "/")
                        (mock/header "Content-Type" "text/html"))]
        (is (= (middleware/canonical-proto request) "http"))))))

(deftest canonical-redirect-url
  (testing "canonical redirect url"

    (testing "with CANONICAL_HOST set"
      (binding [core/*env* {"CANONICAL_HOST" "stack.im"}]
        (let [request (-> (mock/request :get "/tag")
                          (mock/header "Content-Type" "text/html"))]
          (is (= (middleware/canonical-redirect-url request) "http://stack.im/tag")))))

    (testing "with CANONICAL_PORT set"
      (binding [core/*env* {"CANONICAL_HOST" "stack.im" "CANONICAL_PORT" "5000"}]
        (let [request (-> (mock/request :get "/tag")
                          (mock/header "Content-Type" "text/html"))]
          (is (= (middleware/canonical-redirect-url request) "http://stack.im:5000/tag")))))

    (testing "with X-Forwarded-Proto set"

      (testing "to https"
        (binding [core/*env* {"CANONICAL_HOST" "stack.im"}]
          (let [request (-> (mock/request :get "/tag")
                            (mock/header "X-Forwarded-Proto" "https")
                            (mock/header "Content-Type" "text/html"))]
            (is (= (middleware/canonical-redirect-url request) "https://stack.im/tag")))))

      (testing "to http"
        (binding [core/*env* {"CANONICAL_HOST" "stack.im"}]
          (let [request (-> (mock/request :get "/tag")
                            (mock/header "X-Forwarded-Proto" "http")
                            (mock/header "Content-Type" "text/html"))]
            (is (= (middleware/canonical-redirect-url request) "http://stack.im/tag"))))))

    (testing "with everything set"
      (binding [core/*env* {"CANONICAL_HOST" "stack.im" "CANONICAL_PORT" "5000"}]
        (let [request (-> (mock/request :get "/tag")
                          (mock/header "X-Forwarded-Proto" "https")
                          (mock/header "Content-Type" "text/html"))]
          (is (= (middleware/canonical-redirect-url request) "https://stack.im:5000/tag")))))

    (testing "with nothing set"
      (let [request (-> (mock/request :get "/tag")
                        (mock/header "Content-Type" "text/html"))]
        (is (= (middleware/canonical-redirect-url request) "http://localhost/tag"))))))

