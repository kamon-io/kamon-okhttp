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

package kamon.okhttp3

import kamon.Kamon
import kamon.metric.{Histogram, HistogramMetric, RangeSampler}
import kamon.metric.MeasurementUnit._

object Metrics {

  /**
    * General Metrics for OkHttp:
    *
    * - active-requests: The the number active requests.
    * - abnormal-termination:The number of abnormal requests termination.
    */
  case class GeneralMetrics(activeRequests: RangeSampler,
                            abnormalTerminations: Histogram)
  object GeneralMetrics {
    def apply(): GeneralMetrics = {
      val generalTags = Map("component" -> "okhttp")
      new GeneralMetrics(
        Kamon.rangeSampler("active-requests").refine(generalTags),
        Kamon.histogram("abnormal-termination").refine(generalTags))
    }
  }

  /**
    * Request Metrics for OkHttp:
    *
    * - http-request: Request time by status code.
    */
  case class RequestTimeMetrics(requestTimeMetric:HistogramMetric) {
    def forMethod(method: String): Histogram = {
      val requestMetricsTags = Map("component" -> "okhttp", "method" -> method)
      requestTimeMetric.refine(requestMetricsTags)
    }
  }

  object RequestTimeMetrics {
    def apply(): RequestTimeMetrics =
      new RequestTimeMetrics(Kamon.histogram("http-request", time.nanoseconds))
  }

  case class OkHttpMetrics(generalMetrics: GeneralMetrics, requestTimeMetrics: RequestTimeMetrics)
}