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

import kamon.Kamon
import kamon.okhttp3.OkHttp
import kamon.trace.{Span, SpanCustomizer}
import okhttp3.{Interceptor, Request, Response}

import scala.util.{Failure, Success, Try}

final class KamonTracingInterceptor extends Interceptor  {

  override def intercept(chain: Interceptor.Chain): Response = {
    val request: Request = chain.request

    val currentContext = Kamon.currentContext()
    val parentSpan = currentContext.get(Span.ContextKey)

    val clientSpanBuilder = Kamon.buildSpan(OkHttp.generateOperationName(request))
      .asChildOf(parentSpan)
      .withMetricTag("span.kind", "client")
      .withMetricTag("component", "okhttp")
      .withMetricTag("http.method", request.method)
      .withTag("http.url", request.url().uri().toASCIIString)

    val clientRequestSpan = currentContext.get(SpanCustomizer.ContextKey)
      .customize(clientSpanBuilder)
      .start()

    val contextWithClientSpan = currentContext.withKey(Span.ContextKey, clientRequestSpan)
    val requestWithContext = encodeContext(contextWithClientSpan, request)

    val response = Try(chain.proceed(requestWithContext)) match {
      case Success(successfulResponse) => successfulResponse
      case Failure(cause) =>
        clientRequestSpan.addError(cause.getMessage, cause)
        clientRequestSpan.finish()
        throw cause
    }

    val statusCode = response.code()

    clientRequestSpan.tag("http.status_code", statusCode)

    if(isError(statusCode))
      clientRequestSpan.addError("error")

    if(statusCode == StatusCodes.NotFound)
      clientRequestSpan.setOperationName("not-found")

    clientRequestSpan.finish()
    response
  }
}


