(ns ring-clr.util.request
  (:require [clojure.string :as str])
  (:import 
           [System.Net HttpListenerRequest]))

(defn ->ring-protocol
  [protocol-version]
  (case protocol-version
    "1.0" "HTTP/1.0"
    "1.1" "HTTP/1.1"
    "2.0" "HTTP/2"))

(defn ->ring-method
  [method]
  (case method
    "GET"     :get
    "POST"    :post
    "PUT"     :put
    "DELETE"  :delete
    "HEAD"    :head
    "OPTIONS" :options
    "TRACE"   :trace
    "PATCH"   :patch
    (keyword (str/lower-case method))))

