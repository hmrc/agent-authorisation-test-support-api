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

import com.google.inject.Provider
import play.api.libs.json.{Format, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.agentauthorisation.connectors.AgentsExternalStubsConnector
import uk.gov.hmrc.agentauthorisation.models.{EnrolmentKey, Identifier, User}
import uk.gov.hmrc.agentmtdidentifiers.model.Vrn
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, SessionId}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class KnownFactController @Inject() (
  stubsConnector: AgentsExternalStubsConnector,
  ecp: Provider[ExecutionContext],
  controllerComponents: ControllerComponents
) extends BackendController(controllerComponents) {

  implicit val ec: ExecutionContext = ecp.get

  import KnownFactController._

  private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  private val Individual = "Individual"

  def prepareMtdVatKnownFact(vrn: Vrn): Action[AnyContent] = Action.async {
    val user =
      User(
        userId = null,
        assignedPrincipalEnrolments = Seq(EnrolmentKey("HMRC-MTD-VAT", Seq(Identifier("VRN", vrn.value))))
      )
    for {
      (authorizationToken, sessionId, _) <- stubsConnector
                                              .signIn("Alf")(HeaderCarrier(), ec)
      hc =
        HeaderCarrier(authorization = Some(Authorization(authorizationToken)), sessionId = Some(SessionId(sessionId)))
      _ <- stubsConnector.createUser(user, Individual)(hc, ec)
      vatCustomerInformation <- stubsConnector
                                  .getVatCustomerInformation(vrn)(hc, ec)
    } yield vatCustomerInformation.flatMap(_.effectiveRegistrationDate) match {
      case Some(date) =>
        Ok(Json.toJson(KnownFactResponse(Seq("MTD-VAT"), "personal", "vrn", vrn.value, date.format(dateFormat))))
      case None =>
        InternalServerError("Missing VAT Registration Date verifier")
    }
  }

  def prepareMtdItKnownFact(nino: Nino): Action[AnyContent] = Action.async {
    val user =
      User(
        userId = null,
        nino = Some(nino),
        confidenceLevel = Some(250),
        assignedPrincipalEnrolments = Seq(EnrolmentKey("HMRC-MTD-IT", Seq.empty))
      )
    for {
      (authorizationToken, sessionId, _) <- stubsConnector
                                              .signIn("Alf")(HeaderCarrier(), ec)
      hc =
        HeaderCarrier(authorization = Some(Authorization(authorizationToken)), sessionId = Some(SessionId(sessionId)))
      _               <- stubsConnector.createUser(user, Individual)(hc, ec)
      businessDetails <- stubsConnector.getBusinessDetails(nino)(hc, ec)
    } yield businessDetails.flatMap(
      _.businessData.headOption
        .flatMap(_.businessAddressDetails.postalCode)
    ) match {
      case Some(postcode) =>
        Ok(Json.toJson(KnownFactResponse(Seq("MTD-IT"), "personal", "nino", nino.value, postcode)))
      case None => InternalServerError("Missing business postcode verifier")
    }
  }

}

object KnownFactController {

  case class KnownFactResponse(
    service: Seq[String],
    clientType: String,
    clientIdType: String,
    clientId: String,
    knownFact: String
  )

  object KnownFactResponse {
    implicit val formats: Format[KnownFactResponse] =
      Json.format[KnownFactResponse]
  }

}
