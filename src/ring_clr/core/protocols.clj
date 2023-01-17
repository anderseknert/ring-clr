(ns ring-clr.core.protocols
  (:import [System.IO FileInfo StreamWriter Stream])
  (:require [clojure.clr.io :as io]
            [ring-clr.util.response :as response]
            [ring-clr.util.platform :as clr])) ; TODO: move to codec

(defprotocol StreamableResponseBody
  "A protocol for writing data to the response body via an output stream."
  (write-body-to-stream [body response output-stream]
    "Write a value representing a response body to an output stream. The stream
    will be closed after the value had been written."))

(defn- response-writer ^StreamWriter [response ^Stream output-stream]
  (if-let [charset (response/get-charset response)]
    (StreamWriter. output-stream (clr/charset->encoding charset))
    (StreamWriter. output-stream)))

(extend-protocol StreamableResponseBody
  (Type/GetType "System.Byte[]")
  (write-body-to-stream [^bytes body _ ^Stream output-stream]
    (with-open [output-stream output-stream]
      (.Write output-stream body 0 (count body))))
  String
  (write-body-to-stream [body response output-stream]
    (with-open [writer (response-writer response output-stream)]
      (.Write writer body)))
  clojure.lang.ISeq
  (write-body-to-stream [body response output-stream]
    (with-open [writer (response-writer response output-stream)]
      (doseq [chunk body]
        (.Write writer (str chunk)))))
  Stream
  (write-body-to-stream [^Stream body _ ^Stream output-stream]
    (with-open [input-stream  body
                output-stream output-stream]
      (io/copy input-stream output-stream)))
  FileInfo
  (write-body-to-stream [body _ ^Stream output-stream]
    (with-open [output-stream output-stream]
      (io/copy body output-stream)))
  nil
  (write-body-to-stream [_ _ ^Stream output-stream]
    (.Close output-stream)))