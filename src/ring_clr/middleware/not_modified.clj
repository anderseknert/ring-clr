(ns ring-clr.middleware.not-modified
  "Middleware that returns a 304 Not Modified response for responses with
  Last-Modified headers."
  (:require [ring-clr.util.time :refer [parse-date]]
            [ring-clr.util.response :refer [get-header header]]
            [ring-clr.util.io :refer [close!]])
  (:import [System DateTime]))

(defn- etag-match? [request response]
  (when-let [etag (get-header response "ETag")]
    (= etag (get-header request "if-none-match"))))

(defn- date-header ^DateTime [response header]
  (when-let [http-date (get-header response header)]
    (parse-date http-date)))

(defn- not-modified-since? [request response]
  (let [modified-date  (date-header response "Last-Modified")
        modified-since (date-header request "if-modified-since")]
    (and modified-date
         modified-since
         (>= (.CompareTo modified-since modified-date) 0))))

(defn- read-request? [request]
  (#{:get :head} (:request-method request)))

(defn- ok-response? [response]
  (= (:status response) 200))

(defn- cached-response? [request response]
  (let [modified-since (get-header request "if-modified-since")
        if-none-match  (get-header request "if-none-match")]
    (if (and modified-since if-none-match)
      (and (not-modified-since? request response)
           (etag-match? request response))
      (or (not-modified-since? request response)
          (etag-match? request response)))))

(defn not-modified-response
  "Returns 304 or original response based on response and request.
  See: wrap-not-modified."
  [response request]
  (if (and (read-request? request)
           (ok-response? response)
           (cached-response? request response))
    (do (close! (:body response))
        (-> response
            (assoc :status 304)
            (header "Content-Length" 0)
            (assoc :body nil)))
    response))

(defn wrap-not-modified
  "Middleware that returns a 304 Not Modified from the wrapped handler if the
  handler response has an ETag or Last-Modified header, and the request has a
  If-None-Match or If-Modified-Since header that matches the response."
  [handler]
  (fn
    ([request]
     (-> (handler request) (not-modified-response request)))
    ([request respond raise]
     (handler request
              (fn [response] (respond (not-modified-response response request)))
              raise))))
