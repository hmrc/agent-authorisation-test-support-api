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
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class InvitationsController @Inject()(
  invitationsConnector: InvitationsConnector,
  agentsExternalStubsConnector: AgentsExternalStubsConnector)(implicit ec: ExecutionContext)
    extends Controller {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  def acceptInvitation(id: String): Action[AnyContent] = Action.async { implicit request =>
    for {
      maybeInvitation <- invitationsConnector.getInvitation(id)
      result <- maybeInvitation match {
                 case Some(invitation) =>
                   invitationsConnector
                     .acceptInvitation(id, invitation.clientId, invitation.clientIdType)
                     .map {
                       case Some(204) => NoContent
                       case Some(404) => NotFound
                       case Some(403) => Forbidden
                     }
                 case None =>
                   Future.successful(NotFound)
               }
    } yield result
  }

  def rejectInvitation(id: String): Action[AnyContent] = Action.async { implicit request =>
    Future.successful(NotImplemented)
  }

}
