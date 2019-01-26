/* =========================================================================================
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


val kamonCore           = "io.kamon"            	%% "kamon-core"               	  % "1.1.3"
val kamonTestkit        = "io.kamon"            	%% "kamon-testkit"            	  % "1.1.3"
val scalaExtension      = "io.kamon"            	%% "kanela-scala-extension"   	  % "0.0.10"

val okhttp     		      = "com.squareup.okhttp3" 	% "okhttp"	    		              % "3.10.0"
val lombok              = "org.projectlombok"    	% "lombok"                    	  % "1.18.0"
val undertow            = "io.undertow"          	% "undertow-core"              	  % "2.0.9.Final"

lazy val okHttp3 = (project in file("."))
  .enablePlugins(JavaAgent)
  .settings(name := "kamon-okhttp3")
  .settings(javaAgents += "io.kamon"    % "kanela-agent"   % "0.0.15"  % "compile;test")
  .settings(resolvers += Resolver.bintrayRepo("kamon-io", "snapshots"))
  .settings(resolvers += Resolver.mavenLocal)
  .settings(AutomaticModuleName.settings("kamon.okhttp"))
  .settings(
      libraryDependencies ++=
        compileScope(kamonCore, okhttp, scalaExtension) ++
        providedScope(lombok) ++
        testScope(kamonTestkit, scalatest, slf4jApi, logbackClassic, undertow))


