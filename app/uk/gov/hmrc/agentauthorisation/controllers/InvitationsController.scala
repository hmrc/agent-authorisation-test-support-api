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

package uk.gov.hmrc.agentauthorisation.controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, Controller}
import uk.gov.hmrc.agentauthorisation.connectors.{AgentsExternalStubsConnector, InvitationsConnector}
import uk.gov.hmrc.agentauthorisation.models.Invitation
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.logging.{Authorization, SessionId}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class InvitationsController @Inject()(
  invitationsConnector: InvitationsConnector,
  agentsExternalStubsConnector: AgentsExternalStubsConnector)(implicit ec: ExecutionContext)
    extends Controller {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  def acceptInvitation(id: String): Action[AnyContent] = Action.async { implicit request =>
    for {
      headerCarrier(hcStubs1, url1) <- agentsExternalStubsConnector.signIn("Alf")
      maybeInvitation               <- invitationsConnector.getInvitation(id)(hcStubs1, ec)
      result1 <- maybeInvitation match {
                  case Some(invitation) =>
                    invitation.status match {
                      case "Pending" =>
                        for {
                          enrolmentInfo <- agentsExternalStubsConnector
                                            .getEnrolmentInfo(enrolmentKeyFor(invitation))(hcStubs1, ec)
                          headerCarrier(hcStubs2, url2) <- agentsExternalStubsConnector.signIn(
                                                            enrolmentInfo.user
                                                              .flatMap(_.userId)
                                                              .getOrElse(throw new Exception(
                                                                s"serId not found for an invitation ${enrolmentKeyFor(
                                                                  invitation)}")))
                          result2 <- invitationsConnector
                                      .acceptInvitation(id, invitation.clientId, invitation.clientIdType)(hcStubs2, ec)
                                      .map {
                                        case Some(204) => NoContent
                                        case Some(404) =>
                                          NotFound(
                                            s"Invitation $id for ${enrolmentKeyFor(invitation)} not found, current user $url2")
                                        case Some(403) => Forbidden
                                        case _         => InternalServerError
                                      }
                        } yield result2
                      case "Rejected" | "Expired" => Future.successful(Conflict)
                      case "Accepted"             => Future.successful(NoContent)
                      case _                      => Future.successful(Forbidden)
                    }
                  case None =>
                    Future.successful(NotFound)
                }
    } yield result1
  }

  def rejectInvitation(id: String): Action[AnyContent] = Action.async { implicit request =>
    for {
      headerCarrier(hcStubs1, url1) <- agentsExternalStubsConnector.signIn("Alf")
      maybeInvitation               <- invitationsConnector.getInvitation(id)(hcStubs1, ec)
      result1 <- maybeInvitation match {
                  case Some(invitation) =>
                    invitation.status match {
                      case "Pending" =>
                        for {
                          enrolmentInfo <- agentsExternalStubsConnector
                                            .getEnrolmentInfo(enrolmentKeyFor(invitation))(hcStubs1, ec)
                          headerCarrier(hcStubs2, url2) <- agentsExternalStubsConnector.signIn(
                                                            enrolmentInfo.user
                                                              .flatMap(_.userId)
                                                              .getOrElse(throw new Exception(
                                                                s"userId not found for an invitation ${enrolmentKeyFor(
                                                                  invitation)}")))
                          result2 <- invitationsConnector
                                      .rejectInvitation(id, invitation.clientId, invitation.clientIdType)(hcStubs2, ec)
                                      .map {
                                        case Some(204) => NoContent
                                        case Some(404) =>
                                          NotFound(
                                            s"Invitation $id for ${enrolmentKeyFor(invitation)} not found, current user $url2")
                                        case Some(403) => Forbidden
                                        case _         => InternalServerError
                                      }
                        } yield result2
                      case "Accepted" | "Expired" => Future.successful(Conflict)
                      case "Rejected"             => Future.successful(NoContent)
                      case _                      => Future.successful(Forbidden)
                    }
                  case None =>
                    Future.successful(NotFound)
                }
    } yield result1
  }

  def enrolmentKeyFor(invitation: Invitation): String = invitation.service match {
    case "HMRC-MTD-VAT" => s"HMRC-MTD-VAT~VRN~${invitation.clientId}"
    case "HMRC-MTD-IT"  => s"HMRC-MTD-IT~MTDITID~${invitation.clientId}"
    case _              => throw new Exception("Unsupported service type")
  }

  object headerCarrier {
    def unapply(arg: (String, String, String)): Option[(HeaderCarrier, String)] =
      Some((HeaderCarrier(authorization = Some(Authorization(arg._1)), sessionId = Some(SessionId(arg._2))), arg._3))
  }

}
