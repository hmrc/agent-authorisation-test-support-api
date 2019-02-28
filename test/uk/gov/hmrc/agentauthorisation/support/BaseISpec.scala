/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.agentauthorisation.support

import akka.stream.Materializer
import org.scalatestplus.play.OneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.agentauthorisation.stubs.DataStreamStubs
import uk.gov.hmrc.play.test.UnitSpec

abstract class BaseISpec extends UnitSpec with OneAppPerSuite with WireMockSupport with DataStreamStubs {

  override implicit lazy val app: Application = appBuilder.build()

  protected def appBuilder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure(
        "auditing.enabled"                                      -> true,
        "auditing.consumer.baseUri.host"                        -> wireMockHost,
        "auditing.consumer.baseUri.port"                        -> wireMockPort,
        "microservice.services.agents-external-stubs.host"      -> wireMockHost,
        "microservice.services.agents-external-stubs.port"      -> wireMockPort,
        "microservice.services.agent-client-authorisation.host" -> wireMockHost,
        "microservice.services.agent-client-authorisation.port" -> wireMockPort,
        "microservice.services.service-locator.port"            -> wireMockPort,
        "microservice.services.service-locator.host"            -> wireMockHost,
        "microservice.services.service-locator.enabled"         -> false,
        "passcodeAuthentication.enabled"                        -> true,
        "api.supported-versions"                                -> Seq("1.0")
      )

  protected implicit val materializer: Materializer = app.materializer

  def commonStubs(): Unit =
    givenAuditConnector()

  override protected def beforeEach(): Unit =
    super.beforeEach()
}
