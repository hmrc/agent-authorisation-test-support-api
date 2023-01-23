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

package uk.gov.hmrc.agentauthorisation

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar._
import play.api.Configuration
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.agentauthorisation.controllers.ErrorResponse._
import uk.gov.hmrc.agentauthorisation.support.BaseSpec
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.bootstrap.config.HttpAuditEvent

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ErrorHandlerSpec extends BaseSpec {
  trait BaseSetup {
    implicit val sys = ActorSystem("MyTest")
    implicit val mat = ActorMaterializer()
    implicit val configuration = Configuration(
      "bootstrap.errorHandler.warnOnly.statusCodes"     -> List.empty,
      "bootstrap.errorHandler.suppress4xxErrorMessages" -> false,
      "bootstrap.errorHandler.suppress5xxErrorMessages" -> false
    )

    implicit val fakeRequest = FakeRequest()
    val mockAuditConnector = mock[AuditConnector]
    val mockAuditResult = mock[AuditResult]
    val mockHttpAuditEvent = mock[HttpAuditEvent]

    when(mockAuditConnector.sendEvent(any())(any(), any())).thenReturn(Future.successful(mockAuditResult))

    val errorHandler = new ErrorHandler(mockAuditConnector, mockHttpAuditEvent)
  }

  "onClientError" should {
    class Setup(statusCode: Int) extends BaseSetup {
      val response = errorHandler.onClientError(fakeRequest, statusCode, "A message")
    }

    "return ErrorNotFound on 404 Not Found" in new Setup(NOT_FOUND) {
      contentAsJson(response) shouldBe contentAsJson(Future.successful(standardNotFound))
    }

    "return ErrorGenericBadRequest on 400 Bad Request" in new Setup(BAD_REQUEST) {
      contentAsJson(response) shouldBe contentAsJson(Future.successful(standardBadRequest))
    }

    "return ErrorUnauthorized on 401 Unauthorized" in new Setup(UNAUTHORIZED) {
      contentAsJson(response) shouldBe contentAsJson(Future.successful(standardUnauthorised))
    }

    "return a statusCode of 405 with the provided message on 405 Method Not Allowed" in new Setup(METHOD_NOT_ALLOWED) {
      contentAsJson(response) shouldBe Json.obj("statusCode" -> METHOD_NOT_ALLOWED, "message" -> "A message")
    }
  }

  "onServerError" should {
    "return ErrorInternalServerError" in new BaseSetup {
      val response = errorHandler.onServerError(fakeRequest, new RuntimeException("Internal Server Error"))

      contentAsJson(response) shouldBe contentAsJson(Future.successful(standardInternalServerError))
    }
  }
}
