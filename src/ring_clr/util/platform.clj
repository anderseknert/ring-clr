(ns ring-clr.util.platform
  (:require [clojure.string :as str])
  (:import [System.Text Encoding]
           [System.IO File MemoryStream Path StreamReader]))

(defn charset->encoding
  [charset]
  (case (str/lower-case charset)
    "ascii"      Encoding/ASCII
    "iso-8859-1" Encoding/Latin1
    "utf-7"      Encoding/UTF7
    "utf-32"     Encoding/UTF32
    Encoding/UTF8))

(defn str->bytes
  ([s]
   (str->bytes s Encoding/UTF8))
  ([s encoding]
   (.GetBytes encoding s)))

(defn bytes->str
  ([b]
   (bytes->str b Encoding/UTF8))
  ([b encoding]
   (.GetString encoding b)))

(defn empty-memory-stream 
  ([]
   (MemoryStream.))
  ([x]
   (cond
     (string? x)    (empty-memory-stream (str->bytes x))
     (bytes? x)     (MemoryStream. (count x))
     :else          (println "unhandled type" (type x) "with value" x))))

(defn reset-stream! [stream]
  (set! (. stream Position) 0)
  stream)

(defn stream->str [stream]
  (if (instance? MemoryStream stream)
    (bytes->str (.GetBuffer stream))
    (with-open [reader (StreamReader. stream Encoding/UTF8)]
      (.ReadToEnd reader))))

(defn memory-stream->str-safe [stream]
  (bytes->str (.ToArray stream)))

(defn tmp-file-path []
  (Path/GetTempFileName))

(defn file-create [path]
  (File/Create path))

(defn file-read [path]
  (File/OpenRead path))


