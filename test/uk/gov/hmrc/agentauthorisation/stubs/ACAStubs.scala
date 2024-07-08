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

package uk.gov.hmrc.agentauthorisation.stubs

import com.github.tomakehurst.wiremock.client.WireMock._
import java.time.LocalDate
import uk.gov.hmrc.agentauthorisation.UriPathEncoding.encodePathSegment
import uk.gov.hmrc.agentauthorisation.support.WireMockSupport
import uk.gov.hmrc.agentmtdidentifiers.model.{Arn, InvitationId, Vrn}
import uk.gov.hmrc.domain.Nino

trait ACAStubs {
  me: WireMockSupport with TestIdentifiers =>

  def createInvitationStub(
    arn: Arn,
    clientId: String,
    invitationId: InvitationId,
    suppliedClientId: String,
    suppliedClientType: String,
    clientType: String,
    service: String,
    serviceIdentifier: String,
    knownFact: String
  ): Unit =
    stubFor(
      post(urlEqualTo(s"/agent-client-authorisation/agencies/${encodePathSegment(arn.value)}/invitations/sent"))
        .withRequestBody(equalToJson(s"""
                                        |{
                                        |   "service": "$service",
                                        |   "clientType": "$clientType",
                                        |   "clientIdType": "$suppliedClientType",
                                        |   "clientId":"$suppliedClientId",
                                        |   "knownFact":"$knownFact"
                                        |}""".stripMargin))
        .willReturn(
          aResponse()
            .withStatus(201)
            .withHeader(
              "InvitationId",
              invitationId.value
            )
        )
    )

  def failedCreateInvitation(arn: Arn): Unit =
    stubFor(
      post(urlEqualTo(s"/agent-client-authorisation/agencies/${encodePathSegment(arn.value)}/invitations/sent"))
        .willReturn(
          aResponse()
            .withStatus(400)
        )
    )

  def createAgentLink(clientType: String, normalisedAgentName: String): Unit =
    stubFor(
      post(urlEqualTo(s"/agent-client-authorisation/agencies/references/arn/${arn.value}/clientType/$clientType"))
        .willReturn(
          aResponse()
            .withStatus(201)
            .withHeader("location", s"/invitations/$clientType/12345678/$normalisedAgentName")
        )
    )

  def givenMatchingClientIdAndPostcode(nino: Nino, postcode: String) =
    stubFor(
      get(urlEqualTo(s"/agent-client-authorisation/known-facts/individuals/nino/${nino.value}/sa/postcode/$postcode"))
        .willReturn(
          aResponse()
            .withStatus(204)
        )
    )

  def givenNonMatchingClientIdAndPostcode(nino: Nino, postcode: String) =
    stubFor(
      get(urlEqualTo(s"/agent-client-authorisation/known-facts/individuals/nino/${nino.value}/sa/postcode/$postcode"))
        .willReturn(
          aResponse()
            .withStatus(403)
            .withBody(s"""
                         |{
                         |   "code":"POSTCODE_DOES_NOT_MATCH",
                         |   "message":"The submitted postcode did not match the client's postcode as held by HMRC."
                         |}
           """.stripMargin)
        )
    )

  def givenNotEnrolledClientITSA(nino: Nino, postcode: String) =
    stubFor(
      get(urlEqualTo(s"/agent-client-authorisation/known-facts/individuals/nino/${nino.value}/sa/postcode/$postcode"))
        .willReturn(
          aResponse()
            .withStatus(403)
            .withBody(s"""
                         |{
                         |   "code":"CLIENT_REGISTRATION_NOT_FOUND",
                         |   "message":"The Client's MTDfB registration was not found."
                         |}
           """.stripMargin)
        )
    )

  def checkClientIdAndVatRegDate(vrn: Vrn, date: LocalDate, responseStatus: Int) =
    stubFor(
      get(
        urlEqualTo(
          s"/agent-client-authorisation/known-facts/organisations/vat/${vrn.value}/registration-date/${date.toString}"
        )
      )
        .willReturn(
          aResponse()
            .withStatus(responseStatus)
        )
    )

  def verifyCheckVatRegisteredClientStubAttempt(vrn: Vrn, date: LocalDate): Unit = {
    val vrnEncoded = encodePathSegment(vrn.value)
    val dateEncoded = encodePathSegment(date.toString)
    verify(
      1,
      getRequestedFor(
        urlEqualTo(
          s"/agent-client-authorisation/known-facts/organisations/vat/$vrnEncoded/registration-date/$dateEncoded"
        )
      )
    )
  }

  def verifyCheckItsaRegisteredClientStubAttempt(nino: Nino, postcode: String): Unit = {
    val ninoEncoded = encodePathSegment(nino.value)
    val postEncoded = encodePathSegment(postcode)
    verify(
      1,
      getRequestedFor(
        urlEqualTo(s"/agent-client-authorisation/known-facts/individuals/nino/$ninoEncoded/sa/postcode/$postEncoded")
      )
    )
  }

  def verifyNoCheckVatRegisteredClientStubAttempt(): Unit =
    verify(
      0,
      getRequestedFor(urlPathMatching("/agent-client-authorisation/known-facts/organisations/.*/registration-date/.*"))
    )

  def givenGetITSAInvitationStub(arn: Arn, status: String): Unit =
    givenGetAgentInvitationStub(arn, "personal", "MTDITID", mtdItId.value, invitationIdITSA, serviceITSA, status)

  def givenGetVATInvitationStub(arn: Arn, status: String): Unit =
    givenGetAgentInvitationStub(arn, "business", "vrn", validVrn.value, invitationIdVAT, serviceVAT, status)

  def givenGetAgentInvitationStub(
    arn: Arn,
    clientType: String,
    clientIdType: String,
    clientId: String,
    invitationId: String,
    service: String,
    status: String
  ): Unit =
    stubFor(
      get(urlEqualTo(s"/agent-client-authorisation/invitations/$invitationId"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withBody(
              s"""
                 |{
                 |  "invitationId": "$invitationId",
                 |  "arn" : "${arn.value}",
                 |  "service" : "$service",
                 |  "clientType":"$clientType",
                 |  "clientId" : "$clientId",
                 |  "clientIdType" : "$clientIdType",
                 |  "suppliedClientId" : "$clientId",
                 |  "suppliedClientIdType" : "$clientIdType",
                 |  "status" : "$status",
                 |  "created" : "2017-10-31T23:22:50.971Z",
                 |  "lastUpdated" : "2018-09-11T21:02:00.000Z",
                 |  "expiryDate" : "2017-12-18",
                 |  "_links": {
                 |    	"self" : {
                 |			  "href" : "$wireMockBaseUrlAsString/agent-client-authorisation/agencies/${arn.value}/invitations/sent/$invitationId"
                 |		  }
                 |  }
                 |}""".stripMargin
            )
        )
    )

  def givenGetAgentInvitationStubReturns(arn: Arn, invitationId: InvitationId, status: Int) =
    stubFor(
      get(urlEqualTo(s"/agent-client-authorisation/invitations/${invitationId.value}"))
        .willReturn(
          aResponse()
            .withStatus(status)
        )
    )

  val invitation = (
    arn: Arn,
    status: String,
    service: String,
    clientType: String,
    clientIdType: String,
    clientId: String,
    invitationId: String,
    expiryDate: String
  ) =>
    s"""
       |{
       |  "invitationId": "$invitationId",
       |  "arn" : "${arn.value}",
       |  "service" : "$service",
       |  "clientType": "$clientType",
       |  "clientId" : "$clientId",
       |  "clientIdType" : "$clientIdType",
       |  "suppliedClientId" : "$clientId",
       |  "suppliedClientIdType" : "$clientIdType",
       |  "status" : "$status",
       |  "created" : "2017-10-31T23:22:50.971Z",
       |  "lastUpdated" : "2018-09-11T21:02:00.000Z",
       |  "expiryDate" : "$expiryDate",
       |  "invitationId": "$invitationId",
       |  "_links": {
       |    	"self" : {
       |			  "href" : "$wireMockBaseUrlAsString/agent-client-authorisation/agencies/${arn.value}/invitations/sent/$invitationId"
       |		  }
       |  }
       |}""".stripMargin

  def givenInvitationNotFound(arn: Arn, invitationId: String): Unit =
    stubFor(
      get(urlEqualTo(s"/agent-client-authorisation/invitations/$invitationId"))
        .willReturn(
          aResponse()
            .withStatus(404)
        )
    )

  def givenRejectInvitationStub(
    invitationId: String,
    clientIdentifier: String,
    clientIdentifierType: String,
    status: Int
  ) =
    stubFor(
      put(
        urlEqualTo(
          s"/agent-client-authorisation/clients/$clientIdentifierType/$clientIdentifier/invitations/received/$invitationId/reject"
        )
      )
        .willReturn(
          aResponse()
            .withStatus(status)
        )
    )

  def givenRejectInvitationStubInvalid(invitationId: String, clientIdentifier: String, clientIdentifierType: String) =
    stubFor(
      put(
        urlEqualTo(
          s"/agent-client-authorisation/clients/$clientIdentifierType/$clientIdentifier/invitations/received/$invitationId/reject"
        )
      )
        .willReturn(
          aResponse()
            .withStatus(403)
            .withBody(s"""
                         |{
                         |   "code":"INVALID_INVITATION_STATUS",
                         |   "message":"The invitation has an invalid status to be cancelled"
                         |}
           """.stripMargin)
        )
    )

  def givenAcceptInvitationStub(
    invitationId: String,
    clientIdentifier: String,
    clientIdentifierType: String,
    status: Int
  ) =
    stubFor(
      put(
        urlEqualTo(
          s"/agent-client-authorisation/clients/$clientIdentifierType/$clientIdentifier/invitations/received/$invitationId/accept"
        )
      )
        .willReturn(
          aResponse()
            .withStatus(status)
        )
    )

  def givenAcceptInvitationStubInvalid(invitationId: String, clientIdentifier: String, clientIdentifierType: String) =
    stubFor(
      put(
        urlEqualTo(
          s"/agent-client-authorisation/clients/$clientIdentifierType/$clientIdentifier/invitations/received/$invitationId/accept"
        )
      )
        .willReturn(
          aResponse()
            .withStatus(403)
            .withBody(s"""
                         |{
                         |   "code":"INVALID_INVITATION_STATUS",
                         |   "message":"The invitation has an invalid status to be cancelled"
                         |}
           """.stripMargin)
        )
    )
}
