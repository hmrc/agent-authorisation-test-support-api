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

package controllers

import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import stubs.{ACRStubs, AgentsExternalStubs, TestIdentifiers}
import support.BaseISpec
import uk.gov.hmrc.agentauthorisation.controllers.InvitationsController

class InvitationsControllerISpec extends BaseISpec with ACRStubs with AgentsExternalStubs with TestIdentifiers {

  val controller: InvitationsController = app.injector.instanceOf[InvitationsController]

  val fakeRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest().withHeaders("Accept" -> s"application/vnd.hmrc.1.0+json")

  "PUT /agent-authorisation-test-support/invitations/:id" when {

    "the regime type is Income Tax (main agent)" should {

      "return 204 for successfully accepting an invitation" in {
        givenITSAUserAuthenticatedInStubs()
        givenItsaInvitationExists("Pending")
        givenAcceptInvitation(invitationIdITSA, 204)
        val result = controller.acceptInvitation(invitationIdITSA)(fakeRequest)
        status(result) shouldBe 204
      }

      "return 204 for successfully accepting an invitation (alt ITSA)" in {
        givenITSAUserAuthenticatedInStubs()
        givenAltItsaInvitationExists("Pending")
        givenUserIdForNino(nino)
        givenAcceptInvitation(invitationIdITSA, 204)
        val result = controller.acceptInvitation(invitationIdITSA)(fakeRequest)
        status(result) shouldBe 204
      }

      "return 204 for accepting already accepted invitation" in {
        givenITSAUserAuthenticatedInStubs()
        givenItsaInvitationExists("Accepted")
        val result = controller.acceptInvitation(invitationIdITSA)(fakeRequest)
        status(result) shouldBe 204
      }

      "return 409 for accepting already rejected invitation" in {
        givenITSAUserAuthenticatedInStubs()
        givenItsaInvitationExists("Rejected")
        val result = controller.acceptInvitation(invitationIdITSA)(fakeRequest)
        status(result) shouldBe 409
      }

      "return 409 for accepting already expired invitation" in {
        givenITSAUserAuthenticatedInStubs()
        givenItsaInvitationExists("Expired")
        val result = controller.acceptInvitation(invitationIdITSA)(fakeRequest)
        status(result) shouldBe 409
      }

      "return 404 for unable to find invitation in request to fetch invitation" in {
        givenITSAUserAuthenticatedInStubs()
        givenInvitationNotFound(invitationIdITSA)
        val result = controller.acceptInvitation(invitationIdITSA)(fakeRequest)
        status(result) shouldBe 404
      }

      "return 404 for unable to find invitation in request to accept invitation" in {
        givenITSAUserAuthenticatedInStubs()
        givenItsaInvitationExists("Pending")
        givenUserIdForNino(nino)
        givenAcceptInvitation(invitationIdITSA, 404)
        val result = controller.acceptInvitation(invitationIdITSA)(fakeRequest)
        status(result) shouldBe 404
      }

      "return 403 for unauthorised to accept invitation" in {
        givenITSAUserAuthenticatedInStubs()
        givenItsaInvitationExists("Pending")
        givenAcceptInvitation(invitationIdITSA, 403)
        val result = controller.acceptInvitation(invitationIdITSA)(fakeRequest)
        status(result) shouldBe 403
      }

      "return 403 for an unexpected invitation status" in {
        givenITSAUserAuthenticatedInStubs()
        givenItsaInvitationExists("Confused")
        val result = controller.acceptInvitation(invitationIdITSA)(fakeRequest)
        status(result) shouldBe 403
      }

      "return 500 when an unexpected status is received in request to accept invitation" in {
        givenITSAUserAuthenticatedInStubs()
        givenItsaInvitationExists("Pending")
        givenUserIdForNino(nino)
        givenAcceptInvitation(invitationIdITSA, 499)
        val result = controller.acceptInvitation(invitationIdITSA)(fakeRequest)
        status(result) shouldBe 500
      }
    }

    "the regime type is Income Tax (supporting agent)" should {

      "return 204 for successfully accepting an invitation" in {
        givenITSASuppUserAuthenticatedInStubs()
        givenItsaSuppInvitationExists("Pending")
        givenAcceptInvitation(invitationIdITSA, 204)
        val result = controller.acceptInvitation(invitationIdITSA)(fakeRequest)
        status(result) shouldBe 204
      }

      "return 204 for successfully accepting an invitation (alt ITSA)" in {
        givenITSASuppUserAuthenticatedInStubs()
        givenAltItsaSuppInvitationExists("Pending")
        givenUserIdForNino(nino)
        givenAcceptInvitation(invitationIdITSA, 204)
        val result = controller.acceptInvitation(invitationIdITSA)(fakeRequest)
        status(result) shouldBe 204
      }

      "return 204 for accepting already accepted invitation" in {
        givenITSASuppUserAuthenticatedInStubs()
        givenItsaSuppInvitationExists("Accepted")
        val result = controller.acceptInvitation(invitationIdITSA)(fakeRequest)
        status(result) shouldBe 204
      }

      "return 409 for accepting already rejected invitation" in {
        givenITSASuppUserAuthenticatedInStubs()
        givenItsaSuppInvitationExists("Rejected")
        val result = controller.acceptInvitation(invitationIdITSA)(fakeRequest)
        status(result) shouldBe 409
      }

      "return 409 for accepting already expired invitation" in {
        givenITSASuppUserAuthenticatedInStubs()
        givenItsaSuppInvitationExists("Expired")
        val result = controller.acceptInvitation(invitationIdITSA)(fakeRequest)
        status(result) shouldBe 409
      }

      "return 404 for unable to find invitation in request to fetch invitation" in {
        givenITSASuppUserAuthenticatedInStubs()
        givenInvitationNotFound(invitationIdITSA)
        val result = controller.acceptInvitation(invitationIdITSA)(fakeRequest)
        status(result) shouldBe 404
      }

      "return 404 for unable to find invitation in request to accept invitation" in {
        givenITSASuppUserAuthenticatedInStubs()
        givenItsaSuppInvitationExists("Pending")
        givenUserIdForNino(nino)
        givenAcceptInvitation(invitationIdITSA, 404)
        val result = controller.acceptInvitation(invitationIdITSA)(fakeRequest)
        status(result) shouldBe 404
      }

      "return 403 for unauthorised to accept invitation" in {
        givenITSASuppUserAuthenticatedInStubs()
        givenItsaSuppInvitationExists("Pending")
        givenAcceptInvitation(invitationIdITSA, 403)
        val result = controller.acceptInvitation(invitationIdITSA)(fakeRequest)
        status(result) shouldBe 403
      }

      "return 403 for an unexpected invitation status" in {
        givenITSASuppUserAuthenticatedInStubs()
        givenItsaSuppInvitationExists("Confused")
        val result = controller.acceptInvitation(invitationIdITSA)(fakeRequest)
        status(result) shouldBe 403
      }

      "return 500 when an unexpected status is received in request to accept invitation" in {
        givenITSASuppUserAuthenticatedInStubs()
        givenItsaSuppInvitationExists("Pending")
        givenUserIdForNino(nino)
        givenAcceptInvitation(invitationIdITSA, 499)
        val result = controller.acceptInvitation(invitationIdITSA)(fakeRequest)
        status(result) shouldBe 500
      }
    }

    "the regime type is VAT" should {

      "return 204 for successfully accepting an invitation" in {
        givenVATUserAuthenticatedInStubs()
        givenVatInvitationExists("Pending")
        givenAcceptInvitation(invitationIdVAT, 204)
        val result = controller.acceptInvitation(invitationIdVAT)(fakeRequest)
        status(result) shouldBe 204
      }

      "return 204 for accepting already accepted invitation" in {
        givenVATUserAuthenticatedInStubs()
        givenVatInvitationExists("Accepted")
        val result = controller.acceptInvitation(invitationIdVAT)(fakeRequest)
        status(result) shouldBe 204
      }

      "return 409 for accepting already rejected invitation" in {
        givenVATUserAuthenticatedInStubs()
        givenVatInvitationExists("Rejected")
        val result = controller.acceptInvitation(invitationIdVAT)(fakeRequest)
        status(result) shouldBe 409
      }

      "return 409 for accepting already expired invitation" in {
        givenVATUserAuthenticatedInStubs()
        givenVatInvitationExists("Expired")
        val result = controller.acceptInvitation(invitationIdVAT)(fakeRequest)
        status(result) shouldBe 409
      }

      "return 404 for unable to find invitation in request to fetch invitation" in {
        givenVATUserAuthenticatedInStubs()
        givenInvitationNotFound(invitationIdVAT)
        val result = controller.acceptInvitation(invitationIdVAT)(fakeRequest)
        status(result) shouldBe 404
      }

      "return 404 for unable to find invitation in request to accept invitation" in {
        givenVATUserAuthenticatedInStubs()
        givenVatInvitationExists("Pending")
        givenAcceptInvitation(invitationIdVAT, 404)
        val result = controller.acceptInvitation(invitationIdVAT)(fakeRequest)
        status(result) shouldBe 404
      }

      "return 403 for unauthorised to accept invitation" in {
        givenVATUserAuthenticatedInStubs()
        givenVatInvitationExists("Pending")
        givenAcceptInvitation(invitationIdVAT, 403)
        val result = controller.acceptInvitation(invitationIdVAT)(fakeRequest)
        status(result) shouldBe 403
      }

      "return 403 for an unexpected invitation status" in {
        givenVATUserAuthenticatedInStubs()
        givenVatInvitationExists("Confused")
        val result = controller.acceptInvitation(invitationIdVAT)(fakeRequest)
        status(result) shouldBe 403
      }

      "return 500 when an unexpected status is received in request to accept invitation" in {
        givenVATUserAuthenticatedInStubs()
        givenVatInvitationExists("Pending")
        givenAcceptInvitation(invitationIdVAT, 499)
        val result = controller.acceptInvitation(invitationIdVAT)(fakeRequest)
        status(result) shouldBe 500
      }
    }

    "the regime type is not supported" should {

      "throw an exception detailing that there was an unsupported service type" in {
        givenUnsupportedRegimeUserInStubs()
        givenUnsupportedInvitationExists
        val result = controller.acceptInvitation(invalidInvitationId)(fakeRequest)
        intercept[Exception](status(result)).getMessage shouldBe "Unsupported service type"
      }
    }
  }

  "DELETE /agent-authorisation-test-support/invitations/:id" when {

    "the regime type is Income Tax (main agent)" should {

      "return 204 for successfully rejecting an invitation" in {
        givenITSAUserAuthenticatedInStubs()
        givenItsaInvitationExists("Pending")
        givenRejectInvitation(invitationIdITSA, 204)
        val result = controller.rejectInvitation(invitationIdITSA)(fakeRequest)
        status(result) shouldBe 204
      }

      "return 204 for successfully rejecting an invitation (alt ITSA)" in {
        givenITSAUserAuthenticatedInStubs()
        givenAltItsaInvitationExists("Pending")
        givenUserIdForNino(nino)
        givenRejectInvitation(invitationIdITSA, 204)
        val result = controller.rejectInvitation(invitationIdITSA)(fakeRequest)
        status(result) shouldBe 204
      }

      "return 204 for accepting already rejected invitation" in {
        givenITSAUserAuthenticatedInStubs()
        givenItsaInvitationExists("Rejected")
        val result = controller.rejectInvitation(invitationIdITSA)(fakeRequest)
        status(result) shouldBe 204
      }

      "return 409 for accepting already accepted invitation" in {
        givenITSAUserAuthenticatedInStubs()
        givenItsaInvitationExists("Accepted")
        val result = controller.rejectInvitation(invitationIdITSA)(fakeRequest)
        status(result) shouldBe 409
      }

      "return 409 for accepting already expired invitation" in {
        givenITSAUserAuthenticatedInStubs()
        givenItsaInvitationExists("Expired")
        val result = controller.rejectInvitation(invitationIdITSA)(fakeRequest)
        status(result) shouldBe 409
      }

      "return 404 for unable to find invitation in request to fetch invitation" in {
        givenITSAUserAuthenticatedInStubs()
        givenInvitationNotFound(invitationIdITSA)
        val result = controller.rejectInvitation(invitationIdITSA)(fakeRequest)
        status(result) shouldBe 404
      }

      "return 404 for unable to find invitation in request to reject invitation" in {
        givenITSAUserAuthenticatedInStubs()
        givenItsaInvitationExists("Pending")
        givenUserIdForNino(nino)
        givenRejectInvitation(invitationIdITSA, 404)
        val result = controller.rejectInvitation(invitationIdITSA)(fakeRequest)
        status(result) shouldBe 404
      }

      "return 403 for unauthorised to reject invitation" in {
        givenITSAUserAuthenticatedInStubs()
        givenItsaInvitationExists("Pending")
        givenRejectInvitation(invitationIdITSA, 403)
        val result = controller.rejectInvitation(invitationIdITSA)(fakeRequest)
        status(result) shouldBe 403
      }

      "return 403 for an unexpected invitation status" in {
        givenITSAUserAuthenticatedInStubs()
        givenItsaInvitationExists("Confused")
        val result = controller.rejectInvitation(invitationIdITSA)(fakeRequest)
        status(result) shouldBe 403
      }

      "return 500 when an unexpected status is received in request to reject invitation" in {
        givenITSAUserAuthenticatedInStubs()
        givenItsaInvitationExists("Pending")
        givenUserIdForNino(nino)
        givenRejectInvitation(invitationIdITSA, 499)
        val result = controller.rejectInvitation(invitationIdITSA)(fakeRequest)
        status(result) shouldBe 500
      }
    }

    "the regime type is Income Tax (supporting agent)" should {

      "return 204 for successfully rejecting an invitation" in {
        givenITSASuppUserAuthenticatedInStubs()
        givenItsaSuppInvitationExists("Pending")
        givenRejectInvitation(invitationIdITSA, 204)
        val result = controller.rejectInvitation(invitationIdITSA)(fakeRequest)
        status(result) shouldBe 204
      }

      "return 204 for successfully rejecting an invitation (alt ITSA)" in {
        givenITSASuppUserAuthenticatedInStubs()
        givenAltItsaSuppInvitationExists("Pending")
        givenUserIdForNino(nino)
        givenRejectInvitation(invitationIdITSA, 204)
        val result = controller.rejectInvitation(invitationIdITSA)(fakeRequest)
        status(result) shouldBe 204
      }

      "return 204 for accepting already rejected invitation" in {
        givenITSASuppUserAuthenticatedInStubs()
        givenItsaSuppInvitationExists("Rejected")
        val result = controller.rejectInvitation(invitationIdITSA)(fakeRequest)
        status(result) shouldBe 204
      }

      "return 409 for accepting already accepted invitation" in {
        givenITSASuppUserAuthenticatedInStubs()
        givenItsaSuppInvitationExists("Accepted")
        val result = controller.rejectInvitation(invitationIdITSA)(fakeRequest)
        status(result) shouldBe 409
      }

      "return 409 for accepting already expired invitation" in {
        givenITSASuppUserAuthenticatedInStubs()
        givenItsaSuppInvitationExists("Expired")
        val result = controller.rejectInvitation(invitationIdITSA)(fakeRequest)
        status(result) shouldBe 409
      }

      "return 404 for unable to find invitation in request to fetch invitation" in {
        givenITSASuppUserAuthenticatedInStubs()
        givenInvitationNotFound(invitationIdITSA)
        val result = controller.rejectInvitation(invitationIdITSA)(fakeRequest)
        status(result) shouldBe 404
      }

      "return 404 for unable to find invitation in request to reject invitation" in {
        givenITSASuppUserAuthenticatedInStubs()
        givenItsaSuppInvitationExists("Pending")
        givenUserIdForNino(nino)
        givenRejectInvitation(invitationIdITSA, 404)
        val result = controller.rejectInvitation(invitationIdITSA)(fakeRequest)
        status(result) shouldBe 404
      }

      "return 403 for unauthorised to reject invitation" in {
        givenITSASuppUserAuthenticatedInStubs()
        givenItsaSuppInvitationExists("Pending")
        givenRejectInvitation(invitationIdITSA, 403)
        val result = controller.rejectInvitation(invitationIdITSA)(fakeRequest)
        status(result) shouldBe 403
      }

      "return 403 for an unexpected invitation status" in {
        givenITSASuppUserAuthenticatedInStubs()
        givenItsaSuppInvitationExists("Confused")
        val result = controller.rejectInvitation(invitationIdITSA)(fakeRequest)
        status(result) shouldBe 403
      }

      "return 500 when an unexpected status is received in request to reject invitation" in {
        givenITSASuppUserAuthenticatedInStubs()
        givenItsaSuppInvitationExists("Pending")
        givenUserIdForNino(nino)
        givenRejectInvitation(invitationIdITSA, 499)
        val result = controller.rejectInvitation(invitationIdITSA)(fakeRequest)
        status(result) shouldBe 500
      }
    }

    "the regime type is VAT" should {

      "return 204 for successfully rejecting an invitation" in {
        givenVATUserAuthenticatedInStubs()
        givenVatInvitationExists("Pending")
        givenRejectInvitation(invitationIdVAT, 204)
        val result = controller.rejectInvitation(invitationIdVAT)(fakeRequest)
        status(result) shouldBe 204
      }

      "return 204 for accepting already rejected invitation" in {
        givenVATUserAuthenticatedInStubs()
        givenVatInvitationExists("Rejected")
        val result = controller.rejectInvitation(invitationIdVAT)(fakeRequest)
        status(result) shouldBe 204
      }

      "return 409 for accepting already accepted invitation" in {
        givenVATUserAuthenticatedInStubs()
        givenVatInvitationExists("Accepted")
        val result = controller.rejectInvitation(invitationIdVAT)(fakeRequest)
        status(result) shouldBe 409
      }

      "return 409 for accepting already expired invitation" in {
        givenVATUserAuthenticatedInStubs()
        givenVatInvitationExists("Expired")
        val result = controller.rejectInvitation(invitationIdVAT)(fakeRequest)
        status(result) shouldBe 409
      }

      "return 404 for unable to find invitation in request to fetch invitation" in {
        givenVATUserAuthenticatedInStubs()
        givenInvitationNotFound(invitationIdVAT)
        val result = controller.rejectInvitation(invitationIdVAT)(fakeRequest)
        status(result) shouldBe 404
      }

      "return 404 for unable to find invitation in request to reject invitation" in {
        givenVATUserAuthenticatedInStubs()
        givenVatInvitationExists("Pending")
        givenRejectInvitation(invitationIdVAT, 404)
        val result = controller.rejectInvitation(invitationIdVAT)(fakeRequest)
        status(result) shouldBe 404
      }

      "return 403 for unauthorised to reject invitation" in {
        givenVATUserAuthenticatedInStubs()
        givenVatInvitationExists("Pending")
        givenRejectInvitation(invitationIdVAT, 403)
        val result = controller.rejectInvitation(invitationIdVAT)(fakeRequest)
        status(result) shouldBe 403
      }

      "return 403 for an unexpected invitation status" in {
        givenVATUserAuthenticatedInStubs()
        givenVatInvitationExists("Confused")
        val result = controller.rejectInvitation(invitationIdVAT)(fakeRequest)
        status(result) shouldBe 403
      }

      "return 500 when an unexpected status is received in request to reject invitation" in {
        givenVATUserAuthenticatedInStubs()
        givenVatInvitationExists("Pending")
        givenRejectInvitation(invitationIdVAT, 499)
        val result = controller.rejectInvitation(invitationIdVAT)(fakeRequest)
        status(result) shouldBe 500
      }
    }

    "the regime type is not supported" should {

      "throw an exception detailing that there was an unsupported service type" in {
        givenUnsupportedRegimeUserInStubs()
        givenUnsupportedInvitationExists
        val result = controller.rejectInvitation(invalidInvitationId)(fakeRequest)
        intercept[Exception](status(result)).getMessage shouldBe "Unsupported service type"
      }
    }
  }
}
