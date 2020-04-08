/* =========================================================================================
 * Copyright © 2013-2020 the kamon project <http://kamon.io/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License") you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 * =========================================================================================
 */

package kamon.okhttp3.instrumentation

import java.io.IOException

import io.undertow.Undertow
import io.undertow.server.{HttpHandler, HttpServerExchange}
import io.undertow.util.{Headers, StatusCodes => UStatusCodes}
import io.undertow.util.Headers
import kamon.Kamon
import kamon.context.Context
import kamon.tag.Lookups.{plain, plainLong}
import kamon.testkit.{Reconfigure, TestSpanReporter}
import kamon.trace.Span
import okhttp3._
import org.scalatest.concurrent.Eventually
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.SpanSugar
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfterAll, OptionValues}

class OkHttpTracingInstrumentationSpec extends AnyWordSpec
  with Matchers
  with Eventually
  with SpanSugar
  with BeforeAndAfterAll
  with TestSpanReporter
  with Reconfigure
  with OptionValues {

  "the OkHttp Tracing Instrumentation" should {
    "propagate the current context and generate a span around an sync request" in {
      val okSpan = Kamon.spanBuilder("ok-sync-operation-span").start()
      val client = new OkHttpClient.Builder().build()
      val url = "http://localhost:9290/some/path"
      val request = new Request.Builder()
        .url(url)
        .build()

      Kamon.runWithContext(Context.of(Span.Key, okSpan)) {
        val response = client.newCall(request).execute()
        response.body().close()
      }

      eventually(timeout(3 seconds)) {
        val span = testSpanReporter.nextSpan().value

        span.operationName shouldBe "http://localhost:9290/some/path"
        span.kind shouldBe Span.Kind.Client
        span.metricTags.get(plain("component")) shouldBe "okhttp-client"
        span.metricTags.get(plain("http.method")) shouldBe "GET"
        span.tags.get(plain("http.url")) shouldBe url
        span.tags.get(plainLong("http.status_code")) shouldBe 200

        okSpan.id == span.parentId

        testSpanReporter.nextSpan() shouldBe None
      }
    }

    "propagate the current context and generate a span around an async request" in {
      val okAsyncSpan = Kamon.spanBuilder("ok-async-operation-span").start()
      val client = new OkHttpClient.Builder().build()
      val url = "http://localhost:9290/some/path"
      val request = new Request.Builder()
        .url(url)
        .build()

      Kamon.runWithContext(Context.of(Span.Key, okAsyncSpan)) {
        client.newCall(request).enqueue(new Callback() {
          override def onResponse(call: Call, response: Response): Unit = {}

          override def onFailure(call: Call, e: IOException): Unit =
            e.printStackTrace()
        })
      }

      eventually(timeout(3 seconds)) {
        val span = testSpanReporter.nextSpan().value

        span.operationName shouldBe "http://localhost:9290/some/path"
        span.kind shouldBe Span.Kind.Client
        span.metricTags.get(plain("component")) shouldBe "okhttp-client"
        span.metricTags.get(plain("http.method")) shouldBe "GET"
        span.tags.get(plain("http.url")) shouldBe url
        span.tags.get(plainLong("http.status_code")) shouldBe 200

        okAsyncSpan.id == span.parentId

        testSpanReporter.nextSpan() shouldBe None
      }

      //    "pickup a SpanCustomizer from the current context and apply it to the new spans" in {
      //      val okSpan = Kamon.spanBuilder("ok-sync-operation-span").start()
      //      val client = new OkHttpClient.Builder().build()
      //      val url = "https://publicobject.com/helloworld.txt"
      //      val request = new Request.Builder()
      //        .url(url)
      //        .build()
      //
      //      Kamon.runWithContext(Context.of(Span.Key, okSpan).of(Custom)) {
      //        val response = client.newCall(request).execute()
      //        response.body().close()
      //      }
      //
      //
      //      val client = new OkHttpClient.Builder().build()
      //      val request = new Request.Builder()
      //        .url("https://publicobject.com/helloworld.txt")
      //        .build()
      //
      //      Kamon.withContext(Context(SpanCustomizer.ContextKey, SpanCustomizer.forOperationName("customized-client-span"))) {
      //        val response = client.newCall(request).execute()
      //        response.body().close()
      //      }
      //
      //      eventually(timeout(3 seconds)) {
      //        val span = reporter.nextSpan().value
      //        span.operationName shouldBe "customized-client-span"
      //        span.tags("span.kind") shouldBe TagValue.String("client")
      //        span.tags("component") shouldBe TagValue.String("okhttp")
      //        span.tags("http.method") shouldBe TagValue.String("GET")
      //        span.tags("http.status_code") shouldBe TagValue.Number(200)
      //        span.tags("http.url") shouldBe TagValue.String("https://publicobject.com/helloworld.txt")
      //      }
      //    }
    }
  }

  var server: Undertow = _

  override protected def beforeAll(): Unit = {
    enableFastSpanFlushing()
    sampleAlways()
    server = Undertow.builder.addHttpListener(9290, "localhost").setHandler(new HttpHandler {
      override def handleRequest(exchange: HttpServerExchange): Unit = {
        exchange.getResponseHeaders.put(Headers.CONTENT_TYPE, "text/plain")
        exchange.setStatusCode(UStatusCodes.OK)
        exchange.getResponseSender.send("Hello World")
      }
    }).build
    server.start()
  }

  override protected def afterAll(): Unit = {
    server.stop()
  }
}
