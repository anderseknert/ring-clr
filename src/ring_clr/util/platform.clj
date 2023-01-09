(ns ring-clr.util.platform
  (:require [clojure.string :as str])
  (:import (System.Text Encoding)))

(defn charset->encoding
  [charset]
  (case (str/lower-case charset)
    "ascii"      Encoding/ASCII
    "iso-8859-1" Encoding/Latin1
    "utf-7"      Encoding/UTF7
    ;"utf-16"     Encoding/UTF16
    "utf-32"     Encoding/UTF32
    Encoding/UTF8))

(defn str->bytes
  ([s]
   (str->bytes s Encoding/UTF8))
  ([s encoding]
   (.GetBytes encoding s)))
