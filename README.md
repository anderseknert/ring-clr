# ring-clr

Experimental ClojureCLR port of [ring](https://github.com/ring-clojure/ring).

## Running

```clojure
(ns app
  (:require [ring-clr.adapter.httplistener :as server]))

(defn handler [request]
  {:status 200
   :headers {"content-type" "application/json"}
   :body "{\"foo\":\"bar\"}"})

(defn -main []
  (server/run-httplistener handler))
```
## Tests

From the project root directory:
```shell
CLOJURE_LOAD_PATH=src:test Clojure.Main -m ring-clr.test
```