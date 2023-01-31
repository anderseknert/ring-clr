(ns ring-clr.core.protocols-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.clr.io :as io]
            [ring-clr.core.protocols :refer [write-body-to-stream]]
            [ring-clr.util.io :as rio]
            [ring-clr.util.platform :as clr]))

(deftest test-write-body-to-stream
  (testing "bytes"
    (let [body   (clr/str->bytes "foo")
          stream (clr/empty-memory-stream body)]
      (write-body-to-stream body {} stream)
      (is (= (clr/stream->str stream) "foo"))))

  (testing "string"
    (let [body   "foo"
          stream (clr/empty-memory-stream body)]
      (write-body-to-stream body {} stream)
      (is (= (clr/stream->str stream) "foo"))))

  (testing "clojure.lang.ISeq"
    (let [body   '("foo" "bar")
          stream (clr/empty-memory-stream)]
      (write-body-to-stream body {} stream)
      (is (= (clr/memory-stream->str-safe stream) "foobar"))))

  (testing "Stream"
    (let [body   (clr/reset-stream! (rio/string-input-stream "foo"))
          stream (clr/empty-memory-stream)]
      (write-body-to-stream body {} stream)
      (is (= (clr/memory-stream->str-safe stream) "foo"))))

  (testing "FileInfo"
    (let [path   (clr/tmp-file-path)
          _      (spit path "foo")
          body   (io/as-file path)
          stream (clr/empty-memory-stream)]
      (write-body-to-stream body {} stream)
      (is (= (clr/memory-stream->str-safe stream) "foo"))))

  (testing "nil"
    (let [body   nil
          stream (clr/empty-memory-stream)]
      (write-body-to-stream body {} stream)
      (is (false? (or (.CanRead stream) (.CanWrite stream)))))))
