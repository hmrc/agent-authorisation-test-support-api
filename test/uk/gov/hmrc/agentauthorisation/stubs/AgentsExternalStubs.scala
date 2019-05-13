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

package uk.gov.hmrc.agentauthorisation.stubs
import com.github.tomakehurst.wiremock.client.WireMock._

trait AgentsExternalStubs {
  self: TestIdentifiers =>

  def givenITSAUserAuthenticatedInStubs(): Unit = {
    givenUserAuthenticatedInStubs("Alf")
    givenClientEnrolmentExistsInStubs(s"HMRC-MTD-IT~MTDITID~${mtdItId.value}", "ITSAClient001")
    givenUserAuthenticatedInStubs("ITSAClient001")
  }

  def givenUserAuthenticatedInStubs(userId: String): Unit =
    stubFor(
      post(urlEqualTo(s"/agents-external-stubs/sign-in"))
        .withRequestBody(equalToJson(s"""{"planetId":"hmrc","userId":"$userId"}""", true, true))
        .willReturn(
          aResponse()
            .withStatus(201)
            .withHeader("Location", s"/agents-external-stubs/users/$userId")
            .withHeader("Authorization", s"Bearer FOO-$userId")
            .withHeader("X-Session-ID", s"BAR-$userId")
        )
    )

  def givenClientEnrolmentExistsInStubs(enrolmentKey: String, userId: String): Unit =
    stubFor(
      get(urlEqualTo(s"/agents-external-stubs/known-facts/$enrolmentKey"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withBody(s"""{
                         |  "enrolmentKey":"$enrolmentKey",
                         |  "verifiers":[],
                         |  "user": {
                         |    "userId": "$userId",
                         |    "affinityGroup":"Individual",
                         |    "principalEnrolments":[]
                         |  },
                         |  "agents":[]
                         |}""".stripMargin)
        )
    )
}
