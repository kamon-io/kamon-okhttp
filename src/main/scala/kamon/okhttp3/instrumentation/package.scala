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
import kamon.context.Context
import okhttp3.Request

package object instrumentation {

  def isError(statusCode: Int): Boolean =
    statusCode >= 500 && statusCode < 600

  object StatusCodes {
    val NotFound = 404
  }

  def encodeContext(ctx:Context, request:Request): Request = {
    val newRequest  = request.newBuilder()
    val textMap = Kamon.contextCodec().HttpHeaders.encode(ctx)
    textMap.values.foreach { case (name, value) => newRequest.addHeader(name, value) }
    newRequest.build()
  }
}
