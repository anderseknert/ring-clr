(ns ring-clr.util.response
  "Functions for generating and augmenting response maps."
  (:require [clojure.string :as str]
            [ring-clr.util.parsing :as parsing])
  (:import  [System StringComparison]))

(def redirect-status-codes
  "Map a keyword to a redirect status code."
  {:moved-permanently 301
   :found 302
   :see-other 303
   :temporary-redirect 307
   :permanent-redirect 308})

(defn redirect
  "Returns a Ring response for an HTTP 302 redirect. Status may be 
  a key in redirect-status-codes or a numeric code. Defaults to 302"
  ([url] (redirect url :found))
  ([url status]
   {:status  (redirect-status-codes status status)
    :headers {"Location" url}
    :body    ""}))

(defn created
  "Returns a Ring response for a HTTP 201 created response."
  ([url] (created url nil))
  ([url body]
   {:status  201
    :headers {"Location" url}
    :body    body}))

(defn bad-request
  "Returns a 400 'bad request' response."
  [body]
  {:status  400
   :headers {}
   :body    body})

(defn not-found
  "Returns a 404 'not found' response."
  [body]
  {:status  404
   :headers {}
   :body    body})

(defn response
  "Returns a skeletal Ring response with the given body, status of 200, and no
  headers."
  [body]
  {:status  200
   :headers {}
   :body    body})

(defn status
  "Returns an updated Ring response with the given status."
  ([status]
   {:status  status
    :headers {}
    :body    nil})
  ([resp status]
   (assoc resp :status status)))

(defn header
  "Returns an updated Ring response with the specified header added."
  [resp name value]
  (assoc-in resp [:headers name] (str value)))

(defn content-type
  "Returns an updated Ring response with the a Content-Type header corresponding
  to the given content-type."
  [resp content-type]
  (header resp "Content-Type" content-type))

(defn find-header
  "Looks up a header in a Ring response (or request) case insensitively,
  returning the header map entry, or nil if not present."
  [resp ^String header-name]
  (->> (:headers resp)
       (filter #(.Equals header-name (key %) StringComparison/OrdinalIgnoreCase))
       (first)))

(defn get-header
  "Looks up a header in a Ring response (or request) case insensitively,
  returning the value of the header, or nil if not present."
  [resp header-name]
  (some-> resp (find-header header-name) val))

(defn update-header
  "Looks up a header in a Ring response (or request) case insensitively,
  then updates the header with the supplied function and arguments in the
  manner of update-in."
  [resp header-name f & args]
  (let [header-key (or (some-> resp (find-header header-name) key) header-name)]
    (update-in resp [:headers header-key] #(apply f % args))))

(defn charset
  "Returns an updated Ring response with the supplied charset added to the
  Content-Type header."
  [resp charset]
  (update-header resp "Content-Type"
                 (fn [content-type]
                   (-> (or content-type "text/plain")
                       (str/replace #";\s*charset=[^;]*" "")
                       (str "; charset=" charset)))))

(defn get-charset
  "Gets the character encoding of a Ring response."
  [resp]
  (some-> (get-header resp "Content-Type") parsing/find-content-type-charset))

(defn set-cookie
  "Sets a cookie on the response. Requires the handler to be wrapped in the
  wrap-cookies middleware."
  {:added "1.1"}
  [resp name value & [opts]]
  (assoc-in resp [:cookies name] (merge {:value value} opts)))

(defn response?
  "True if the supplied value is a valid response map."
  {:added "1.1"}
  [resp]
  (and (map? resp)
       (integer? (:status resp))
       (map? (:headers resp))))
