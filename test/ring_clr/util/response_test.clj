(ns ring-clr.util.response-test
  (:require [clojure.test :refer [are deftest is testing]]
            [ring-clr.util.response :refer :all]))

(deftest test-redirect
  (is (= {:status 302 :headers {"Location" "http://google.com"} :body ""}
         (redirect "http://google.com")))
  (are [x y] (= (->> x
                     (redirect "/foo")
                     :status)
                y)
    :moved-permanently 301
    :found 302
    :see-other 303
    :temporary-redirect 307
    :permanent-redirect 308
    300 300))

(deftest test-bad-request
  (is (= {:status 400 :headers {} :body "Bad Request"}
         (bad-request "Bad Request"))))

(deftest test-not-found
  (is (= {:status 404 :headers {} :body "Not found"}
         (not-found "Not found"))))

(deftest test-created
  (testing "with location and without body"
    (is (= {:status 201 :headers {"Location" "foobar/location"} :body nil}
           (created "foobar/location"))))
  (testing "with body and with location"
    (is (= {:status 201 :headers {"Location" "foobar/location"} :body "foobar"}
           (created "foobar/location" "foobar")))))

(deftest test-response
  (is (= {:status 200 :headers {} :body "foobar"}
         (response "foobar"))))

(deftest test-status
  (testing "with response"
    (is (= {:status 200 :body ""} (status {:status nil :body ""} 200))))
  (testing "without response"
    (is (= {:status 200 :headers {} :body nil} (status 200)))))

(deftest test-content-type
  (is (= {:status 200 :headers {"Content-Type" "text/html" "Content-Length" "10"}}
         (content-type {:status 200 :headers {"Content-Length" "10"}}
                       "text/html"))))

(deftest test-charset
  (testing "add charset"
    (is (= (charset {:status 200 :headers {"Content-Type" "text/html"}} "UTF-8")
           {:status 200 :headers {"Content-Type" "text/html; charset=UTF-8"}})))
  (testing "replace existing charset"
    (is (= (charset {:status 200 :headers {"Content-Type" "text/html; charset=UTF-16"}}
                    "UTF-8")
           {:status 200 :headers {"Content-Type" "text/html; charset=UTF-8"}})))
  (testing "default content-type"
    (is (= (charset {:status 200 :headers {}} "UTF-8")
           {:status 200 :headers {"Content-Type" "text/plain; charset=UTF-8"}})))
  (testing "case insensitive"
    (is (= (charset {:status 200 :headers {"content-type" "text/html"}} "UTF-8")
           {:status 200 :headers {"content-type" "text/html; charset=UTF-8"}}))))

(deftest test-get-charset
  (testing "simple charset"
    (is (= (get-charset {:headers {"Content-Type" "text/plain; charset=UTF-8"}})
           "UTF-8")))
  (testing "case insensitive"
    (is (= (get-charset {:headers {"content-type" "text/plain; charset=UTF-16"}})
           "UTF-16")))
  (testing "missing charset"
    (is (nil? (get-charset {:headers {"Content-Type" "text/plain"}}))))
  (testing "missing content-type"
    (is (nil? (get-charset {:headers {}}))))
  (testing "content-type with quoted charset"
    (is (= (get-charset {:headers {"content-type" "text/plain; charset=\"UTF-8\""}})
           "UTF-8"))))

(deftest test-header
  (is (= {:status 200 :headers {"X-Foo" "Bar"}}
         (header {:status 200 :headers {}} "X-Foo" "Bar"))))

(deftest test-response?
  (is (response? {:status 200, :headers {}}))
  (is (response? {:status 200, :headers {}, :body "Foo"}))
  (is (not (response? {})))
  (is (not (response? {:users []}))))

(deftest test-set-cookie
  (is (= {:status 200 :headers {} :cookies {"Foo" {:value "Bar"}}}
         (set-cookie {:status 200 :headers {}}
                     "Foo" "Bar")))
  (is (= {:status 200 :headers {} :cookies {"Foo" {:value "Bar" :http-only true}}}
         (set-cookie {:status 200 :headers {}}
                     "Foo" "Bar" {:http-only true}))))

(deftest test-find-header
  (is (= (find-header {:headers {"Content-Type" "text/plain"}} "Content-Type")
         ["Content-Type" "text/plain"]))
  (is (= (find-header {:headers {"content-type" "text/plain"}} "Content-Type")
         ["content-type" "text/plain"]))
  (is (= (find-header {:headers {"Content-typE" "text/plain"}} "content-type")
         ["Content-typE" "text/plain"]))
  (is (nil? (find-header {:headers {"Content-Type" "text/plain"}} "content-length"))))

(deftest test-get-header
  (is (= (get-header {:headers {"Content-Type" "text/plain"}} "Content-Type")
         "text/plain"))
  (is (= (get-header {:headers {"content-type" "text/plain"}} "Content-Type")
         "text/plain"))
  (is (= (get-header {:headers {"Content-typE" "text/plain"}} "content-type")
         "text/plain"))
  (is (nil? (get-header {:headers {"Content-Type" "text/plain"}} "content-length"))))

(deftest test-update-header
  (is (= (update-header {:headers {"Content-Type" "text/plain"}}
                        "content-type"
                        str "; charset=UTF-8")
         {:headers {"Content-Type" "text/plain; charset=UTF-8"}}))
  (is (= (update-header {:headers {}}
                        "content-type"
                        str "; charset=UTF-8")
         {:headers {"content-type" "; charset=UTF-8"}})))
