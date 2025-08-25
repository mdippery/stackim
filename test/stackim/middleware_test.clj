(ns stackim.middleware-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [stackim.core :as core]
            [stackim.env :as env]
            [stackim.middleware :as middleware]))

(deftest hsts-age
  (testing "HSTS age"
    (= middleware/hsts-age 31536000)))

(deftest hsts-header-value
  (testing "Strict-Transport-Security value"
    (= middleware/hsts-header-value "max-age=31536000; includeSubDomains; preload")))

(deftest canonical-port
  (testing "canonical port configuration"
    (testing "with environment variable"
      (binding [env/*env* {"CANONICAL_PORT" "8080"}]
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
      (binding [env/*env* {"CANONICAL_HOST" "stack.im"}]
        (let [request (-> (mock/request :get "/")
                          (mock/header "Content-Type" "text/html"))]
          (is (= (middleware/canonical-host request) "stack.im"))))

      (testing "with header"
        (binding [env/*env* {"CANONICAL_HOST" "stack.im"}]
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
      (binding [env/*env* {"CANONICAL_HOST" "stack.im"}]
        (let [request (-> (mock/request :get "/tag")
                          (mock/header "Content-Type" "text/html"))]
          (is (= (middleware/canonical-redirect-url request) "http://stack.im/tag")))))

    (testing "with CANONICAL_PORT set"
      (binding [env/*env* {"CANONICAL_HOST" "stack.im" "CANONICAL_PORT" "5000"}]
        (let [request (-> (mock/request :get "/tag")
                          (mock/header "Content-Type" "text/html"))]
          (is (= (middleware/canonical-redirect-url request) "http://stack.im:5000/tag")))))

    (testing "with X-Forwarded-Proto set"

      (testing "to https"
        (binding [env/*env* {"CANONICAL_HOST" "stack.im"}]
          (let [request (-> (mock/request :get "/tag")
                            (mock/header "X-Forwarded-Proto" "https")
                            (mock/header "Content-Type" "text/html"))]
            (is (= (middleware/canonical-redirect-url request) "https://stack.im/tag")))))

      (testing "to http"
        (binding [env/*env* {"CANONICAL_HOST" "stack.im"}]
          (let [request (-> (mock/request :get "/tag")
                            (mock/header "X-Forwarded-Proto" "http")
                            (mock/header "Content-Type" "text/html"))]
            (is (= (middleware/canonical-redirect-url request) "http://stack.im/tag"))))))

    (testing "with everything set"
      (binding [env/*env* {"CANONICAL_HOST" "stack.im" "CANONICAL_PORT" "5000"}]
        (let [request (-> (mock/request :get "/tag")
                          (mock/header "X-Forwarded-Proto" "https")
                          (mock/header "Content-Type" "text/html"))]
          (is (= (middleware/canonical-redirect-url request) "https://stack.im:5000/tag")))))

    (testing "with nothing set"
      (let [request (-> (mock/request :get "/tag")
                        (mock/header "Content-Type" "text/html"))]
        (is (= (middleware/canonical-redirect-url request) "http://localhost/tag"))))))

(deftest add-header
  (testing "add header"
    (let [in-response {:headers {"Content-Type" "text/html" "Etag" "0"}
                       :body "Hello, world!"}
          expected-response {:headers {"Content-Type" "text/html"
                                       "Etag" "0"
                                       "Content-Length" "13"}
                             :body "Hello, world!"}
          out-response (middleware/add-header in-response "Content-Length" "13")]
      (= out-response expected-response))))

(deftest add-hsts
  (testing "add-hsts middleware"
    (let [mock-handler (fn [request] {:status 200 :body "OK"})
          wrapped (middleware/add-hsts mock-handler)]
      (testing "when http"
        (testing "and X-Forward-Proto header is not present"
          (let [request (-> (mock/request :get "/")
                            (mock/header "Content-Type" "text/html"))
                response (wrapped request)
                headers (:headers response)]
            (testing "Strict-Transport-Security header is not added"
              (is (not (contains? headers "Strict-Transport-Security"))))))

        (testing "and X-Forward-Proto header is 'http'"
          (let [request (-> (mock/request :get "/")
                            (mock/header "X-Forwarded-Proto" "http")
                            (mock/header "Content-Type" "text/html"))
                response (wrapped request)
                headers (:headers response)]
            (testing "Strict-Transport-Security header is not added"
              (is (not (contains? headers "Strict-Transport-Security")))))))

      (testing "when https"
        (let [request (-> (mock/request :get "/")
                          (mock/header "X-Forwarded-Proto" "https")
                          (mock/header "Content-Type" "text/html"))
              response (wrapped request)
              headers (:headers response)]
          (testing "Strict-Transport-Security header is added"
            (is (contains? headers "Strict-Transport-Security"))))))))
