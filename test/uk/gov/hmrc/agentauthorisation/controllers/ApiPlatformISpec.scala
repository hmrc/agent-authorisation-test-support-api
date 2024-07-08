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

package uk.gov.hmrc.agentauthorisation.controllers

import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient
import uk.gov.hmrc.agentauthorisation.support.{BaseISpec, Resource}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.ExecutionContext.Implicits.global

class ApiPlatformISpec extends BaseISpec with GuiceOneServerPerSuite {

  override implicit lazy val app: Application = appBuilder.build()

  implicit val hc: HeaderCarrier = HeaderCarrier(otherHeaders = Seq("Accept" -> s"application/vnd.hmrc.1.0+json"))

  override protected def appBuilder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure(
        "auditing.enabled"                                 -> true,
        "auditing.consumer.baseUri.host"                   -> wireMockHost,
        "auditing.consumer.baseUri.port"                   -> wireMockPort,
        "microservice.services.agents-external-stubs.host" -> wireMockHost,
        "microservice.services.agents-external-stubs.port" -> wireMockPort
      )

  implicit val ws: WSClient = app.injector.instanceOf[WSClient]

  override def commonStubs(): Unit =
    givenAuditConnector()

  "/public/api/definition" should {
    "return the definition JSON" in {
      val response: HttpResponse = new Resource(s"/api/definition", port).get()
      response.status shouldBe 200

      val definition = response.json

      (definition \ "api" \ "name").as[String] shouldBe "Agent Authorisation Test Support"
      (definition \ "api" \ "categories").as[Seq[String]] should contain("AGENTS")

      val accessConfig = definition \ "api" \ "versions" \\ "access"
      (accessConfig.head \ "type").as[String] shouldBe "PUBLIC"
    }
  }

  "provide YAML documentation exists for all API versions" in new ApiTestSupport {

    lazy override val runningPort: Int = port

    forAllApiVersions(yamlByVersion) { case (version, yaml) =>
      info(s"Checking API YAML documentation for version[$version] of the API")

      withClue("YAML does not contain a valid YAML 1.0 version header") {
        yaml should include("""version: '1.0'""")
      }

      withClue("RAML does not contain the title 'Agent Authorisation API'") {
        yaml should include("title: Agent Authorisation")

      }

      withClue(s"YAML does not contain a matching version declaration of [$version]") {
        yaml should include(s"""version: '$version'""")
      }
      ()
    }
  }
}
