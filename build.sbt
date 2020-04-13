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
val jettyV9Version = "9.4.25.v20191220"

val kamonCore = "io.kamon" %% "kamon-core" % "2.1.0"
val kamonTestkit = "io.kamon" %% "kamon-testkit" % "2.1.0"
val kanelaAgent = "io.kamon" % "kanela-agent" % kanelaVersion
val kamonCommon = "io.kamon" %% "kamon-instrumentation-common" % "2.0.0"

val okhttp = "com.squareup.okhttp3" % "okhttp" % "4.5.0"
val scalatest = "org.scalatest" %% "scalatest" % "3.1.0"
val jettyServerV9 = "org.eclipse.jetty" % "jetty-server" % jettyV9Version
val jettyServletV9 = "org.eclipse.jetty" % "jetty-servlet" % jettyV9Version

lazy val okHttp3 = (project in file("."))
  .enablePlugins(JavaAgent)
  .settings(
    name := "kamon-okhttp",
    bintrayPackage := "kamon-servlet",
    moduleName := "kamon-okhttp",
    AutomaticModuleName.settings("kamon-okhttp"),
    scalaVersion := "2.11.12",
    crossScalaVersions := Seq("2.11.12", "2.12.10", "2.13.1"),
    scalacOptions ++= Seq(
      "-language:higherKinds",
      "-language:postfixOps") ++
      (CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 11)) => Seq("-Ybackend:GenBCode", "-Ydelambdafy:method", "-target:jvm-1.8")
        case Some((2, 12)) => Seq("-opt:l:method")
        case _ => Seq.empty[String]
      }),
    resolvers += Resolver.bintrayRepo("kamon-io", "snapshots"),
    resolvers += Resolver.mavenLocal,
    javaAgents += "io.kamon" % "kanela-agent" % kanelaVersion % "compile;test",
    libraryDependencies ++=
      compileScope(kamonCore, kamonCommon) ++
        providedScope(okhttp, kanelaAgent) ++
        testScope(kamonTestkit, scalatest, slf4jApi, logbackClassic, jettyServerV9, jettyServletV9))
