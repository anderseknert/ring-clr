# ring-clr

Experimental ClojureCLR port of [ring](https://github.com/ring-clojure/ring), built mainly for the purpose of providing
an example of what a ClojureCLR project might look like, and — in the spirit of
[ClojureCLR](https://github.com/clojure/clojure-clr) — to have some fun!

## Differences from ring

Since ring is essentially an abstraction on top of the Servlet API, and there is no Servlet API in the CLR, the
ClojureCLR adapters wrapping the HTTP server implementation will need to be more comprehensive. Currently only the
built-in [HttpListener](https://learn.microsoft.com/en-us/dotnet/api/system.net.httplistener) server has been provided
an adapter, but adding one for e.g. [Kestrel](https://learn.microsoft.com/en-us/aspnet/core/fundamentals/servers/kestrel)
should be doable.

* `ring.util.codec` is included here, and not in a separate project.
* No time or effort spent on non-UTF8 encodings — it's 2023.

Except for that, things should work pretty much the same, and much of existing middleware should work as-is.

## Try it out!

```clojure
(ns app
  (:require [ring-clr.adapter.httplistener :as server]))

(defn handler [request]
  {:status 200
   :headers {"content-type" "application/json"}
   :body "{\"hello\":\"world\"}"})

(defn -main []
  (server/run-httplistener handler))
```

## Development

### Run tests

From the project root directory:
```shell
CLOJURE_LOAD_PATH=src:test Clojure.Main -m ring-clr.test
```

## TODO

Lots :)

* Add implementation for all types of `StreamableResponseBody`
* Calculate and set `content-length` header
* Most of the stuff around serving static files
* Datetime parsing of other formats than RFC 1123