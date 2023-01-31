(ns app
  (:require [ring-clr.adapter.httplistener :as server]))

(defn handler [request]
  {:status 200
   :headers {"content-type" "application/json"}
   :body "{\"hello\":\"world\"}"})

(defn -main []
  (server/run-httplistener handler))