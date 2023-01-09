(ns main
  (:require [clojure.pprint :as pp]
            [ring-clr.core.protocols :as protocols]
            [ring-clr.request :as rr]
            [ring-clr.response :as rp])
  (:import [System.Net HttpListener HttpListenerException]))

(def running (atom false))

(def listener (HttpListener.))
(-> listener .Prefixes (.Add "http://localhost:8100/"))

(defn serve []
  (println "starting to serve...")
  (while @running
    (try
      (let [context (.GetContext listener)]
        @(future
          (let [req (.Request context)
                res (.Response context)]

            (pp/pprint (rr/->ring-request req))

            (rp/set-status! res 200)
            (rp/set-headers! res {"foo" "bar" "baz" ["a" "b" "c"]})

            (protocols/write-body-to-stream "fåoubar" res (.OutputStream res))

            )))
      (catch HttpListenerException _
        (println "listener stopped"))
      (catch Exception e
        (println e)
        (println (ex-message e)))))
  (println "stopped serving"))

(defn start []
  (.Start listener)
  (swap! running not)
  (serve))

(defn stop []
  (swap! running not)
  (.Stop listener))

(defn -main []
  (start))

