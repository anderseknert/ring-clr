(ns ring-clr.util.mime-type-test
  (:require [clojure.test :refer [are deftest is testing]]
            [ring-clr.util.mime-type :refer [ext-mime-type]]))

(deftest ext-mime-type-test
  (testing "default mime types"
    (are [f m] (= (ext-mime-type f) m)
      "foo.txt"  "text/plain"
      "foo.html" "text/html"
      "foo.png"  "image/png"))
  (testing "custom mime types"
    (is (= (ext-mime-type "foo.bar" {"bar" "application/bar"})
           "application/bar"))
    (is (= (ext-mime-type "foo.txt" {"txt" "application/text"})
           "application/text")))
  (testing "case insensitivity"
    (is (= (ext-mime-type "FOO.TXT") "text/plain"))))
