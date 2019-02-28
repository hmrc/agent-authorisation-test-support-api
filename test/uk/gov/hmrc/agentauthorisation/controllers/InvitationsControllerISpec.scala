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

import play.api.test.FakeRequest
import uk.gov.hmrc.agentauthorisation.stubs.{ACAStubs, TestIdentifiers}
import uk.gov.hmrc.agentauthorisation.support.BaseISpec

class InvitationsControllerISpec extends BaseISpec with ACAStubs with TestIdentifiers {

  val controller: InvitationsController = app.injector.instanceOf[InvitationsController]

  val fakeRequest =
    FakeRequest().withHeaders("Accept" -> s"application/vnd.hmrc.1.0+json")

  "PUT /agent-authorisation-test-support/invitations/:id" should {
    "return 204 for successfully accepting an invitation" in {
      givenGetITSAInvitationStub(arn, "Pending")
      givenAcceptInvitationStub(invitationIdITSA, mtdItId.value, "MTDITID", 204)
      val result = await(controller.acceptInvitation(invitationIdITSA)(fakeRequest))
      status(result) shouldBe 204
    }

    "return 404 for unable to find invitation to accept" in {
      givenInvitationNotFound(arn, invitationIdITSA)
      givenAcceptInvitationStub(invitationIdITSA, mtdItId.value, "MTDITID", 404)
      val result = await(controller.acceptInvitation(invitationIdITSA)(fakeRequest))
      status(result) shouldBe 404
    }

    "return 403 for unauthorised to accept invitation" in {
      givenGetITSAInvitationStub(arn, "Pending")
      givenAcceptInvitationStub(invitationIdITSA, mtdItId.value, "MTDITID", 403)
      val result = await(controller.acceptInvitation(invitationIdITSA)(fakeRequest))
      status(result) shouldBe 403
    }
  }

  "DELETE /agent-authorisation-test-support/invitations/:id" should {
    "return 204 for successfully rejecting an invitation" in {
      givenGetITSAInvitationStub(arn, "Pending")
      givenRejectInvitationStub(invitationIdITSA, mtdItId.value, "MTDITID", 204)
      val result = await(controller.rejectInvitation(invitationIdITSA)(fakeRequest))
      status(result) shouldBe 204
    }

    "return 404 for unable to find invitation to reject" in {
      givenInvitationNotFound(arn, invitationIdITSA)
      givenRejectInvitationStub(invitationIdITSA, mtdItId.value, "MTDITID", 404)
      val result = await(controller.rejectInvitation(invitationIdITSA)(fakeRequest))
      status(result) shouldBe 404
    }

    "return 403 for unauthorised to reject invitation" in {
      givenGetITSAInvitationStub(arn, "Pending")
      givenRejectInvitationStub(invitationIdITSA, mtdItId.value, "MTDITID", 403)
      val result = await(controller.rejectInvitation(invitationIdITSA)(fakeRequest))
      status(result) shouldBe 403
    }
  }

}
