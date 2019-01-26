OkHttp3 Integration   ![Build Status](https://travis-ci.org/kamon-io/kamon-okhttp.svg?branch=master)
==========================

[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/kamon-io/Kamon?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.kamon/kamon-okhttp_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.kamon/kamon-okhttp_2.12)


The `kamon-okhttp` module brings bytecode instrumentation to trace okhttp requests

The <b>kamon-okhttp</b> module requires you to start your application using the Kanela Agent.

The bytecode instrumentation provided by the `kamon-okhttp` module hooks into the client to automatically
start and finish Spans for requests that are issued within a trace. This translates into you having metrics about how
the requests you are doing are behaving.

### Getting Started

Kamon OkHttp module is currently available for Scala 2.11 and 2.12.

Supported releases and dependencies are shown below.

| kamon-okhttp  | status | jdk  | scala            
|:------:|:------:|:----:|------------------
|  1.0.3 | stable | 1.8+ | 2.11, 2.12  

To get started with SBT, simply add the following to your `build.sbt`
file:

```scala
libraryDependencies += "io.kamon" %% "kamon-okhttp" % "1.0.3"
```


### Metrics ###

The following metrics will be recorded:

* __active-requests__: The the number active requests.
* __abnormal-termination__: The number of abnormal requests termination.
* __http-request__: Request time by http method.
