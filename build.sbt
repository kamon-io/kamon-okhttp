/* =========================================================================================
 * Copyright © 2013-2020 the kamon project <http://kamon.io/>
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


val kanelaVersion = "1.0.5"

val kamonCore           = "io.kamon"            	%% "kamon-core"               	  % "2.1.0"
val kamonTestkit        = "io.kamon"            	%% "kamon-testkit"            	  % "2.1.0"
val kanelaAgent         = "io.kamon"            	%  "kanela-agent"             	  % kanelaVersion
val kamonCommon         = "io.kamon"              %% "kamon-instrumentation-common" % "2.0.0"

val okhttp     		      = "com.squareup.okhttp3" 	% "okhttp"	    		              % "4.5.0"
val undertow            = "io.undertow"          	% "undertow-core"              	  % "2.0.30.Final"
val scalatest           = "org.scalatest"         %% "scalatest"                    % "3.1.0"

lazy val okHttp3 = (project in file("."))
  .enablePlugins(JavaAgent)
  .settings(name := "kamon-okhttp")
  .settings(bintrayPackage := "kamon-servlet")
  .settings(moduleName := "kamon-okhttp")
  .settings(AutomaticModuleName.settings("kamon-okhttp"))
  .settings(resolvers += Resolver.bintrayRepo("kamon-io", "snapshots"))
  .settings(resolvers += Resolver.mavenLocal)
  .settings(javaAgents += "io.kamon" % "kanela-agent" % kanelaVersion % "compile;test")
  .settings(
      libraryDependencies ++=
        compileScope(kamonCore, kamonCommon, okhttp) ++
        providedScope(okhttp, kanelaAgent) ++
        testScope(kamonTestkit, scalatest, slf4jApi, logbackClassic, undertow))


