(ns ring-clr.test
  (:require [clojure.test :refer [run-all-tests]]
            [ring-clr.core.protocols-test]
            [ring-clr.util.mime-type-test]
            [ring-clr.util.request-test]
            [ring-clr.util.response-test]))

(defn -main []
  (run-all-tests #"^ring-clr(.*)-test"))

