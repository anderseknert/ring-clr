(ns ring-clr.util.time
  "Functions for dealing with time and dates in HTTP requests."
  (:require [clojure.string :as str])
  (:import  [System DateTime FormatException]
            [System.Globalization CultureInfo]))

(def ^:no-doc http-date-formats
  {:rfc1123 "ddd, dd MMM yyyy HH':'mm':'ss 'GMT'" ; "EEE, dd MMM yyyy HH:mm:ss zzz"
   ;:rfc1036 "EEEE, dd-MMM-yy HH:mm:ss zzz"
   ;:asctime "EEE MMM d HH:mm:ss yyyy"
   })

(defn- attempt-parse [date format]
  (println date format)
  (try
    (DateTime/ParseExact date (get http-date-formats format) CultureInfo/InvariantCulture)
    (catch FormatException _ nil)))

(defn- trim-quotes [s]
  (str/replace s #"^'|'$" ""))

(defn parse-date
  "Attempt to parse a HTTP date. Returns nil if unsuccessful."
  [http-date]
  (->> (keys http-date-formats)
       (map (partial attempt-parse (trim-quotes http-date)))
       (remove nil?)
       (first)))

(defn format-date
  "Format a date as RFC1123 format."
  [^DateTime date]
  (DateTime/ParseExact date (:rfc1123 http-date-formats) CultureInfo/InvariantCulture))