# ring-clr

![build](https://github.com/anderseknert/ring-clr/actions/workflows/build.yaml/badge.svg)

Experimental ClojureCLR port of [Ring](https://github.com/ring-clojure/ring), built mainly for the purpose of providing
an example of what a ClojureCLR project might look like, and — in the spirit of
[ClojureCLR](https://github.com/clojure/clojure-clr) — to have some fun!

## Try it out!

### Prerequisites

* `brew install dotnet` (or any other way of installing .NET)
* `dotnet tool install --global --version 1.12.0-alpha10 Clojure.Main`

### Running ring-clr

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

Run the above from a REPL, or run the `app.cljr` file provided with the repo:

```shell
CLOJURE_LOAD_PATH=src:demo Clojure.Main -m app
```

Or with the new CLI tool:

```shell
dotnet tool install --global --version 0.1.0-alpha4 Clojure.Cljr
cljr -A:demo -m app
```

## Differences from Ring

Since Ring is essentially an abstraction on top of the Servlet API, and there is no Servlet API in the CLR, the
ClojureCLR adapters wrapping the HTTP server implementation will need to be more comprehensive. Currently only the
built-in [HttpListener](https://learn.microsoft.com/en-us/dotnet/api/system.net.httplistener) server has been provided
an adapter, but adding one for e.g. [Kestrel](https://learn.microsoft.com/en-us/aspnet/core/fundamentals/servers/kestrel)
should be doable. Other differences should be minimal:

* Serving a "resource" (i.e. from classpath / jar) not applicable here
* Made `ring.util.codec` included in core, and not in a separate project
* Nothing marked deprecated in Ring has been included in this port
* No time or effort spent on non-UTF8 encodings — it's 2023

Except for that, things should work pretty much the same, and much of existing middleware should work as-is.

## Documentation

The [docs for Ring](https://github.com/ring-clojure/ring/wiki) should be valid for almost
everything found here. For the cases where a Java type is expected as input our output, the
closest CLR equivalent type is used instead:

- `File` => `FileInfo` or `DirectoryInfo`
- `Date` => `DateTime`
- `InputStream` => `Stream`
- `ByteArrayOutputStream` => `MemoryStream`

If you find anything not mentioned above, or not easily understood, please open an issue!

## Development

### Run tests

From the project root directory:
```shell
CLOJURE_LOAD_PATH=src:test Clojure.Main -m ring-clr.test
```

## TODO

- [x] Add implementation for all types of `StreamableResponseBody`
- [x] Calculate and set `content-length` header (no Jetty to do it for us)
- [x] Most of the stuff around serving static files
- [ ] File upload (multipart params) middleware
- [ ] Cookie session store middleware
- [ ] Datetime parsing of other formats than RFC 1123
- [ ] Provide ClojureCLR specific documentation
- [x] Use .cljr extension for all files, ensure tooling understands it
- [ ] Publish to Nuget, maybe Clojars
