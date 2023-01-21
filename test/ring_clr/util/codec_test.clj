(ns ring-clr.util.codec-test
  (:require [clojure.test :refer [are deftest is testing]]
            [ring-clr.util.platform :as clr]
            [ring-clr.util.codec :refer [base64-decode base64-encode form-decode form-encode form-decode-str
                                         percent-decode percent-encode url-decode url-encode]]))

(deftest test-percent-encode
  (is (= (percent-encode " ") "%20"))
  (is (= (percent-encode "+") "%2B"))
  (is (= (percent-encode "foo") "%66%6F%6F")))

(deftest test-percent-decode
  (is (= (percent-decode "%s/") "%s/"))
  (is (= (percent-decode "%20") " "))
  (is (= (percent-decode "foo%20bar") "foo bar"))
  (is (= (percent-decode "%24") "$")))

(deftest test-url-encode
  (is (= (url-encode "foo/bar") "foo%2Fbar"))
  (is (= (url-encode "foo+bar") "foo+bar"))
  (is (= (url-encode "foo bar") "foo%20bar")))

(deftest test-url-decode
  (is (= (url-decode "foo%2Fbar") "foo/bar"))
  (is (= (url-decode "%") "%")))

(deftest test-base64-encoding
  (let [str-bytes (clr/str->bytes "foo?/+")]
    (is (= (vec str-bytes) (vec (base64-decode (base64-encode str-bytes)))))))

(deftest test-form-encode
  (testing "strings"
    (are [x y] (= (form-encode x) y)
      "foo bar" "foo+bar"
      "foo+bar" "foo%2bbar"
      "foo/bar" "foo%2fbar"))
  (testing "maps"
    (are [x y] (= (form-encode x) y)
      {"a" "b"}         "a=b"
      {:a "b"}          "a=b"
      {"a" 1}           "a=1"
      {"a" nil}         "a="
      {"a" "b" "c" "d"} "a=b&c=d"
      {"a" "b c"}       "a=b+c"
      {"a" ["b" "c"]}   "a=b&a=c"
      {"a" ["c" "b"]}   "a=c&a=b"
      {"a" (seq [1 2])} "a=1&a=2"
      {"a" #{"c" "b"}}  "a=b&a=c")))

(deftest test-form-decode-str
  (is (= (form-decode-str "foo=bar+baz") "foo=bar baz"))
  (is (nil? (form-decode-str "%D")))
  (is (= (form-decode-str "foo=bar+baz" nil) "foo=bar baz"))
  (is (= (form-decode-str "foo=bar+baz" "UTF-8") "foo=bar baz")))

(deftest test-form-decode
  (are [x y] (= (form-decode x) y)
    "foo"     "foo"
    "a=b"     {"a" "b"}
    "a=b&c=d" {"a" "b" "c" "d"}
    "foo+bar" "foo bar"
    "a=b+c"   {"a" "b c"}
    "a=b%2Fc" {"a" "b/c"}
    "a=b&c"   {"a" "b" "c" ""}
    "a=&b=c"  {"a" "" "b" "c"}
    "a&b=c"   {"a" "" "b" "c"}
    "="       {"" ""}
    "a="      {"a" ""}
    "=b"      {"" "b"}))
