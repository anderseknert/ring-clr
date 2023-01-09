(ns ring-clr.request
  (:require [clojure.string :as str])
  (:import [System Environment]
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

(defn ->ring-headers
  [^HttpListenerRequest req]
  (let [headers (.Headers req)
        keys (-> headers .AllKeys vec)]
    (into {} (map (fn [k]
                    [(str/lower-case k) 
                     (str/join "," (.GetValues headers k))]) keys))))

(defn ->ring-request
  [^HttpListenerRequest req]
  {:body            (.InputStream req)
   :headers         (->ring-headers req)
   :protocol        (-> req .ProtocolVersion str ->ring-protocol)
   :query-string    (let [q (-> req .Url .Query)]
                      (when (not= "" q) q))
   :remote-addr     (-> req .RemoteEndPoint .Address str)
   :request-method  (-> req .HttpMethod ->ring-method)
   :scheme          (-> req .Url .Scheme keyword)
   :server-port     (-> req .LocalEndPoint .Port)
   :server-name     Environment/MachineName
   :ssl-client-cert (.GetClientCertificate req)
   :uri             (-> req .Url .AbsolutePath)})
