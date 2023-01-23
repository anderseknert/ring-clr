(ns ring-clr.util.io
  "Utility functions for handling I/O."
  (:require [clojure.string :as str]
            [ring-clr.util.platform :as clr])
  (:import [System IDisposable Uri]
           [System.IO DirectoryInfo File FileAttributes FileInfo FileSystemInfo MemoryStream Path]))

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

(defn last-modified-date
  "Returns the last modified date for a file, rounded down to the nearest
  second."
  [^FileInfo file]
  (-> file (.FullName) (File/GetLastWriteTime)))

; TODO: Move these additions elsewhere

(defn ->path [& parts]
  (reduce #(Path/Join %1 %2) "" parts))

"/Users/anderseknert/git/dotnet/ring-clr/test/ring_clr/assets/index.html"
"/Users/anderseknert/git/dotnet/ring-clr/test/ring_clr/assets/index.html"

(defn ->canonical-path [s]
  (-> (Uri. s)
      (.LocalPath)
      (Path/GetFullPath)
      (.TrimEnd Path/DirectorySeparatorChar)
      (.TrimEnd Path/AltDirectorySeparatorChar)))

(defn directory? [^FileSystemInfo file]
  (= FileAttributes/Directory
     (bit-and (.Attributes file) FileAttributes/Directory)))

(defn ->file-system-info ^FileSystemInfo [path]
  (let [finfo (FileInfo. path)]
    (if (.Exists finfo)
      finfo
      (let [dinfo (DirectoryInfo. path)]
        (when (.Exists dinfo)
          dinfo)))))