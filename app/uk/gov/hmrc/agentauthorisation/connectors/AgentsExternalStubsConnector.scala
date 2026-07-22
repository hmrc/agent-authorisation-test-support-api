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

package uk.gov.hmrc.agentauthorisation.connectors

import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.utils.UriEncoding
import sttp.model.Uri
import sttp.model.Uri.UriContext
import uk.gov.hmrc.agentauthorisation.models.{BusinessDetails, User, VatCustomerInfo, Vrn}
import uk.gov.hmrc.agentauthorisation.util.HttpAPIMonitor
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, HttpReads, HttpResponse, SessionId}
import uk.gov.hmrc.http.HeaderNames.authorisation
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.play.bootstrap.metrics.Metrics

import java.net.URL
import java.nio.charset.StandardCharsets
import javax.inject.{Inject, Named, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AgentsExternalStubsConnector @Inject() (
  @Named("agents-external-stubs-baseUrl") baseUrl: URL,
  http: HttpClientV2,
  val metrics: Metrics
)(implicit val ec: ExecutionContext)
    extends HttpAPIMonitor {

  private val AgentsExternalStubsUri = uri"$baseUrl/agents-external-stubs/"

  def signInAndGetSessionHeaders(userId: String)(implicit sessionHeaders: HeaderCarrier): Future[HeaderCarrier] =
    http
      .post(uri"$AgentsExternalStubsUri/sign-in".toJavaUri.toURL)
      .withBody(Json.obj("planetId" -> "hmrc", "userId" -> userId))
      .execute[HttpResponse]
      .map { response =>
        val authorizationHeader = response.header(HeaderNames.AUTHORIZATION).map(Authorization(_))
        val sessionIdHeader = response.header("X-Session-ID").map(SessionId(_))
        HeaderCarrier(authorization = authorizationHeader, sessionId = sessionIdHeader)
      }

  def createUser(user: User, affinityGroup: String)(implicit sessionHeaders: HeaderCarrier): Future[Unit] = {
    val uri = uri"$AgentsExternalStubsUri/users?affinityGroup=$affinityGroup"
    http
      .post(uri.toJavaUri.toURL)
      .withBody(Json.toJson(user))
      .execute[Unit]
  }

  def getUserIdForNino(nino: String)(implicit sessionHeaders: HeaderCarrier): Future[String] = {
    val encodedNino = encodePathSegment(nino)
    val uri = uri"$AgentsExternalStubsUri/users/nino/$encodedNino"

    http
      .get(uri.toJavaUri.toURL)
      .execute[HttpResponse]
      .map { response =>
        (response.json \ "userId").as[String]
      }
  }

  def getUserIdForEnrolment(enrolmentKey: String)(implicit sessionHeaders: HeaderCarrier): Future[String] = {
    val encodedKnownFact = encodePathSegment(enrolmentKey)
    val uri = uri"$AgentsExternalStubsUri/known-facts/$encodedKnownFact"

    http
      .get(uri.toJavaUri.toURL)
      .execute[HttpResponse]
      .map { response =>
        (response.json \ "user" \ "userId").as[String]
      }
  }

  def getBusinessDetails(nino: Nino)(implicit sessionHeaders: HeaderCarrier): Future[Option[BusinessDetails]] =
    getWithDesHeaders[BusinessDetails](
      uri"$baseUrl/registration/business-details/nino/${encodePathSegment(nino.value)}"
    )

  def getVatCustomerInformation(vrn: Vrn)(implicit sessionHeaders: HeaderCarrier): Future[Option[VatCustomerInfo]] =
    getWithDesHeaders[VatCustomerInfo](
      uri"$baseUrl/vat/customer/vrn/${encodePathSegment(vrn.value)}/information"
    )

  private def getWithDesHeaders[T: HttpReads](url: Uri)(implicit sessionHeaders: HeaderCarrier): Future[Option[T]] =
    http
      .get(url.toJavaUri.toURL)
      .setHeader(
        authorisation -> "Bearer 123",
        "Environment" -> "test"
      )
      .execute[Option[T]]

  private def encodePathSegment(pathSegment: String): String =
    UriEncoding.encodePathSegment(pathSegment, StandardCharsets.UTF_8.name)

}
