(ns ring-clr.middleware.content-length
  (:require [ring-clr.util.response :as response])
  (:import [System.IO FileInfo Stream]))

(defn- content-length [response length]
  (cond-> response
    (nil? (response/get-header response "content-length"))
    (response/content-length length)))

(defprotocol ContentLengthMeasurable
  "A protocol for for determining and setting appropriate content-length header.
   Note that this is not present in Ring for the JVM, likely because Jetty (and
   other web servers) do this for you."
  (set-content-length [body response]
    "Unless previously set, add content-length response header for the size of
     data."))

(extend-protocol ContentLengthMeasurable
  (Type/GetType "System.Byte[]")
  (set-content-length [body response]
    (content-length response (count body)))
  String
  (set-content-length [body response]
    (content-length response (count body)))
  clojure.lang.ISeq
  (set-content-length [body response]
    (content-length response (reduce (fn [acc x] (+ acc (count x))) 0 body)))
  Stream
  (set-content-length [^bytes body response]
    (cond-> response (.CanSeek body) (response/content-length (.Length body))))
  FileInfo
  (set-content-length [^bytes body response]
    (response/content-length response (.Length body)))
  nil
  (set-content-length [_ response] response))

(defn wrap-content-length
  "Middleware that adds a content-length header to the response if one is not
  set by the handler."
  ([handler]
   (fn
     ([request]
      (let [response (handler request)]
        (set-content-length (:body response) response)))
     ([request respond raise]
      (handler request
               (fn [response] (respond (set-content-length (:body request) response)))
               raise)))))