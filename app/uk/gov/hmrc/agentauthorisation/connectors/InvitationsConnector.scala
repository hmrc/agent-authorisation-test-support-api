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

package uk.gov.hmrc.agentauthorisation.connectors

import java.net.URL

import com.codahale.metrics.MetricRegistry
import com.kenshoo.play.metrics.Metrics
import javax.inject.{Inject, Named, Singleton}
import org.joda.time.LocalDate
import org.joda.time.format.ISODateTimeFormat
import uk.gov.hmrc.agent.kenshoo.monitoring.HttpAPIMonitor
import uk.gov.hmrc.agentauthorisation.models.Invitation
import uk.gov.hmrc.agentmtdidentifiers.model.Vrn
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class InvitationsConnector @Inject()(
  @Named("agent-client-authorisation-baseUrl") baseUrl: URL,
  http: HttpPost with HttpGet with HttpPut,
  metrics: Metrics)
    extends HttpAPIMonitor {

  override val kenshooRegistry: MetricRegistry = metrics.defaultRegistry
  private val dateFormatter = ISODateTimeFormat.date()

  def checkPostcodeForClient(nino: Nino, postcode: String)(
    implicit
    hc: HeaderCarrier,
    ec: ExecutionContext): Future[Option[Boolean]] =
    monitor(s"ConsumedAPI-CheckPostcode-GET") {
      http
        .GET[HttpResponse](
          new URL(
            baseUrl,
            s"/agent-client-authorisation/known-facts/individuals/nino/${nino.value}/sa/postcode/$postcode").toString)
        .map(_ => Some(true))
    }.recover {
      case notMatched: Upstream4xxResponse if notMatched.message.contains("POSTCODE_DOES_NOT_MATCH") =>
        Some(false)
      case notEnrolled: Upstream4xxResponse if notEnrolled.message.contains("CLIENT_REGISTRATION_NOT_FOUND") =>
        None
    }

  def checkVatRegDateForClient(vrn: Vrn, registrationDateKnownFact: LocalDate)(
    implicit
    hc: HeaderCarrier,
    ec: ExecutionContext): Future[Option[Boolean]] =
    monitor(s"ConsumedAPI-CheckVatRegDate-GET") {
      http
        .GET[HttpResponse](
          new URL(
            baseUrl,
            s"/agent-client-authorisation/known-facts/organisations/vat/${vrn.value}/registration-date/${dateFormatter
              .print(registrationDateKnownFact)}").toString)
        .map(_ => Some(true))
    }.recover {
      case ex: Upstream4xxResponse if ex.upstreamResponseCode == 403 =>
        Some(false)
      case _: NotFoundException => None
    }

  def getInvitation(invitationId: String)(
    implicit
    headerCarrier: HeaderCarrier,
    executionContext: ExecutionContext) =
    monitor(s"ConsumedAPI-Get-Invitation-GET") {
      http.GET[Option[Invitation]](new URL(baseUrl, s"/agent-client-authorisation/invitations/$invitationId").toString)
    }.recoverWith {
      case _: NotFoundException => Future successful None
    }

  def acceptInvitation(invitationId: String, clientIdentifier: String, clientIdentifierType: String)(
    implicit
    headerCarrier: HeaderCarrier,
    executionContext: ExecutionContext): Future[Option[Int]] =
    monitor(s"ConsumedAPI-Accept-Invitation-PUT") {
      http
        .PUT[String, HttpResponse](
          new URL(
            baseUrl,
            s"/agent-client-authorisation/clients/${clientIdentifierType.toUpperCase}/$clientIdentifier/invitations/received/$invitationId/accept").toString,
          ""
        )
        .map(response => Some(response.status))
    }.recover {
      case _: NotFoundException    => Some(404)
      case ex: Upstream4xxResponse => Some(ex.upstreamResponseCode)
      case _                       => Some(403)
    }

  def rejectInvitation(invitationId: String, clientIdentifier: String, clientIdentifierType: String)(
    implicit
    headerCarrier: HeaderCarrier,
    executionContext: ExecutionContext): Future[Option[Int]] =
    monitor(s"ConsumedAPI-Reject-Invitation-PUT") {
      http
        .PUT[String, HttpResponse](
          new URL(
            baseUrl,
            s"/agent-client-authorisation/clients/${clientIdentifierType.toUpperCase}/$clientIdentifier/invitations/received/$invitationId/reject").toString,
          ""
        )
        .map(response => Some(response.status))
    }.recover {
      case _: NotFoundException    => Some(404)
      case ex: Upstream4xxResponse => Some(ex.upstreamResponseCode)
      case _                       => Some(403)
    }
}
