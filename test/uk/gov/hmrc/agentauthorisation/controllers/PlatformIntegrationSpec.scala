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

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{status => _, _}
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import play.api.http.Status.{NO_CONTENT, OK}
import play.api.libs.json.JsValue
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.agentauthorisation.support.BaseISpec

import scala.concurrent.Future

class PlatformIntegrationSpec extends BaseISpec {

  val stubHost = "localhost"
  val stubPort = 11112
  val wireMockServer = new WireMockServer(wireMockConfig().port(stubPort))

  override def beforeEach(): Unit = {
    wireMockServer.start()
    WireMock.configureFor(stubHost, stubPort)
    stubFor(
      post(urlMatching("/registration"))
        .willReturn(aResponse().withStatus(NO_CONTENT))
    )
    ()
  }

  trait Setup {
    val documentationController: DocumentationController =
      app.injector.instanceOf[DocumentationController]
    val yamlController: YamlController = app.injector.instanceOf[YamlController]
    val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  }

  "microservice" should {

    "provide definition endpoint and documentation endpoint for each api" in new Setup {
      def verifyDocumentationPresent(version: String, endpointName: String): Unit =
        withClue(s"Getting documentation version '$version' of endpoint '$endpointName'") {
          val documentationResult =
            documentationController.definition()(request) // check
          status(documentationResult) shouldBe OK
          ()
        }

      val result: Future[Result] = documentationController.definition()(request)
      status(result) shouldBe OK

      val jsonResponse: JsValue = contentAsJson(result)

      val versions: Seq[String] = (jsonResponse \\ "version").map(_.as[String]).toSeq
      val endpointNames: Seq[Seq[String]] =
        (jsonResponse \\ "endpoints")
          .map(_ \\ "endpointName")
          .map(_.map(_.as[String]).toSeq)
          .toSeq

      versions
        .zip(endpointNames)
        .flatMap { case (version, endpoint) =>
          endpoint.map(endpointName => (version, endpointName))
        }
        .foreach { case (version, endpointName) =>
          verifyDocumentationPresent(version, endpointName)
        }
    }

    "provide yaml documentation" in new Setup {
      val result = yamlController.yaml("1.0", "application.yaml")(request)

      status(result) shouldBe OK
      contentAsString(result) should startWith("openapi: 3.0.3")
    }
  }

  override protected def afterEach(): Unit = {
    wireMockServer.stop()
    wireMockServer.resetMappings()
  }
}
