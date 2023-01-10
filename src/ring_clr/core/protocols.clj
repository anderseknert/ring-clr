(ns ring-clr.core.protocols
  (:import [System.IO BinaryWriter StreamWriter Stream])
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
  Byte[]
  (write-body-to-stream [body _ ^Stream output-stream]
    (with-open [binary-writer (BinaryWriter. output-stream)]
      (.Write binary-writer ^Byte[] body)))
  String
  (write-body-to-stream [body response output-stream]
    (with-open [writer (response-writer response output-stream)]
      (.Write writer body)))
  clojure.lang.ISeq
  (write-body-to-stream [body response output-stream]
    (with-open [writer (response-writer response output-stream)]
      (doseq [chunk body]
        (.Write writer (str chunk)))))
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