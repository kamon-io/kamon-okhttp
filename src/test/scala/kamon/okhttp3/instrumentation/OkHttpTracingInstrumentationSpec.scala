/* =========================================================================================
 * Copyright © 2013-2018 the kamon project <http://kamon.io/>
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

import kamon.Kamon
import kamon.context.Context
import kamon.testkit.{MetricInspection, Reconfigure, TestSpanReporter}
import kamon.trace.Span.TagValue
import kamon.trace.{Span, SpanCustomizer}
import kamon.util.Registration
import okhttp3._
import org.scalatest.concurrent.Eventually
import org.scalatest.time.SpanSugar
import org.scalatest.{BeforeAndAfterAll, Matchers, OptionValues, WordSpec}

class OkHttpTracingInstrumentationSpec extends WordSpec
  with Matchers
  with Eventually
  with SpanSugar
  with BeforeAndAfterAll
  with MetricInspection
  with Reconfigure
  with OptionValues {

  "the OkHttp Tracing Instrumentation" should {
    "propagate the current context and generate a span around an sync request" in {
      val okSpan = Kamon.buildSpan("ok-sync-operation-span").start()
      val client = new OkHttpClient.Builder().build()
      val request = new Request.Builder()
        .url("https://publicobject.com/helloworld.txt")
        .build()

      Kamon.withContext(Context.create(Span.ContextKey, okSpan)) {
        val response = client.newCall(request).execute()
        response.body().close()
      }

      eventually(timeout(3 seconds)) {
        val span = reporter.nextSpan().value
        span.operationName shouldBe "https://publicobject.com"
        span.tags("span.kind") shouldBe TagValue.String("client")
        span.tags("component") shouldBe TagValue.String("okhttp")
        span.tags("http.method") shouldBe TagValue.String("GET")
        span.tags("http.status_code") shouldBe TagValue.Number(200)

        span.context.parentID.string shouldBe okSpan.context().spanID.string
      }
    }

    "propagate the current context and generate a span around an async request" in {
      val okAsyncSpan = Kamon.buildSpan("ok-async-operation-span").start()
      val client = new OkHttpClient.Builder().build()
      val request = new Request.Builder()
        .url("https://publicobject.com/helloworld.txt")
        .build()

      Kamon.withContext(Context.create(Span.ContextKey, okAsyncSpan)) {
        client.newCall(request).enqueue(new Callback() {
          override def onResponse(call: Call, response: Response): Unit = {}
          override def onFailure(call: Call, e: IOException): Unit =
            e.printStackTrace()
        })
      }

      eventually(timeout(3 seconds)) {
        val span = reporter.nextSpan().value
        span.operationName shouldBe "https://publicobject.com"
        span.tags("span.kind") shouldBe TagValue.String("client")
        span.tags("component") shouldBe TagValue.String("okhttp")
        span.tags("http.method") shouldBe TagValue.String("GET")
        span.tags("http.status_code") shouldBe TagValue.Number(200)
        span.tags("http.url") shouldBe TagValue.String("https://publicobject.com/helloworld.txt")

//        span.context.parentID.string shouldBe okAsyncSpan.context().spanID.string
      }
    }

    "pickup a SpanCustomizer from the current context and apply it to the new spans" in {
      val client = new OkHttpClient.Builder().build()
      val request = new Request.Builder()
        .url("https://publicobject.com/helloworld.txt")
        .build()

      Kamon.withContext(Context(SpanCustomizer.ContextKey, SpanCustomizer.forOperationName("customized-client-span"))) {
        val response = client.newCall(request).execute()
        response.body().close()
      }

      eventually(timeout(3 seconds)) {
        val span = reporter.nextSpan().value
        span.operationName shouldBe "customized-client-span"
        span.tags("span.kind") shouldBe TagValue.String("client")
        span.tags("component") shouldBe TagValue.String("okhttp")
        span.tags("http.method") shouldBe TagValue.String("GET")
        span.tags("http.status_code") shouldBe TagValue.Number(200)
        span.tags("http.url") shouldBe TagValue.String("https://publicobject.com/helloworld.txt")
      }
    }
  }

  var registration: Registration = _
  val reporter = new TestSpanReporter()

  override protected def beforeAll(): Unit = {
    enableFastSpanFlushing()
    sampleAlways()
    registration = Kamon.addReporter(reporter)
  }

  override protected def afterAll(): Unit = {
    registration.cancel()
  }
}