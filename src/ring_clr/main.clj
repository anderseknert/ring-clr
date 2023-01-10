(ns ring-clr.main
  (:require [clojure.pprint :as pp]
            [ring-clr.adapter.httplistener :as httplistener]))

(defn handler [request]
  (pp/pprint request)
  {:status 200
   :headers {"content-type" "application/json"}
   :body "{\"foo\":\"bar\"}"})

(defn -main []
  (httplistener/run-httplistener handler))

