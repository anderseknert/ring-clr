(ns ring-clr.util.time-test
  (:require [clojure.test :refer [are deftest]]
            [ring-clr.util.time :refer [parse-date]])
  (:import [System DateTime]))

(deftest test-parse-date
  (are [x y] (= (parse-date x) y)
    "Sun, 06 Nov 1994 08:49:37 GMT"   (DateTime. 1994 11 6 8 49 37)
    ;; "Sunday, 06-Nov-94 08:49:37 GMT"  (date-time 1994 11 6 8 49 37)
    ;; "Sun Nov  6 08:49:37 1994"        (DateTime. 1994 11 6 8 49 37)
    ;; "'Sun, 06 Nov 1994 08:49:37 GMT'" (date-time 1994 11 6 8 49 37)
    ))
