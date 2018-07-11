/*
 * =========================================================================================
 * Copyright © 2013-2018 the kamon project <http://kamon.io/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
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

import kamon.okhttp3.Metrics.{GeneralMetrics, OkHttpMetrics, RequestTimeMetrics}
import okhttp3.{Interceptor, Response}

final class KamonMetricsInterceptor extends Interceptor  {

  val metrics = OkHttpMetrics(GeneralMetrics(), RequestTimeMetrics())

  override def intercept(chain: Interceptor.Chain): Response = {
    val request = chain.request()
    val startTimestamp = System.nanoTime()

    metrics.generalMetrics.activeRequests.increment()

    try {
      chain.proceed(chain.request)
    } catch {
      case t: Throwable =>
        metrics.generalMetrics.abnormalTerminations.record(System.nanoTime() - startTimestamp)
        throw t
    } finally {
      val elapsed = System.nanoTime() - startTimestamp
      metrics.requestTimeMetrics.forMethod(request.method()).record(elapsed)
      metrics.requestTimeMetrics.forMethod("total").record(elapsed)
      metrics.generalMetrics.activeRequests.decrement()
    }
  }
}


