(ns ring-clr.core.protocols
  (:import [System.IO StreamWriter Stream])
  (:require [ring-clr.util.response :as response]
            [ring-clr.util.platform :as platform])) ; TODO: move to codec

(defprotocol StreamableResponseBody
  "A protocol for writing data to the response body via an output stream."
  (write-body-to-stream [body response output-stream]
    "Write a value representing a response body to an output stream. The stream
    will be closed after the value had been written."))

(defn- response-writer ^StreamWriter [response ^Stream output-stream]
  (if-let [charset (response/get-charset response)]
    (StreamWriter. output-stream (platform/charset->encoding charset))
    (StreamWriter. output-stream)))

(extend-protocol StreamableResponseBody
;;   (Class/forName "[B")
;;   (write-body-to-stream [body _ ^OutputStream output-stream]
;;     (.write output-stream ^bytes body)
;;     (.close output-stream))
  String
  (write-body-to-stream [body response output-stream]
    (doto (response-writer response output-stream)
      (.Write body)
      (.Close)))
;;   clojure.lang.ISeq
;;   (write-body-to-stream [body response output-stream]
;;     (let [writer (response-writer response output-stream)]
;;       (doseq [chunk body]
;;         (.write writer (str chunk)))
;;       (.close writer)))
;;   java.io.InputStream
;;   (write-body-to-stream [body _ ^OutputStream output-stream]
;;     (with-open [body body]
;;       (io/copy body output-stream))
;;     (.close output-stream))
;;   java.io.File
;;   (write-body-to-stream [body _ ^OutputStream output-stream]
;;     (io/copy body output-stream)
;;     (.close output-stream))
;;   nil
;;   (write-body-to-stream [_ _ ^java.io.OutputStream output-stream]
;;     (.close output-stream))
  )