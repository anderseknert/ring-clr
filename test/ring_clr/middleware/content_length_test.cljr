(ns ring-clr.middleware.content-length-test
  (:require [clojure.test :refer [deftest is testing]]
            [ring-clr.util.platform :as clr]
            [ring-clr.middleware.content-length :refer [wrap-content-length]])
  (:import  [System.IO FileInfo]))

(deftest wrap-content-length-test
  (testing "response with content-length left unchanged"
    (let [request  {}
          response {:body "foo" :headers {"Content-Length" "123"}}
          handler  (wrap-content-length (constantly response))]
      (is (= (handler request)
             {:body "foo" :headers {"Content-Length" "123"}}))))

  (testing "string response without content-length"
    (let [request  {}
          response {:body "foobar" :headers {}}
          handler  (wrap-content-length (constantly response))]
      (is (= (handler request)
             {:body "foobar" :headers {"Content-Length" "6"}}))))

  (testing "seq response without content-length"
    (let [request  {}
          response {:body (list "hello" " " "world") :headers {}}
          handler  (wrap-content-length (constantly response))]
      (is (= (handler request)
             {:body (list "hello" " " "world") :headers {"Content-Length" "11"}}))))
  
  (testing "stream response without content-length"
    (let [request  {}
          stream   (clr/str->memory-stream "foobar")
          response {:body stream :headers {}}
          handler  (wrap-content-length (constantly response))]
      (is (= (handler request)
             {:body stream :headers {"Content-Length" "6"}}))))

  (testing "file response without content-length"
    (let [request  {}
          file     (FileInfo. (clr/tmp-file-path))
          _        (spit file "foobar in file")
          response {:body file :headers {}}
          handler  (wrap-content-length (constantly response))]
      (is (= (handler request)
             {:body file :headers {"Content-Length" "14"}}))))

  (testing "nil response without content-length"
    (let [request  {}
          response {:body nil :headers {}}
          handler  (wrap-content-length (constantly response))]
      (is (= (handler request)
             {:body nil :headers {}})))))
    
  

;;   (testing "response with content-type"
;;     (let [response {:headers {"Content-Type" "application/x-foo"}}
;;           handler (wrap-content-type (constantly response))]
;;       (is (= (handler {:uri "/foo/bar.png"})
;;              {:headers {"Content-Type" "application/x-foo"}}))))

;;   (testing "unknown file extension"
;;     (let [response {:headers {}}
;;           handler  (wrap-content-type (constantly response))]
;;       (is (= (handler {:uri "/foo/bar.xxxaaa"})
;;              {:headers {"Content-Type" "application/octet-stream"}}))
;;       (is (= (handler {:uri "/foo/bar"})
;;              {:headers {"Content-Type" "application/octet-stream"}}))))

;;   (testing "response with mime-types option"
;;     (let [response {:headers {}}
;;           handler  (wrap-content-type (constantly response) {:mime-types {"edn" "application/edn"}})]
;;       (is (= (handler {:uri "/all.edn"})
;;              {:headers {"Content-Type" "application/edn"}}))))

;;   (testing "nil response"
;;     (let [handler (wrap-content-type (constantly nil))]
;;       (is (nil? (handler {:uri "/foo/bar.txt"})))))

;;   (testing "response header case insensitivity"
;;     (let [response {:headers {"CoNteNt-typE" "application/x-overridden"}}
;;           handler (wrap-content-type (constantly response))]
;;       (is (= (handler {:uri "/foo/bar.png"})
;;              {:headers {"CoNteNt-typE" "application/x-overridden"}})))))

;; (deftest wrap-content-type-cps-test
;;   (testing "response without content-type"
;;     (let [handler   (wrap-content-type (fn [_ respond _] (respond {:headers {}})))
;;           response  (promise)
;;           exception (promise)]
;;       (handler {:uri "/foo/bar.png"} response exception)
;;       (is (= @response {:headers {"Content-Type" "image/png"}}))
;;       (is (not (realized? exception)))))

;;   (testing "nil response"
;;     (let [handler   (wrap-content-type (fn [_ respond _] (respond nil)))
;;           response  (promise)
;;           exception (promise)]
;;       (handler {:uri "/foo/bar.png"} response exception)
;;       (is (nil? @response))
;;       (is (not (realized? exception))))))

;; (deftest content-type-response-test
;;   (testing "function exists"
;;     (is (fn? content-type-response)))

;;   (testing "nil response"
;;     (is (nil? (content-type-response nil {})))))