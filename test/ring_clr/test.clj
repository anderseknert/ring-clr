(ns ring-clr.test
  (:require [clojure.test :refer [run-all-tests]]
            [ring-clr.core.protocols-test]
            [ring-clr.middleware.content-type-test]
            [ring-clr.middleware.cookies-test]
            [ring-clr.middleware.file-test]
            [ring-clr.middleware.flash-test]
            [ring-clr.middleware.head-test]
            [ring-clr.middleware.keyword-params-test]
            [ring-clr.middleware.nested-params-test]
            [ring-clr.middleware.not-modified-test]
            [ring-clr.middleware.params-test]
            [ring-clr.middleware.session.memory-test]
            [ring-clr.util.codec-test]
            [ring-clr.util.mime-type-test]
            [ring-clr.util.request-test]
            [ring-clr.util.response-test]
            [ring-clr.util.time-test]))

(defn -main []
  (run-all-tests #"^ring-clr(.*)-test"))

