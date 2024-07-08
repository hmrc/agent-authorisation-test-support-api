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

import java.net.URL
import javax.inject.{Inject, Named, Singleton}
import java.time.format.DateTimeFormatter
import uk.gov.hmrc.agentauthorisation.models.Invitation
import uk.gov.hmrc.agentauthorisation.util.HttpAPIMonitor
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.play.bootstrap.metrics.Metrics

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class InvitationsConnector @Inject() (
  @Named("agent-client-authorisation-baseUrl") baseUrl: URL,
  http: HttpPost with HttpGet with HttpPut,
  val metrics: Metrics
)(implicit val ec: ExecutionContext)
    extends HttpAPIMonitor {

  def getInvitation(invitationId: String)(implicit headerCarrier: HeaderCarrier, ec: ExecutionContext) =
    monitor(s"ConsumedAPI-Get-Invitation-GET") {
      http.GET[Option[Invitation]](new URL(baseUrl, s"/agent-client-authorisation/invitations/$invitationId").toString)
    }.recoverWith { case _: NotFoundException =>
      Future successful None
    }

  def acceptInvitation(invitationId: String, clientIdentifier: String, clientIdentifierType: String)(implicit
    headerCarrier: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Option[Int]] =
    monitor(s"ConsumedAPI-Accept-Invitation-PUT") {
      http
        .PUT[String, HttpResponse](
          new URL(
            baseUrl,
            s"/agent-client-authorisation/clients/${clientIdentifierType.toUpperCase}/$clientIdentifier/invitations/received/$invitationId/accept"
          ).toString,
          ""
        )
        .map(response => Some(response.status))
    }.recover {
      case _: NotFoundException      => Some(404)
      case ex: UpstreamErrorResponse => Some(ex.statusCode)
      case _                         => Some(403)
    }

  def rejectInvitation(invitationId: String, clientIdentifier: String, clientIdentifierType: String)(implicit
    headerCarrier: HeaderCarrier,
    executionContext: ExecutionContext
  ): Future[Option[Int]] =
    monitor(s"ConsumedAPI-Reject-Invitation-PUT") {
      http
        .PUT[String, HttpResponse](
          new URL(
            baseUrl,
            s"/agent-client-authorisation/clients/${clientIdentifierType.toUpperCase}/$clientIdentifier/invitations/received/$invitationId/reject"
          ).toString,
          ""
        )
        .map(response => Some(response.status))
    }.recover {
      case _: NotFoundException      => Some(404)
      case ex: UpstreamErrorResponse => Some(ex.statusCode)
      case _                         => Some(403)
    }
}
