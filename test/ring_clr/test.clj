(ns ring-clr.test
  (:require [clojure.test :refer [run-all-tests]]
            [ring-clr.util.response-test]))

(defn -main []
  (run-all-tests #"^ring-clr(.*)-test"))

