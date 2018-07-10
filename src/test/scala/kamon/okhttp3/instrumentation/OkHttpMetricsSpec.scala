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

import java.util.concurrent.Executors

import io.undertow.Undertow
import io.undertow.server.HttpServerExchange
import io.undertow.util.{Headers, StatusCodes => UStatusCodes}
import kamon.okhttp3.Metrics.{GeneralMetrics, RequestTimeMetrics}
import kamon.testkit.MetricInspection
import okhttp3.{OkHttpClient, Request}
import org.scalatest.concurrent.Eventually
import org.scalatest.time.SpanSugar
import org.scalatest.{BeforeAndAfterAll, Matchers, OptionValues, WordSpec}

import scala.concurrent.{ExecutionContext, Future}

class OkHttpMetricsSpec extends WordSpec with Matchers with Eventually with SpanSugar with BeforeAndAfterAll
  with MetricInspection  with OptionValues {

  val parallelRequestExecutor = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(10))

  "the OkHttp Metrics" should {
    "track the total of active request" in {
      val client = new OkHttpClient.Builder().build()
      val request = new Request.Builder()
        .url("http://localhost:9290/")
        .build()

      for(_ <- 1 to 10) yield {
        Future {
          val response = client.newCall(request).execute()
          response.code() shouldBe 200
          response.body().close()
        }(parallelRequestExecutor)
      }

      eventually(timeout(3 seconds)) {
        GeneralMetrics().activeRequests.distribution().max shouldBe 10L
      }

      eventually(timeout(2 seconds)) {
        GeneralMetrics().activeRequests.distribution().min shouldBe 0L
      }
    }

    "track the request time with method GET" in {
      val client = new OkHttpClient.Builder().build()
      val request = new Request.Builder()
        .url("http://localhost:9290/")
        .build()

      for(_ <- 1 to 10) yield {
        val response = client.newCall(request).execute()
        response.code() shouldBe 200
        response.body().close()
      }

      eventually(timeout(3 seconds)) {
        RequestTimeMetrics().forMethod("GET").distribution().max should be > 0L
      }
    }
  }

  var server:Undertow = _

  override protected def beforeAll(): Unit = {
    server = Undertow.builder.addHttpListener(9290, "localhost").setHandler(
      (exchange: HttpServerExchange) => {
        exchange.getResponseHeaders.put(Headers.CONTENT_TYPE, "text/plain")
        exchange.setStatusCode(UStatusCodes.OK)
        exchange.getResponseSender.send("Hello World")
      }).build
    server.start()
  }

  override protected def afterAll(): Unit = {
    server.stop()
  }
}