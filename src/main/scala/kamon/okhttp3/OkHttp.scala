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

import com.typesafe.config.Config
import kamon.util.DynamicAccess
import kamon.{Kamon, OnReconfigureHook}
import okhttp3.Request

object OkHttp {

  @volatile var nameGenerator: NameGenerator = nameGeneratorFromConfig(Kamon.config())
  @volatile var metricsEnabled: Boolean = metricsFromConfig(Kamon.config())

  def generateOperationName(request: Request): String =
    nameGenerator.generateOperationName(request)

  private def nameGeneratorFromConfig(config: Config): NameGenerator = {
    val dynamic = new DynamicAccess(getClass.getClassLoader)
    val nameGeneratorFQCN = config.getString("kamon.okhttp.name-generator")
    dynamic.createInstanceFor[NameGenerator](nameGeneratorFQCN, Nil).get
  }

  def metricsFromConfig(config: Config): Boolean =
    config.getBoolean("kamon.okhttp.metrics.enabled")

  Kamon.onReconfigure(new OnReconfigureHook {
    override def onReconfigure(newConfig: Config): Unit = {
      nameGenerator = nameGeneratorFromConfig(newConfig)
      metricsEnabled = metricsFromConfig(newConfig)
    }
  })
}

trait NameGenerator {
  def generateOperationName(request: Request): String
}

class DefaultNameGenerator extends NameGenerator {
  override def generateOperationName(request: Request): String = {
    request.url().uri().toASCIIString
  }
}
