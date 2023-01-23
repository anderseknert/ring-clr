(ns ring-clr.middleware.file-test
  (:require [clojure.test :refer [deftest is testing]]
            [ring-clr.middleware.file :refer [file-request wrap-file]]
            [ring-clr.util.io :as io])
  (:import [System.IO DirectoryInfo FileInfo]))

(def test-response
  {:status 200, :headers {}, :body "test"})

(deftest wrap-file-no-directory
  (is (thrown-with-msg? Exception #".*Directory does not exist.*"
                        (wrap-file (constantly test-response) "not_here"))))

;(println "file" *file*)
; TODO: This will only work when tests are run from the project root directory
(def project-dir (.FullName (FileInfo. ".")))

(def public-dir (io/->path project-dir "test/ring_clr/assets"))
(def index-html (FileInfo. (io/->path public-dir "index.html")))
(def foo-html   (FileInfo. (io/->path public-dir "foo.html")))

(def app (wrap-file (constantly test-response) (io/->path public-dir)))

(deftest test-wrap-file-unsafe-method
  (is (= test-response (app {:request-method :post :uri "/foo"}))))

(deftest test-wrap-file-forbidden-url
  (is (= test-response (app {:request-method :get :uri "/../foo"}))))

(deftest test-wrap-file-no-file
  (is (= test-response (app {:request-method :get :uri "/dynamic"}))))

(deftest test-wrap-file-directory
  (let [{:keys [status headers body]} (app {:request-method :get :uri "/"})]
    (is (= 200 status))
    (is (= (into #{} (keys headers)) #{"Content-Length" "Last-Modified"}))
    (is (= (.FullName index-html) (.FullName body)))))

(deftest test-wrap-file-with-extension
  (let [{:keys [status headers body]} (app {:request-method :get :uri "/foo.html"})]
    (is (= 200 status))
    (is (= (into #{} (keys headers)) #{"Content-Length" "Last-Modified"}))
    (is (= (.FullName foo-html) (.FullName body)))))

(deftest test-wrap-file-no-index
  (let [app  (wrap-file (constantly test-response) 
                        (io/->path public-dir) 
                        {:index-files? false})
        resp (app {:request-method :get :uri "/"})]
    (is (= test-response resp))))

(deftest test-wrap-file-path-info
  (let [request {:request-method :get
                 :uri "/bar/foo.html"
                 :context "/bar"
                 :path-info "/foo.html"}
        {:keys [status headers body]} (app request)]
    (is (= 200 status))
    (is (= (into #{} (keys headers)) #{"Content-Length" "Last-Modified"}))
    (is (= (.FullName foo-html) (.FullName body)))))

(deftest wrap-file-cps-test
  (let [dynamic-response {:status 200, :headers {}, :body "foo"}
        handler          (-> (fn [_ respond _] (respond dynamic-response))
                             (wrap-file public-dir))]
    (testing "file response"
      (let [request   {:request-method :get :uri "/foo.html"}
            response  (promise)
            exception (promise)]
        (handler request response exception)
        (is (= 200 (:status @response)))
        (is (= #{"Content-Length" "Last-Modified"} (-> @response :headers keys set)))
        (is (= (.FullName foo-html) (.FullName (:body @response))))
        (is (not (realized? exception)))))

    (testing "non-file response"
      (let [request   {:request-method :get :uri "/dynamic"}
            response  (promise)
            exception (promise)]
        (handler request response exception)
        (is (= dynamic-response @response))
        (is (not (realized? exception)))))))

(deftest file-request-test
  (is (fn? file-request)))

(deftest test-head-request
  (let [{:keys [status headers body]} (app {:request-method :head :uri "/foo.html"})]
    (is (= 200 status))
    (is (= (into #{} (keys headers)) #{"Content-Length" "Last-Modified"}))
    (is (nil? body))))

(deftest test-wrap-file-with-java-io-file
  (let [app (wrap-file (constantly :response) (DirectoryInfo. public-dir))]
    (let [{:keys [status headers body]} (app {:request-method :get :uri "/"})]
      (is (= 200 status))
      (is (= (into #{} (keys headers)) #{"Content-Length" "Last-Modified"}))
      (is (=  (.FullName index-html) (.FullName body))))
    (let [{:keys [status headers body]} (app {:request-method :get :uri "/foo.html"})]
      (is (= 200 status))
      (is (= (into #{} (keys headers)) #{"Content-Length" "Last-Modified"}))
      (is (=  (.FullName foo-html) (.FullName body))))))

(defn- prefer-foo-handler
  ([request]
   (if (= (:uri request) "/foo.html")
     {:status 200, :headers {}, :body "override"}
     {:status 404, :headers {}, :body "not found"}))
  ([request respond _]
   (respond (prefer-foo-handler request))))

(deftest test-wrap-file-with-prefer-handler
  (let [handler (wrap-file prefer-foo-handler
                           (DirectoryInfo. public-dir)
                           {:prefer-handler? true})]

    (testing "middleware serves file (synchronously)"
      (let [response (handler {:request-method :get, :uri "/index.html"})]
        (is (= 200 (:status response)))
        (is (= (.FullName index-html) (.FullName (:body response))))))

    (testing "middleware serves file (asynchronously)"
      (let [response (promise)
            error    (promise)]
        (handler {:request-method :get, :uri "/index.html"} response error)
        (is (= 200 (:status @response)))
        (is (= (.FullName index-html) (.FullName (:body @response))))
        (is (not (realized? error)))))

    (testing "handler serves file (synchronously)"
      (let [response (handler {:request-method :get, :uri "/foo.html"})]
        (is (= 200 (:status response)))
        (is (= "override" (:body response)))))

    (testing "handler serves file (asynchronously)"
      (let [response (promise)
            error    (promise)]
        (handler {:request-method :get, :uri "/foo.html"} response error)
        (is (= 200 (:status @response)))
        (is (= "override" (:body @response)))
        (is (not (realized? error)))))))
