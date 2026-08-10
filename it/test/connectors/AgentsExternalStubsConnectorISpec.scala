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

import stubs.{AgentsExternalStubs, TestIdentifiers}
import support.BaseISpec
import uk.gov.hmrc.agentauthorisation.connectors.AgentsExternalStubsConnector
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, SessionId}

class AgentsExternalStubsConnectorISpec extends BaseISpec with AgentsExternalStubs with TestIdentifiers {

  val connector: AgentsExternalStubsConnector = app.injector.instanceOf[AgentsExternalStubsConnector]

  given hc: HeaderCarrier = HeaderCarrier()

  "signInAndGetSessionHeaders" should {

    "return a HeaderCarrier with Authorization and SessionId headers on successful sign-in" in {
      givenAuditConnector()
      givenUserAuthenticatedInStubs("Alf")
      val result = connector.signInAndGetSessionHeaders("Alf").futureValue

      result.authorization.map(_.value).shouldBe(Some("Bearer FOO-Alf"))
      result.sessionId.map(_.value).shouldBe(Some("BAR-Alf"))
    }
  }

  "getUserIdForNino" should {

    "return the userId for a given NINO" in {
      givenAuditConnector()
      givenUserIdForNino(nino)
      val session =
        HeaderCarrier(authorization = Some(Authorization("Bearer FOO-Alf")), sessionId = Some(SessionId("BAR-Alf")))
      val result = connector.getUserIdForNino(nino)(using session).futureValue

      result.shouldBe(userIdITSA)
    }
  }

  "getUserIdForEnrolment" should {

    "return the userId for a given ITSA enrolment key" in {
      givenAuditConnector()
      val enrolmentKey = s"HMRC-MTD-IT~MTDITID~${mtdItId.value}"
      givenClientEnrolmentExistsInStubs(enrolmentKey, userIdITSA)
      val session =
        HeaderCarrier(
          authorization = Some(Authorization(s"Bearer FOO-$userIdITSA")),
          sessionId = Some(SessionId(s"BAR-$userIdITSA"))
        )
      val result = connector.getUserIdForEnrolment(enrolmentKey)(using session).futureValue

      result.shouldBe(userIdITSA)
    }

    "return the userId for a given VAT enrolment key" in {
      givenAuditConnector()
      val enrolmentKey = s"HMRC-MTD-VAT~VRN~${validVrn.value}"
      givenClientEnrolmentExistsInStubs(enrolmentKey, "VATClient001")
      val session =
        HeaderCarrier(
          authorization = Some(Authorization("Bearer FOO-VATClient001")),
          sessionId = Some(SessionId("BAR-VATClient001"))
        )
      val result = connector.getUserIdForEnrolment(enrolmentKey)(using session).futureValue

      result.shouldBe("VATClient001")
    }
  }
}
