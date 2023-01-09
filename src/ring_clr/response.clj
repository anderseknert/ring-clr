(ns ring-clr.response
  (:require [clojure.string :as str])
  (:import [System.Net HttpListenerResponse]))

(defn set-status!
  [^HttpListenerResponse res status]
  (set! (. res StatusCode) (int status))
  res)

(defn set-headers!
  [^HttpListenerResponse res headers]
  (run! (fn [[k v]]
          (if (string? v)
            (.AppendHeader res k v)
            (run! #(.AppendHeader res k %) v)))
        headers)
  res)