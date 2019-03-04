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

import com.google.inject.Provider
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{Format, Json}
import play.api.mvc.{Action, AnyContent, Controller}
import uk.gov.hmrc.agentauthorisation.connectors.AgentsExternalStubsConnector
import uk.gov.hmrc.agentauthorisation.models.User
import uk.gov.hmrc.agentmtdidentifiers.model.Vrn
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.logging.{Authorization, SessionId}

import scala.concurrent.ExecutionContext

@Singleton
class KnownFactController @Inject()(stubsConnector: AgentsExternalStubsConnector, ecp: Provider[ExecutionContext])
    extends Controller {

  implicit val ec: ExecutionContext = ecp.get

  import KnownFactController._

  def prepareMtdVatKnownFact(vrn: Vrn): Action[AnyContent] = Action.async { implicit request =>
    val user =
      User(
        affinityGroup = "Organisation",
        principalEnrolments = Seq(User.Enrolment("HMRC-MTD-VAT", Some(Seq(User.Identifier("VRN", vrn.value))))))
    for {
      (authorizationToken, sessionId, _) <- stubsConnector
                                             .signIn("Alf")(HeaderCarrier(), ec)
      hc = HeaderCarrier(
        authorization = Some(Authorization(authorizationToken)),
        sessionId = Some(SessionId(sessionId)))
      _ <- stubsConnector.createUser(user)(hc, ec)
      vatCustomerInformation <- stubsConnector
                                 .getVatCustomerInformation(vrn)(hc, ec)
    } yield
      vatCustomerInformation.flatMap(_.effectiveRegistrationDate) match {
        case Some(date) =>
          Ok(Json.toJson(KnownFactResponse(Seq("MTD-VAT"), "business", "vrn", vrn.value, date.toString("yyyy-MM-dd"))))
        case None =>
          InternalServerError("Missing VAT Registration Date verifier")
      }
  }

  def prepareMtdItKnownFact(nino: Nino): Action[AnyContent] = Action.async { implicit request =>
    val user =
      User(
        affinityGroup = "Individual",
        nino = Some(nino),
        confidenceLevel = Some(200),
        principalEnrolments = Seq(User.Enrolment("HMRC-MTD-IT")))
    for {
      (authorizationToken, sessionId, _) <- stubsConnector
                                             .signIn("Alf")(HeaderCarrier(), ec)
      hc = HeaderCarrier(
        authorization = Some(Authorization(authorizationToken)),
        sessionId = Some(SessionId(sessionId)))
      _               <- stubsConnector.createUser(user)(hc, ec)
      businessDetails <- stubsConnector.getBusinessDetails(nino)(hc, ec)
    } yield
      businessDetails.flatMap(
        _.businessData.headOption
          .flatMap(_.businessAddressDetails.postalCode)) match {
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
    knownFact: String)

  object KnownFactResponse {
    implicit val formats: Format[KnownFactResponse] =
      Json.format[KnownFactResponse]
  }

}
