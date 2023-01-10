(ns ring-clr.util.parsing
  "Regular expressions for parsing HTTP. For internal use."
  (:require [clojure.string :as str]))

(def ^{:doc "HTTP token: 1*<any CHAR except CTLs or tspecials>. See RFC2068"}
  re-token
  #"[!#$%&'*\-+.0-9A-Z\^_`a-z\|~]+")

(def ^{:doc "HTTP quoted-string: <\"> *<any TEXT except \"> <\">. See RFC2068."}
  re-quoted
  #"\"((?:\\\"|[^\"])*)\"")

(def ^{:doc "HTTP value: token | quoted-string. See RFC2109"}
  re-value
  (str "(" re-token ")|" re-quoted))

(def ^{:doc "Pattern for pulling the charset out of the content-type header"}
  re-charset
  (re-pattern (str ";?charset=(" re-value ");?")))

(defn find-content-type-charset
  "Return the charset of a given a content-type string."
  [s]
  (when-let [m (re-find re-charset s)]
    (let [m (mapv #(str/replace % #"\"" "") m)]
      (or (m 1) (m 2)))))