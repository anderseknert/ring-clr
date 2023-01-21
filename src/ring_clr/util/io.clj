(ns ring-clr.util.io
  "Utility functions for handling I/O."
  (:require [ring-clr.util.platform :as clr])
  (:import [System IDisposable]
           [System.IO MemoryStream]
           [System.Text Encoding]))

;; (defn piped-input-stream
;;   "Create an input stream from a function that takes an output stream as its
;;   argument. The function will be executed in a separate thread. The stream
;;   will be automatically closed after the function finishes.

;;   For example:

;;     (piped-input-stream
;;       (fn [ostream]
;;         (spit ostream \"Hello\")))"
;;   {:added "1.1"}
;;   [func]
;;   (let [input  (PipedInputStream.)
;;         output (PipedOutputStream.)]
;;     (.connect input output)
;;     (future
;;       (try
;;         (func output)
;;         (finally (.close output))))
;;     input))

(defn string-input-stream
  "Returns a MemoryStream for the given String."
  ([s]
   (string-input-stream s "UTF-8"))
  ([s encoding]
   (MemoryStream. (.GetBytes (clr/charset->encoding encoding) s))))

(defn close!
  "Ensure a stream is closed, swallowing any exceptions."
  ; Note: .Dispose doesn't throw..
  [stream]
  (when (instance? IDisposable stream)
    (.Dispose stream)))

;; (defn last-modified-date
;;   "Returns the last modified date for a file, rounded down to the nearest
;;   second."
;;   {:added "1.2"}
;;   [^File file]
;;   (-> (.lastModified file)
;;       (/ 1000) (long) (* 1000)
;;       (java.util.Date.)))