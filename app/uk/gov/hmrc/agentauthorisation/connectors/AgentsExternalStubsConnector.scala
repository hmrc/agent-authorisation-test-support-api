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

package uk.gov.hmrc.agentauthorisation.connectors

import java.net.URL
import java.nio.charset.StandardCharsets

import com.codahale.metrics.MetricRegistry
import com.kenshoo.play.metrics.Metrics
import javax.inject.{Inject, Named, Singleton}
import play.api.http.HeaderNames
import play.api.libs.json.{JsObject, Json}
import play.utils.UriEncoding
import uk.gov.hmrc.agent.kenshoo.monitoring.HttpAPIMonitor
import uk.gov.hmrc.agentauthorisation.models.{BusinessDetails, EnrolmentInfo, User, VatCustomerInfo}
import uk.gov.hmrc.agentmtdidentifiers.model.Vrn
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.logging.Authorization

import scala.concurrent.{ExecutionContext, Future}
@Singleton
class AgentsExternalStubsConnector @Inject()(
  @Named("agents-external-stubs-baseUrl") baseUrl: URL,
  http: HttpPost with HttpGet with HttpPut,
  metrics: Metrics)
    extends HttpAPIMonitor {

  override val kenshooRegistry: MetricRegistry = metrics.defaultRegistry

  def signIn(userId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[(String, String, String)] =
    http
      .POST[JsObject, HttpResponse](
        s"$baseUrl/agents-external-stubs/sign-in",
        Json.obj("planetId" -> "hmrc", "userId" -> userId))
      .map(
        response =>
          (
            response
              .header(HeaderNames.AUTHORIZATION)
              .getOrElse(throw new Exception("Missing Authorization token")),
            response.header("X-Session-ID").getOrElse(throw new Exception("Missing X-Session-ID token")),
            response.header(HeaderNames.LOCATION).getOrElse(throw new Exception("User location URI not found"))
        )
      )

  def createUser(user: User)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] =
    http
      .POST[User, HttpResponse](s"$baseUrl/agents-external-stubs/users", user)
      .map(_ => ())
      .recover {
        case e: Upstream4xxResponse if e.upstreamResponseCode == 409 => ()
      }

  def updateCurrentUser(user: User)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] =
    http
      .PUT[User, HttpResponse](s"$baseUrl/agents-external-stubs/users", user)
      .map(_ => ())
      .recover {
        case e: Upstream4xxResponse if e.upstreamResponseCode == 409 => ()
      }

  def getEnrolmentInfo(enrolmentKey: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[EnrolmentInfo] =
    http
      .GET[EnrolmentInfo](s"$baseUrl/agents-external-stubs/known-facts/${UriEncoding
        .encodePathSegment(enrolmentKey, StandardCharsets.UTF_8.name)}")

  def getBusinessDetails(
    nino: Nino)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[BusinessDetails]] =
    getWithDesHeaders[BusinessDetails](
      "getRegistrationBusinessDetailsByNino",
      new URL(baseUrl, s"/registration/business-details/nino/${encodePathSegment(nino.value)}").toString)

  def getVatCustomerInformation(
    vrn: Vrn)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[VatCustomerInfo]] = {
    val url = new URL(baseUrl, s"/vat/customer/vrn/${encodePathSegment(vrn.value)}/information")
    getWithDesHeaders[VatCustomerInfo]("GetVatCustomerInformation", url.toString)
  }

  private def getWithDesHeaders[T: HttpReads](apiName: String, url: String)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext): Future[Option[T]] = {
    val desHeaderCarrier =
      hc.copy(
        authorization = Some(Authorization(s"Bearer 123")),
        extraHeaders = hc.extraHeaders :+ "Environment" -> "test")
    http.GET[Option[T]](url)(implicitly[HttpReads[Option[T]]], desHeaderCarrier, ec)
  }

  private def encodePathSegment(pathSegment: String): String =
    UriEncoding.encodePathSegment(pathSegment, StandardCharsets.UTF_8.name)

}
