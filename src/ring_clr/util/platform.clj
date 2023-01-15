(ns ring-clr.util.platform
  (:require [clojure.string :as str])
  (:import [System.Text Encoding]
           [System.IO File MemoryStream Path SeekOrigin StreamReader]))

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

(defn str->memory-stream [s]
  (let [stream (MemoryStream.)]
    (.Write stream (str->bytes s))
    (.Seek stream 0 SeekOrigin/Begin)))

(defn stream->str [stream]
  (when (instance? MemoryStream stream)
    (.Seek stream 0 SeekOrigin/Begin))
  (with-open [reader (StreamReader. stream Encoding/UTF8)]
    (.ReadToEnd reader)))


(defn tmp-file-path []
  (Path/GetTempFileName))

(defn file-create [path]
  (File/Create path))

(defn file-read [path]
  (File/OpenRead path))


