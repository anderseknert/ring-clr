(ns ring-clr.core.protocols-test
  (:require [clojure.test :refer [deftest is testing]]
            [ring-clr.core.protocols :refer [write-body-to-stream]]
            [ring-clr.util.platform :as clr]))

; NOTE: we should use a MemoryStream for tests, but this is not possible until
; https://ask.clojure.org/index.php/12549/using-proxy-to-extend-memorystream-fails-on-close
; is resolved

(deftest test-write-body-to-stream
  (testing "string"
    (let [path (clr/tmp-file-path)]
      (write-body-to-stream "foo" {} (clr/file-create path))
      (is (= (clr/stream->str (clr/file-read path)) "foo"))))

  (testing "clojure.lang.ISeq"
    (let [path (clr/tmp-file-path)]
      (write-body-to-stream '("foo" "bar") {} (clr/file-create path))
      (is (= (clr/stream->str (clr/file-read path)) "foobar"))))
  
  (testing "nil"
    (let [path (clr/tmp-file-path)
          stream (clr/file-create path)]
      (write-body-to-stream nil {} stream)
      (is (false? (or (.CanRead stream) (.CanWrite stream)))))))
