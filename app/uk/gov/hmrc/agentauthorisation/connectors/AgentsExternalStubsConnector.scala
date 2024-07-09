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
import uk.gov.hmrc.agentauthorisation.models.{BusinessDetails, User, VatCustomerInfo}
import uk.gov.hmrc.agentauthorisation.util.HttpAPIMonitor
import uk.gov.hmrc.agentmtdidentifiers.model.Vrn
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderNames.authorisation
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse, UpstreamErrorResponse}
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

  def signIn(userId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[(String, String, String)] =
    http
      .post(
        new URL(s"$baseUrl/agents-external-stubs/sign-in")
      )
      .withBody(
        Json.obj("planetId" -> "hmrc", "userId" -> userId)
      )
      .execute[HttpResponse]
      .map(response =>
        (
          response
            .header(HeaderNames.AUTHORIZATION)
            .getOrElse(throw new Exception("Missing Authorization token")),
          response.header("X-Session-ID").getOrElse(throw new Exception("Missing X-Session-ID token")),
          response.header(HeaderNames.LOCATION).getOrElse(throw new Exception("User location URI not found"))
        )
      )

  def createUser(user: User, affinityGroup: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] =
    http
      .post(
        new URL(s"$baseUrl/agents-external-stubs/users?affinityGroup=$affinityGroup")
      )
      .withBody(Json.toJson(user))
      .execute[HttpResponse]
      .map(_ => ())
      .recover {
        case e: UpstreamErrorResponse if e.statusCode == 409 => ()
      }

  def updateCurrentUser(user: User)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] =
    http
      .put(new URL(s"$baseUrl/agents-external-stubs/users"))
      .withBody(Json.toJson(user))
      .execute[HttpResponse]
      .map(_ => ())
      .recover {
        case e: UpstreamErrorResponse if e.statusCode == 409 => ()
      }

  def getUserIdForEnrolment(enrolmentKey: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[String] =
    http
      .get(new URL(s"$baseUrl/agents-external-stubs/known-facts/${UriEncoding
          .encodePathSegment(enrolmentKey, StandardCharsets.UTF_8.name)}"))
      .execute[HttpResponse]
      .map { response =>
        (response.json \ "user" \ "userId").as[String]
      }

  def getUserIdForNino(nino: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[String] =
    http
      .get(new URL(s"$baseUrl/agents-external-stubs/users/nino/${UriEncoding
          .encodePathSegment(nino, StandardCharsets.UTF_8.name)}"))
      .execute[HttpResponse]
      .map { response =>
        (response.json \ "userId").as[String]
      }

  def getBusinessDetails(
    nino: Nino
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[BusinessDetails]] =
    getWithDesHeaders[BusinessDetails](
      "getRegistrationBusinessDetailsByNino",
      new URL(baseUrl, s"/registration/business-details/nino/${encodePathSegment(nino.value)}").toString
    )

  def getVatCustomerInformation(
    vrn: Vrn
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[VatCustomerInfo]] = {
    val url = new URL(baseUrl, s"/vat/customer/vrn/${encodePathSegment(vrn.value)}/information")
    getWithDesHeaders[VatCustomerInfo]("GetVatCustomerInformation", url.toString)
  }

  private def getWithDesHeaders[T: HttpReads](apiName: String, url: String)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Option[T]] = {
    val desHeader =
      Seq(
        authorisation -> "Bearer 123",
        "Environment" -> "test"
      )
    http.get(new URL(url)).setHeader(desHeader: _*).execute[Option[T]]
  }

  private def encodePathSegment(pathSegment: String): String =
    UriEncoding.encodePathSegment(pathSegment, StandardCharsets.UTF_8.name)

}
