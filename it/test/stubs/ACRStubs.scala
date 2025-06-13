/*
 * Copyright 2025 HM Revenue & Customs
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

package stubs

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.libs.json.{JsValue, Json}
import support.WireMockSupport

trait ACRStubs {

  me: WireMockSupport with TestIdentifiers =>

  def invitationJson(
    invitationId: String,
    status: String,
    service: String,
    clientId: String,
    clientIdType: String,
    clientType: String
  ): JsValue =
    Json.obj(
      "invitationId" -> invitationId,
      "arn"          -> arn.value,
      "clientType"   -> clientType,
      "clientId"     -> clientId,
      "clientIdType" -> clientIdType,
      "service"      -> service,
      "status"       -> status
    )

  def givenItsaInvitationExists(status: String): StubMapping = givenInvitationExists(
    invitationIdITSA,
    invitationJson(invitationIdITSA, status, serviceITSA, mtdItId.value, identifierITSA, "personal")
  )

  def givenItsaSuppInvitationExists(status: String): StubMapping = givenInvitationExists(
    invitationIdITSA,
    invitationJson(invitationIdITSA, status, serviceITSASupp, mtdItId.value, identifierITSA, "personal")
  )

  def givenAltItsaInvitationExists(status: String): StubMapping = givenInvitationExists(
    invitationIdITSA,
    invitationJson(invitationIdITSA, status, serviceITSA, nino, identifierAltITSA, "personal")
  )

  def givenAltItsaSuppInvitationExists(status: String): StubMapping = givenInvitationExists(
    invitationIdITSA,
    invitationJson(invitationIdITSA, status, serviceITSASupp, nino, identifierAltITSA, "personal")
  )

  def givenVatInvitationExists(status: String): StubMapping = givenInvitationExists(
    invitationIdVAT,
    invitationJson(invitationIdVAT, status, serviceVAT, validVrn.value, identifierVAT, "business")
  )

  def givenUnsupportedInvitationExists: StubMapping = givenInvitationExists(
    invalidInvitationId,
    invitationJson(invalidInvitationId, "Pending", unsupportedService, "ABC", "ABC", "business")
  )

  private def givenInvitationExists(invitationId: String, invitationJson: JsValue): StubMapping =
    stubFor(
      get(urlEqualTo(s"/test-only/invitation/$invitationId"))
        .willReturn(
          aResponse()
            .withBody(invitationJson.toString())
            .withStatus(200)
        )
    )

  def givenInvitationNotFound(invitationId: String): Unit =
    stubFor(
      get(urlEqualTo(s"/test-only/invitation/$invitationId"))
        .willReturn(
          aResponse()
            .withStatus(404)
        )
    )

  def givenAcceptInvitation(invitationId: String, status: Int): StubMapping =
    stubFor(
      put(
        urlEqualTo(
          s"/agent-client-relationships/authorisation-response/accept/$invitationId"
        )
      )
        .willReturn(
          aResponse()
            .withStatus(status)
        )
    )

  def givenRejectInvitation(invitationId: String, status: Int): StubMapping =
    stubFor(
      put(
        urlEqualTo(
          s"/agent-client-relationships/client/authorisation-response/reject/$invitationId"
        )
      )
        .willReturn(
          aResponse()
            .withStatus(status)
        )
    )
}
