(ns ring-clr.util.response
  "Functions for generating and augmenting response maps."
  (:require [clojure.string :as str]
            [ring-clr.util.io :as io]
            [ring-clr.util.parsing :as parsing]
            [ring-clr.util.time :as time])
  (:import  [System StringComparison]
            [System.IO DirectoryInfo FileInfo FileSystemInfo]))

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
  [resp name value & [opts]]
  (assoc-in resp [:cookies name] (merge {:value value} opts)))

(defn response?
  "True if the supplied value is a valid response map."
  [resp]
  (and (map? resp)
       (integer? (:status resp))
       (map? (:headers resp))))

(defn- canonical-path ^String [^FileSystemInfo file]
  (io/->canonical-path (.FullName file)))

(defn- safe-path? [^String root ^String path]
  (str/starts-with? (io/->canonical-path (io/->path root path))
                    (io/->canonical-path root)))

(defn- directory-transversal?
  "Check if a path contains '..'."
  [^String path]
  (-> (str/split path #"/|\\")
      (set)
      (contains? "..")))

(defn- find-file-named [^DirectoryInfo dir ^String filename]
  (let [path (FileInfo. (io/->path (.FullName dir) filename))]
    (when (.Exists path)
      path)))

(defn- find-file-starting-with [^DirectoryInfo dir ^String prefix]
  (first
   (filter
    #(str/starts-with? (str/lower-case (.Name ^FileInfo %)) prefix)
    (.GetFiles dir))))

(defn- find-index-file
  "Search the directory for an index file."
  [^DirectoryInfo dir]
  (or (find-file-named dir "index.html")
      (find-file-named dir "index.htm")
      (find-file-starting-with dir "index.")))

(defn- safely-find-file [^String path opts]
  (if-let [^String root (:root opts)]
    (when (or (safe-path? root path)
            (and (:allow-symlinks? opts) (not (directory-transversal? path))))
      (io/->file-system-info (io/->path root path)))
    (io/->file-system-info path)))

(defn- find-file [^String path opts]
  (when-let [^FileSystemInfo file (safely-find-file path opts)]
    (cond
      (io/directory? file)
      (and (:index-files? opts true) (find-index-file file))
      (.Exists file)
      file)))

(defn- file-data [^FileInfo file]
  {:content        file
   :content-length (.Length file)
   :last-modified  (io/last-modified-date file)})

(defn content-length [resp len]
  (if len
    (header resp "Content-Length" len)
    resp))

(defn- last-modified [resp last-mod]
  (if last-mod
    (header resp "Last-Modified" (time/format-date last-mod))
    resp))

(defn file-response
  "Returns a Ring response to serve a static file, or nil if an appropriate
  file does not exist.
  Options:
    :root            - take the filepath relative to this root path
    :index-files?    - look for index.* files in directories (defaults to true)
    :allow-symlinks? - allow symlinks that lead to paths outside the root path
                       (defaults to false)"
  ([filepath]
   (file-response filepath {}))
  ([filepath options]
   (when-let [file (find-file filepath options)]
     (let [data (file-data file)]
       (-> (response (:content data))
           (content-length (:content-length data))
           (last-modified (:last-modified data)))))))