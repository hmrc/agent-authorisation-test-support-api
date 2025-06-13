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

package connectors

import stubs.{ACRStubs, TestIdentifiers}
import support.BaseISpec
import uk.gov.hmrc.agentauthorisation.connectors.AgentClientRelationshipsConnector
import uk.gov.hmrc.agentauthorisation.models.Invitation
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global

class AgentClientRelationshipsConnectorISpec extends BaseISpec with ACRStubs with TestIdentifiers {

  val connector: AgentClientRelationshipsConnector = app.injector.instanceOf[AgentClientRelationshipsConnector]

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val itsaInvitation: Invitation = Invitation(
    invitationIdITSA,
    arn,
    "personal",
    mtdItId.value,
    identifierITSA,
    serviceITSA,
    "Pending"
  )

  val itsaSuppInvitation: Invitation = itsaInvitation.copy(service = serviceITSASupp)
  val altItsaInvitation: Invitation = itsaInvitation.copy(clientIdType = identifierAltITSA, clientId = nino)

  val vatInvitation: Invitation = Invitation(
    invitationIdVAT,
    arn,
    "business",
    validVrn.value,
    identifierVAT,
    serviceVAT,
    "Pending"
  )

  "getInvitation" should {

    "return an ITSA invitation" in {
      givenAuditConnector()
      givenItsaInvitationExists("Pending")
      val result = connector.getInvitation(invitationIdITSA).futureValue

      result shouldBe Some(itsaInvitation)
    }

    "return an ITSA supp invitation" in {
      givenAuditConnector()
      givenItsaSuppInvitationExists("Pending")
      val result = connector.getInvitation(invitationIdITSA).futureValue

      result shouldBe Some(itsaSuppInvitation)
    }

    "return an alt ITSA invitation" in {
      givenAuditConnector()
      givenAltItsaInvitationExists("Pending")
      val result = connector.getInvitation(invitationIdITSA).futureValue

      result shouldBe Some(altItsaInvitation)
    }

    "return a VAT invitation" in {
      givenAuditConnector()
      givenVatInvitationExists("Pending")
      val result = connector.getInvitation(invitationIdVAT).futureValue

      result shouldBe Some(vatInvitation)
    }

    "return no invitation" in {
      givenAuditConnector()
      givenInvitationNotFound(invitationIdITSA)
      val result = connector.getInvitation(invitationIdITSA).futureValue

      result shouldBe None
    }
  }

  "acceptInvitation" should {

    "return 204 when accepting is successful" in {
      givenAuditConnector()
      givenAcceptInvitation(invitationIdITSA, 204)
      val result = connector.acceptInvitation(invitationIdITSA).futureValue

      result shouldBe Some(204)
    }

    "return 404 when invitation is not found" in {
      givenAuditConnector()
      givenAcceptInvitation(invitationIdITSA, 404)
      val result = connector.acceptInvitation(invitationIdITSA).futureValue

      result shouldBe Some(404)
    }

    "return the status code of the HTTP exception when an UpstreamErrorResponse is returned" in {
      givenAuditConnector()
      givenAcceptInvitation(invitationIdITSA, 500)
      val result = connector.acceptInvitation(invitationIdITSA).futureValue

      result shouldBe Some(500)
    }
  }

  "rejectInvitation" should {

    "return 204 when rejecting is successful" in {
      givenAuditConnector()
      givenRejectInvitation(invitationIdITSA, 204)
      val result = connector.rejectInvitation(invitationIdITSA).futureValue

      result shouldBe Some(204)
    }

    "return 404 when invitation is not found" in {
      givenAuditConnector()
      givenRejectInvitation(invitationIdITSA, 404)
      val result = connector.rejectInvitation(invitationIdITSA).futureValue

      result shouldBe Some(404)
    }

    "return the status code of the HTTP exception when an UpstreamErrorResponse is returned" in {
      givenAuditConnector()
      givenRejectInvitation(invitationIdITSA, 500)
      val result = connector.rejectInvitation(invitationIdITSA).futureValue

      result shouldBe Some(500)
    }
  }

}
