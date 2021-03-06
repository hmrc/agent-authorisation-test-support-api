/*
 * Copyright 2020 HM Revenue & Customs
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
import uk.gov.hmrc.play.config.ServicesConfig

class MicroserviceModule(val environment: Environment, val configuration: Configuration)
    extends AbstractModule with ServicesConfig {

  override val runModeConfiguration: Configuration = configuration
  override protected def mode = environment.mode

  def configure(): Unit = {
    val appName = "agent-authorisation"

    val loggerDateFormat: Option[String] =
      configuration.getString("logger.json.dateformat")
    Logger.info(s"Starting microservice : $appName : in mode : ${environment.mode}")
    MDC.put("appName", appName)
    loggerDateFormat.foreach(str => MDC.put("logger.json.dateformat", str))

    bindProperty("appName")
    bindBaseUrl("agents-external-stubs")
    bindBaseUrl("agent-client-authorisation")

    bind(classOf[CorePost]).to(classOf[DefaultHttpClient])
    bind(classOf[HttpGet]).to(classOf[DefaultHttpClient])
    bind(classOf[HttpPost]).to(classOf[DefaultHttpClient])
    bind(classOf[HttpPut]).to(classOf[DefaultHttpClient])

    bindSeqStringProperty("api.supported-versions")
  }

  private def bindServiceProperty(propertyName: String) =
    bind(classOf[String])
      .annotatedWith(Names.named(s"$propertyName"))
      .toProvider(new ServicePropertyProvider(propertyName))

  private class ServicePropertyProvider(propertyName: String) extends Provider[String] {
    override lazy val get =
      getConfString(propertyName, throw new RuntimeException(s"No configuration value found for '$propertyName'"))
  }

  private def bindServiceBooleanProperty(propertyName: String) =
    bind(classOf[Boolean])
      .annotatedWith(Names.named(s"$propertyName"))
      .toProvider(new ServiceBooleanPropertyProvider(propertyName))

  private class ServiceBooleanPropertyProvider(propertyName: String) extends Provider[Boolean] {
    override lazy val get =
      getConfBool(propertyName, throw new RuntimeException(s"No configuration value found for '$propertyName'"))
  }

  private def bindBaseUrl(serviceName: String) =
    bind(classOf[URL])
      .annotatedWith(Names.named(s"$serviceName-baseUrl"))
      .toProvider(new BaseUrlProvider(serviceName))

  private class BaseUrlProvider(serviceName: String) extends Provider[URL] {
    override lazy val get = new URL(baseUrl(serviceName))
  }

  private def bindProperty(propertyName: String) =
    bind(classOf[String])
      .annotatedWith(Names.named(propertyName))
      .toProvider(new PropertyProvider(propertyName))

  private class PropertyProvider(confKey: String) extends Provider[String] {
    override lazy val get = configuration
      .getString(confKey)
      .getOrElse(throw new IllegalStateException(s"No value found for configuration property $confKey"))
  }

  private def bindProperty2param(objectName: String, propertyName: String) =
    bind(classOf[String])
      .annotatedWith(Names.named(objectName))
      .toProvider(new PropertyProvider2param(propertyName))

  private class PropertyProvider2param(confKey: String) extends Provider[String] {
    override lazy val get =
      getConfString(confKey, throw new IllegalStateException(s"No value found for configuration property $confKey"))
  }

  private def bindIntegerProperty(propertyName: String) =
    bind(classOf[Int])
      .annotatedWith(Names.named(propertyName))
      .toProvider(new IntegerPropertyProvider(propertyName))

  private class IntegerPropertyProvider(confKey: String) extends Provider[Int] {
    override lazy val get: Int = configuration
      .getInt(confKey)
      .getOrElse(throw new IllegalStateException(s"No value found for configuration property $confKey"))
  }

  private def bindSeqStringProperty(propertyName: String) =
    bind(new TypeLiteral[Seq[String]]() {})
      .annotatedWith(Names.named(propertyName))
      .toProvider(new SeqStringPropertyProvider(propertyName))

  private class SeqStringPropertyProvider(confKey: String) extends Provider[Seq[String]] {
    override lazy val get: Seq[String] = configuration
      .getStringSeq(confKey)
      .getOrElse(throw new IllegalStateException(s"No value found for configuration property $confKey"))

  }

}
