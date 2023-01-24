(ns ring-clr.adapter.httplistener
  "A Ring adapter that uses the HttpListener web server.
 
   Adapters are used to convert Ring handlers into running web servers."
  (:require [clojure.string :as str]
            [ring-clr.core.protocols :as protocols]
            [ring-clr.middleware.content-length :as content-length]
            [ring-clr.util.request :as request])
  (:import [System Environment]
           [System.Net HttpListener HttpListenerRequest HttpListenerResponse HttpListenerException]))

(def running (atom false))

(defn- ->ring-headers
[^HttpListenerRequest request]
  (let [headers (.Headers request)
        keys (-> headers .AllKeys vec)]
    (into {} (map (fn [k]
                    [(str/lower-case k)
                     (str/join "," (.GetValues headers k))]) keys))))

(defn- ->ring-request
  [^HttpListenerRequest request]
  {:body            (.InputStream request)
   :headers         (->ring-headers request)
   :protocol        (-> request .ProtocolVersion str request/->ring-protocol)
   :query-string    (let [q (-> request .Url .Query)]
                      (when (not= "" q) q))
   :remote-addr     (-> request .RemoteEndPoint .Address str)
   :request-method  (-> request .HttpMethod request/->ring-method)
   :scheme          (-> request .Url .Scheme keyword)
   :server-port     (-> request .LocalEndPoint .Port)
   :server-name     Environment/MachineName
   :ssl-client-cert (.GetClientCertificate request)
   :uri             (-> request .Url .AbsolutePath)})

(defn- status!
  [^HttpListenerResponse response response-map]
  (set! (. response StatusCode) (int (:status response-map)))
  response)

(defn- headers!
  [^HttpListenerResponse response response-map]
  (run! (fn [[k v]]
          (if (string? v)
            (.AppendHeader response k v)
            (run! #(.AppendHeader response k %) v)))
        (:headers response-map))
  response)

(defn- body!
  [^HttpListenerResponse response response-map]
  (protocols/write-body-to-stream (:body response-map) response-map (.OutputStream response)))

(defn serve [^HttpListener listener handler]
  (while @running
    (try
      (let [context (.GetContext listener)]
        (future
          (let [response     (.Response context)
                request-map  (-> context .Request ->ring-request)
                response-map (handler request-map)]
            (-> response
                (status!  response-map)
                (headers! response-map)
                (body!    response-map)))))
      (catch HttpListenerException _)
      (catch Exception e
        (println e)))))

(defn start [^HttpListener listener]
  (.Start listener)
  (swap! running (constantly true)))

(defn stop [^HttpListener listener]
  (when (.IsListening listener)
    (.Stop listener))
  (swap! running (constantly false)))

(defn- create-listener [{:keys [host port] :or {host "localhost" port 8000}}]
  (let [listener (HttpListener.)
        host-port (format "http://%s:%d/" host port)]
    (-> listener .Prefixes (.Add host-port))
    (println "Listening on" host-port)
    listener))

; TODO: ssl, custom thread pool, async.. more :)
(defn run-httplistener
  "Start a HttpListener webserver to serve the given handler according to the
  supplied options:
  :configurator           - a function called with the Jetty Server instance
  :port                   - the port to listen on (defaults to 8000)
  :host                   - the hostname to listen on (defaults to localhost)"
  ([handler]
   (run-httplistener handler {}))
  ([handler options]
   (let [listener (create-listener options)
         handler (content-length/wrap-content-length handler)]
     (when-let [configurator (:configurator options)]
       (configurator listener))
     (try
       (start listener)
       (serve listener handler)
       listener
       (catch Exception e
         (stop listener)
         (throw e))))))