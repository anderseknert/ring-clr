(ns ring-clr.util.request
  "Functions for augmenting and pulling information from request maps."
  (:require [clojure.string :as str]
            [ring-clr.util.parsing :as parsing])
  (:import  [System.IO FileInfo Stream]))

(defn ->ring-protocol
  [protocol-version]
  (case protocol-version
    "1.0" "HTTP/1.0"
    "1.1" "HTTP/1.1"
    "2.0" "HTTP/2"))

(defn ->ring-method
  [method]
  (case method
    "GET"     :get
    "POST"    :post
    "PUT"     :put
    "DELETE"  :delete
    "HEAD"    :head
    "OPTIONS" :options
    "TRACE"   :trace
    "PATCH"   :patch
    (keyword (str/lower-case method))))

(defn request-url
  "Return the full URL of the request."
  [request]
  (str (-> request :scheme name)
       "://"
       (get-in request [:headers "host"])
       (:uri request)
       (when-let [query (:query-string request)]
         (str "?" query))))

(defn content-type
  "Return the content-type of the request, or nil if no content-type is set."
  [request]
  (when-let [type (get (:headers request) "content-type")]
    (if-let [i (str/index-of type ";")]
      (subs type 0 i)
      type)))

(defn content-length
  "Return the content-length of the request, or nil no content-length is set."
  [request]
  (when-let [^String length (get-in request [:headers "content-length"])]
    (parse-long length)))

(defn character-encoding
  "Return the character encoding for the request, or nil if it is not set."
  [request]
  (some-> (get-in request [:headers "content-type"])
          parsing/find-content-type-charset))

(defn urlencoded-form?
  "True if a request contains a urlencoded form in the body."
  [request]
  (when-let [^String type (content-type request)]
    (str/starts-with? type "application/x-www-form-urlencoded")))

(defmulti ^String body-string
  "Return the request body as a string."
  {:arglists '([request])}
  (comp class :body))

(defmethod body-string nil [_] nil)

(defmethod body-string String [request]
  (:body request))

(defmethod body-string clojure.lang.ISeq [request]
  (apply str (:body request)))

(defmethod body-string FileInfo [request]
  (slurp (:body request)))

(defmethod body-string Stream [request]
  (slurp (:body request)))

(defn path-info
  "Returns the relative path of the request."
  [request]
  (or (:path-info request)
      (:uri request)))

(defn in-context?
  "Returns true if the URI of the request is a subpath of the supplied context."
  [request context]
  (str/starts-with? ^String (:uri request) context))

(defn set-context
  "Associate a context and path-info with the request. The request URI must be
  a subpath of the supplied context."
  [request ^String context]
  {:pre [(in-context? request context)]}
  (assoc request
         :context context
         :path-info (subs (:uri request) (count context))))


