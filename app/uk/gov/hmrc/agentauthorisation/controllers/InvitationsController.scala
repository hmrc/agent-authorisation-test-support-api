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

import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.agentauthorisation.connectors.{AgentClientRelationshipsConnector, AgentsExternalStubsConnector}
import uk.gov.hmrc.agentauthorisation.models.Invitation
import uk.gov.hmrc.agentauthorisation.models.{NinoType, Service}
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, SessionId}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class InvitationsController @Inject() (
  agentClientRelationshipsConnector: AgentClientRelationshipsConnector,
  agentsExternalStubsConnector: AgentsExternalStubsConnector,
  controllerComponents: ControllerComponents
)(implicit ec: ExecutionContext)
    extends BackendController(controllerComponents) {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  def acceptInvitation(id: String): Action[AnyContent] = Action.async {
    for {
      headerCarrier(hcStubs1, _) <- agentsExternalStubsConnector.signIn("Alf")
      maybeInvitation            <- agentClientRelationshipsConnector.getInvitation(id)(hcStubs1, ec)
      result1 <- maybeInvitation match {
                   case Some(invitation) =>
                     invitation.status match {
                       case "Pending" =>
                         for {
                           userId                        <- getUserId(invitation)(hcStubs1, ec)
                           headerCarrier(hcStubs2, url2) <- agentsExternalStubsConnector.signIn(userId)
                           result2 <-
                             agentClientRelationshipsConnector
                               .acceptInvitation(id)(hcStubs2, ec)
                               .map {
                                 case Some(204) => NoContent
                                 case Some(404) =>
                                   NotFound(
                                     s"Invitation $id for ${enrolmentKeyFor(invitation)} not found, current user $url2"
                                   )
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

  def rejectInvitation(id: String): Action[AnyContent] = Action.async {
    for {
      headerCarrier(hcStubs1, _) <- agentsExternalStubsConnector.signIn("Alf")
      maybeInvitation            <- agentClientRelationshipsConnector.getInvitation(id)(hcStubs1, ec)
      result1 <- maybeInvitation match {
                   case Some(invitation) =>
                     invitation.status match {
                       case "Pending" =>
                         for {
                           userId                        <- getUserId(invitation)(hcStubs1, ec)
                           headerCarrier(hcStubs2, url2) <- agentsExternalStubsConnector.signIn(userId)
                           result2 <-
                             agentClientRelationshipsConnector
                               .rejectInvitation(id)(hcStubs2, ec)
                               .map {
                                 case Some(204) => NoContent
                                 case Some(404) =>
                                   NotFound(
                                     s"Invitation $id for ${enrolmentKeyFor(invitation)} not found, current user $url2"
                                   )
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
    case "HMRC-MTD-VAT"     => s"HMRC-MTD-VAT~VRN~${invitation.clientId}"
    case "HMRC-MTD-IT"      => s"HMRC-MTD-IT~MTDITID~${invitation.clientId}"
    case "HMRC-MTD-IT-SUPP" => s"HMRC-MTD-IT~MTDITID~${invitation.clientId}"
    case _                  => throw new Exception("Unsupported service type")
  }

  private def getUserId(invitation: Invitation)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[String] =
    if (isAltItsa(invitation)) agentsExternalStubsConnector.getUserIdForNino(invitation.clientId)
    else agentsExternalStubsConnector.getUserIdForEnrolment(enrolmentKeyFor(invitation))

  private def isAltItsa(i: Invitation): Boolean = (i.service, i.clientIdType) match {
    case (Service.MtdIt.id | Service.MtdItSupp.id, NinoType.id) => true
    case _                                                      => false
  }

  object headerCarrier {
    def unapply(arg: (String, String, String)): Option[(HeaderCarrier, String)] =
      Some((HeaderCarrier(authorization = Some(Authorization(arg._1)), sessionId = Some(SessionId(arg._2))), arg._3))
  }

}
