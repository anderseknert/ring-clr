(ns ring-clr.util.codec
  "Functions for encoding and decoding data."
  (:require [clojure.string :as str]
            [ring-clr.util.platform :as clr])
  (:import [System Convert Uri]
           [System.Collections IDictionary]
           [System.Globalization NumberStyles]
           [System.Text Encoding]
           [System.Web HttpUtility]))

(defn assoc-conj
  "Associate a key with a value in a map. If the key already exists in the map,
  a vector of values is associated with the key."
  [map key val]
  (assoc map key
         (if-let [cur (get map key)]
           (if (vector? cur)
             (conj cur val)
             [cur val])
           val)))

(defn- double-escape [^String x]
  (-> x
      (str/replace "\\" "\\\\")
      (str/replace "$" "\\$")))

(defn percent-encode
  "Percent-encode every character in the given string using either the specified
  encoding, or UTF-8 by default."
  ([unencoded]
   (percent-encode unencoded "UTF-8"))
  ([^String unencoded ^String encoding]
   (->> (clr/str->bytes unencoded (clr/charset->encoding encoding))
        (map (partial format "%%%02X"))
        (str/join))))

(defn percent-decode
  ([encoded]
   (percent-decode encoded "UTF-8"))
  ([encoded _]
   (Uri/UnescapeDataString encoded)))

(defn url-encode
  "Returns the url-encoded version of the given string, using either a specified
  encoding or UTF-8 by default."
  ([unencoded]
   (url-encode unencoded "UTF-8"))
  ([unencoded encoding]
   (str/replace unencoded #"[^A-Za-z0-9_~.+-]+" #(double-escape (percent-encode % encoding)))))

(defn ^String url-decode
  "Returns the url-decoded version of the given string, using either a specified
  encoding or UTF-8 by default. If the encoding is invalid, nil is returned."
  ([encoded]
   (url-decode encoded "UTF-8"))
  ([encoded encoding]
   (percent-decode encoded encoding)))

(defn base64-encode
  "Encode an array of bytes into a base64 encoded string."
  [^bytes unencoded]
  (Convert/ToBase64String unencoded))

(defn base64-decode
  "Decode a base64 encoded string into an array of bytes."
  [^String encoded]
  (Convert/FromBase64String encoded))

(defprotocol ^:no-doc FormEncodeable
  (form-encode* [x encoding]))

(extend-protocol FormEncodeable
  String
  (form-encode* [unencoded encoding]
    (HttpUtility/UrlEncode unencoded (clr/charset->encoding encoding)))
  IDictionary
  (form-encode* [params encoding]
    (letfn [(encode [x] (form-encode* x encoding))
            (encode-param [k v] (str (encode (name k)) "=" (encode v)))]
      (->> params
           (mapcat
            (fn [[k v]]
              (cond
                (sequential? v) (map #(encode-param k %) v)
                (set? v)        (sort (map #(encode-param k %) v))
                :else           (list (encode-param k v)))))
           (str/join "&"))))
  Object
  (form-encode* [x encoding]
    (form-encode* (str x) encoding))
  nil
  (form-encode* [_ _] ""))

(defn form-encode
  "Encode the supplied value into www-form-urlencoded format, often used in
  URL query strings and POST request bodies, using the specified encoding.
  If the encoding is not specified, it defaults to UTF-8"
  ([x]
   (form-encode x "UTF-8"))
  ([x encoding]
   (form-encode* x encoding)))

(defn form-decode-str
  "Decode the supplied www-form-urlencoded string using the specified encoding,
  or UTF-8 by default."
  ([encoded]
   (form-decode-str encoded "UTF-8"))
  ([^String encoded encoding]
   ; The UrlDecode method doesn't throw on invalid encoding.. so we'll need to try
   ; and identify that ourselves
   (when-not (re-find #"%.{1}$" encoded)
     (try
       (HttpUtility/UrlDecode encoded (clr/charset->encoding (or encoding "UTF-8")))
       (catch Exception e (println (ex-message e)))))))

(defn- nvc->map [clr-map]
  (let [keys (-> clr-map .AllKeys vec)]
    (into {} (map (fn [k]
                    [k (str/join "," (.GetValues clr-map k))]) keys))))  

(defn nil-keys-to-empty [m]
  (into {} (for [[k v] m] (if (nil? k) [v ""] [k v]))))

(defn form-decode
  ([encoded]
   (form-decode encoded "UTF-8"))
  ([^String encoded encoding]
   (if-not (str/includes? encoded "=")
     (HttpUtility/UrlDecode encoded (clr/charset->encoding (or encoding "UTF-8")))
     (-> encoded
         (HttpUtility/ParseQueryString (clr/charset->encoding (or encoding "UTF-8")))
         (nvc->map)
         (nil-keys-to-empty)))))
