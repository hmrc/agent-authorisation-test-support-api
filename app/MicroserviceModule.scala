/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.net.URL

import com.google.inject.{AbstractModule, TypeLiteral}
import com.google.inject.name.Names
import javax.inject.Provider
import org.slf4j.MDC
import play.api.{Configuration, Environment, Logger}
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

class MicroserviceModule(val environment: Environment, val configuration: Configuration) extends AbstractModule {

  override def configure(): Unit = {
    val appName = "agent-authorisation"

    val loggerDateFormat: Option[String] =
      configuration.getOptional[String]("logger.json.dateformat")
    Logger(getClass).info(s"Starting microservice : $appName : in mode : ${environment.mode}")
    MDC.put("appName", appName)
    loggerDateFormat.foreach(str => MDC.put("logger.json.dateformat", str))

    bindBaseUrl("agents-external-stubs")
    bindBaseUrl("agent-client-authorisation")

    bind(classOf[CorePost]).to(classOf[DefaultHttpClient])
    bind(classOf[HttpGet]).to(classOf[DefaultHttpClient])
    bind(classOf[HttpPost]).to(classOf[DefaultHttpClient])
    bind(classOf[HttpPut]).to(classOf[DefaultHttpClient])

    bindSeqStringProperty("api.supported-versions")
    ()
  }

  val servicesConfig: ServicesConfig = new ServicesConfig(configuration)

  private def bindBaseUrl(serviceName: String) =
    bind(classOf[URL])
      .annotatedWith(Names.named(s"$serviceName-baseUrl"))
      .toProvider(new BaseUrlProvider(serviceName))

  private class BaseUrlProvider(serviceName: String) extends Provider[URL] {
    override lazy val get = new URL(servicesConfig.baseUrl(serviceName))
  }

  private def bindSeqStringProperty(propertyName: String) =
    bind(new TypeLiteral[Seq[String]]() {})
      .annotatedWith(Names.named(propertyName))
      .toProvider(new SeqStringPropertyProvider(propertyName))

  private class SeqStringPropertyProvider(confKey: String) extends Provider[Seq[String]] {
    override lazy val get: Seq[String] = configuration
      .getOptional[Seq[String]](confKey)
      .getOrElse(throw new IllegalStateException(s"No value found for configuration property $confKey"))

  }

}
