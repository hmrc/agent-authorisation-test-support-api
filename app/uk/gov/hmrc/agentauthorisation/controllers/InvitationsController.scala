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

import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import uk.gov.hmrc.agentauthorisation.connectors.{AgentClientRelationshipsConnector, AgentsExternalStubsConnector}
import uk.gov.hmrc.agentauthorisation.models.Invitation
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class InvitationsController @Inject() (
  agentClientRelationshipsConnector: AgentClientRelationshipsConnector,
  agentsExternalStubsConnector: AgentsExternalStubsConnector,
  controllerComponents: ControllerComponents
)(using ec: ExecutionContext)
    extends BackendController(controllerComponents) {

  private val BlankSession = HeaderCarrier()

  def acceptInvitation(inviteId: String): Action[AnyContent] = Action.async {
    for {
      (maybeInvitation, sessionHeaders) <- getInvitation(inviteId)
      result <- maybeInvitation match {
                  case None => Future.successful(NotFound)
                  case Some(invitation) =>
                    invitation.status match {
                      case "Pending"              => acceptPendingInvitation(invitation, sessionHeaders)
                      case "Rejected" | "Expired" => Future.successful(Conflict)
                      case "Accepted"             => Future.successful(NoContent)
                      case _                      => Future.successful(Forbidden)
                    }
                }
    } yield result
  }

  def rejectInvitation(id: String): Action[AnyContent] = Action.async {
    for {
      (maybeInvitation, sessionHeaders) <- getInvitation(id)
      result <- maybeInvitation match {
                  case None => Future.successful(NotFound)
                  case Some(invitation) =>
                    invitation.status match {
                      case "Pending"              => rejectPendingInvitation(invitation, sessionHeaders)
                      case "Accepted" | "Expired" => Future.successful(Conflict)
                      case "Rejected"             => Future.successful(NoContent)
                      case _                      => Future.successful(Forbidden)
                    }
                }
    } yield result
  }

  private def getInvitation(inviteId: String): Future[(Option[Invitation], HeaderCarrier)] =
    for {
      sessionHeaders  <- agentsExternalStubsConnector.signInAndGetSessionHeaders("Alf")(using BlankSession)
      maybeInvitation <- agentClientRelationshipsConnector.getInvitation(inviteId)(using sessionHeaders)
    } yield (maybeInvitation, sessionHeaders)

  private def acceptPendingInvitation(invitation: Invitation, sessionHeaders: HeaderCarrier): Future[Result] =
    for {
      userId         <- getUserId(invitation)(using sessionHeaders)
      sessionHeaders <- agentsExternalStubsConnector.signInAndGetSessionHeaders(userId)(using BlankSession)
      status <- agentClientRelationshipsConnector.acceptInvitation(invitation.invitationId)(using sessionHeaders)
    } yield statusAsResponse(status, invitation)

  private def rejectPendingInvitation(invitation: Invitation, sessionHeaders: HeaderCarrier): Future[Result] =
    for {
      userId         <- getUserId(invitation)(using sessionHeaders)
      sessionHeaders <- agentsExternalStubsConnector.signInAndGetSessionHeaders(userId)(using BlankSession)
      status <- agentClientRelationshipsConnector.rejectInvitation(invitation.invitationId)(using sessionHeaders)
    } yield statusAsResponse(status, invitation)

  private def statusAsResponse(status: Int, invitation: Invitation): Result =
    status match {
      case 204 => NoContent
      case 404 => NotFound(s"Invitation ${invitation.invitationId} for ${enrolmentKeyFor(invitation)} not found.")
      case 403 => Forbidden
      case unexpected =>
        InternalServerError(
          s"Unexpected response $unexpected for invitation ${invitation.invitationId} for ${enrolmentKeyFor(invitation)}"
        )
    }

  private def enrolmentKeyFor(invitation: Invitation): String = invitation.service match {
    case "HMRC-MTD-VAT"     => s"HMRC-MTD-VAT~VRN~${invitation.suppliedClientId}"
    case "HMRC-MTD-IT"      => s"HMRC-MTD-IT~MTDITID~${invitation.suppliedClientId}"
    case "HMRC-MTD-IT-SUPP" => s"HMRC-MTD-IT~MTDITID~${invitation.suppliedClientId}"
    case _                  => throw new Exception("Unsupported service type")
  }

  private def getUserId(invitation: Invitation)(using sessionHeaders: HeaderCarrier): Future[String] =
    invitation.suppliedClientIdType match {
      case "ni" => agentsExternalStubsConnector.getUserIdForNino(invitation.suppliedClientId)
      case _    => agentsExternalStubsConnector.getUserIdForEnrolment(enrolmentKeyFor(invitation))
    }
}
