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

import kamon.okhttp3.OkHttp
import kanela.agent.libs.net.bytebuddy.asm.Advice
import kanela.agent.scala.KanelaInstrumentation
import okhttp3.OkHttpClient

class OkHttpInstrumentation extends KanelaInstrumentation {

  /**
    * Instrument:
    *
    * okhttp3.OkHttpClient::constructor
    */
  forTargetType("okhttp3.OkHttpClient") { builder =>
    builder
      .withAdvisorFor(isConstructor().and(takesOneArgumentOf("okhttp3.OkHttpClient$Builder")), classOf[OkHttpClientBuilderAdvisor])
      .build()
  }
}

/**
  * Avisor for okhttp3.OkHttpClient::constructor(OkHttpClient.Builder)
  */
class OkHttpClientBuilderAdvisor
object OkHttpClientBuilderAdvisor {
  @Advice.OnMethodEnter(suppress = classOf[Throwable])
  def addKamonInterceptor(@Advice.Argument(0) builder:OkHttpClient.Builder): Unit = {
    if(OkHttp.metricsEnabled) builder.addNetworkInterceptor(new KamonMetricsInterceptor)
    builder.addNetworkInterceptor(new KamonTracingInterceptor)
  }
}
