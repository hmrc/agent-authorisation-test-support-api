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

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, equalTo, get, post, stubFor, urlPathEqualTo}
import play.api.test.FakeRequest
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.agentauthorisation.support.BaseISpec
import uk.gov.hmrc.agentmtdidentifiers.model.Vrn
import uk.gov.hmrc.domain.Nino

class KnownFactControllerISpec extends BaseISpec {

  lazy val controller: KnownFactController =
    app.injector.instanceOf[KnownFactController]

  val vrn = "703850256"
  val nino = "AB104897B"

  val fakeRequest =
    FakeRequest().withHeaders("Accept" -> s"application/vnd.hmrc.1.0+json")

  "KnownFactController" should {
    "return MTD-VAT known fact" in {

      givenAuditConnector()
      givenSignedIdToStubs()
      givenUserCreationInStubsSucceeds()
      givenVatCustomerInformationExists(vrn)

      val result =
        await(controller.prepareMtdVatKnownFact(Vrn(vrn))(fakeRequest))
      status(result) shouldBe 200
      (jsonBodyOf(result) \ "knownFact").as[String] shouldBe "2017-11-09"
    }

    "return MTD-IT known fact" in {

      givenAuditConnector()
      givenSignedIdToStubs()
      givenUserCreationInStubsSucceeds()
      givenBusinessDetailsExists(nino)

      val result =
        await(controller.prepareMtdItKnownFact(Nino(nino))(fakeRequest))
      status(result) shouldBe 200

      (jsonBodyOf(result) \ "knownFact").as[String] shouldBe "WV34 8JW"

    }

    "return MTD-IT known fact when user already exists" in {

      givenAuditConnector()
      givenSignedIdToStubs()
      givenUserCreationInStubsReturnConflict()
      givenBusinessDetailsExists(nino)

      val result =
        await(controller.prepareMtdItKnownFact(Nino(nino))(fakeRequest))
      status(result) shouldBe 200

      (jsonBodyOf(result) \ "knownFact").as[String] shouldBe "WV34 8JW"

    }

    "return MTD-VAT known fact when user already exists" in {

      givenAuditConnector()
      givenSignedIdToStubs()
      givenUserCreationInStubsReturnConflict()
      givenVatCustomerInformationExists(vrn)

      val result =
        await(controller.prepareMtdVatKnownFact(Vrn(vrn))(fakeRequest))
      status(result) shouldBe 200

    }

    "return 500 when the known fact is missing for VAT service" in {

      givenAuditConnector()
      givenSignedIdToStubs()
      givenUserCreationInStubsSucceeds()
      givenVatCustomerInformationExistsNoKF(vrn)

      val result =
        await(controller.prepareMtdVatKnownFact(Vrn(vrn))(fakeRequest))
      status(result) shouldBe 500
    }

    "return 500 when the known fact is missing for ITSA service" in {

      givenAuditConnector()
      givenSignedIdToStubs()
      givenUserCreationInStubsSucceeds()
      givenBusinessDetailsExistsNoKF(nino)

      val result =
        await(controller.prepareMtdItKnownFact(Nino(nino))(fakeRequest))
      status(result) shouldBe 500
    }
  }

  def givenSignedIdToStubs() =
    stubFor(
      post(urlPathEqualTo("/agents-external-stubs/sign-in"))
        .willReturn(
          aResponse()
            .withStatus(201)
            .withHeader(HeaderNames.AUTHORIZATION, "Bearer 1234567890")
            .withHeader("X-Session-ID", "1234567890")))

  def givenUserCreationInStubsSucceeds() =
    stubFor(
      post(urlPathEqualTo("/agents-external-stubs/users"))
        .willReturn(
          aResponse()
            .withStatus(201)
            .withHeader(HeaderNames.LOCATION, "/agents-external-stubs/users/abc123")))

  def givenUserCreationInStubsReturnConflict() =
    stubFor(
      post(urlPathEqualTo("/agents-external-stubs/users"))
        .willReturn(aResponse().withStatus(409)))

  def givenVatCustomerInformationExists(vrn: String) =
    stubFor(
      get(urlPathEqualTo(s"/vat/customer/vrn/$vrn/information"))
        .withHeader("X-Session-ID", equalTo("1234567890"))
        .willReturn(aResponse().withStatus(200).withBody(s"""
                                                            |{
                                                            |  "vrn" : "$vrn",
                                                            |  "approvedInformation" : {
                                                            |    "customerDetails" : {
                                                            |      "tradingName" : "Conto-Dom Inc.",
                                                            |      "mandationStatus" : "4",
                                                            |      "registrationReason" : "0006",
                                                            |      "effectiveRegistrationDate" : "2017-11-09",
                                                            |      "businessStartDate" : "1990-09-28"
                                                            |    }
                                                            |  }
                                                            |}
           """.stripMargin)))

  def givenVatCustomerInformationExistsNoKF(vrn: String) =
    stubFor(
      get(urlPathEqualTo(s"/vat/customer/vrn/$vrn/information"))
        .withHeader("X-Session-ID", equalTo("1234567890"))
        .willReturn(aResponse().withStatus(200).withBody(s"""
                                                            |{
                                                            |  "vrn" : "$vrn",
                                                            |  "approvedInformation" : {
                                                            |    "customerDetails" : {
                                                            |      "tradingName" : "Conto-Dom Inc.",
                                                            |      "mandationStatus" : "4",
                                                            |      "registrationReason" : "0006",
                                                            |      "businessStartDate" : "1990-09-28"
                                                            |    }
                                                            |  }
                                                            |}
           """.stripMargin)))

  def givenBusinessDetailsExists(nino: String) =
    stubFor(
      get(urlPathEqualTo(s"/registration/business-details/nino/$nino"))
        .withHeader("X-Session-ID", equalTo("1234567890"))
        .willReturn(aResponse().withStatus(200).withBody(s"""
                                                            |{
                                                            |  "safeId" : "XW0007448578742",
                                                            |  "nino" : "AB104897B",
                                                            |  "mtdbsa" : "WVNQ79994005359",
                                                            |  "propertyIncome" : true,
                                                            |  "businessData" : [ {
                                                            |    "tradingName" : "Royal Bergentronic Group",
                                                            |    "businessAddressDetails" : {
                                                            |      "addressLine1" : "85 Thurlby Way",
                                                            |      "addressLine2" : "The Knight House",
                                                            |      "addressLine3" : "Aberdeen",
                                                            |      "addressLine4" : "AB45 0CJ",
                                                            |      "postalCode" : "WV34 8JW",
                                                            |      "countryCode" : "GB"
                                                            |    }
                                                            |  }]
                                                            |}
           """.stripMargin)))

  def givenBusinessDetailsExistsNoKF(nino: String) =
    stubFor(
      get(urlPathEqualTo(s"/registration/business-details/nino/$nino"))
        .withHeader("X-Session-ID", equalTo("1234567890"))
        .willReturn(aResponse().withStatus(200).withBody(s"""
                                                            |{
                                                            |  "safeId" : "XW0007448578742",
                                                            |  "nino" : "AB104897B",
                                                            |  "mtdbsa" : "WVNQ79994005359",
                                                            |  "propertyIncome" : true,
                                                            |  "businessData" : [ {
                                                            |    "tradingName" : "Royal Bergentronic Group",
                                                            |    "businessAddressDetails" : {
                                                            |      "addressLine1" : "85 Thurlby Way",
                                                            |      "addressLine2" : "The Knight House",
                                                            |      "addressLine3" : "Aberdeen",
                                                            |      "addressLine4" : "AB45 0CJ",
                                                            |      "countryCode" : "GB"
                                                            |    }
                                                            |  }]
                                                            |}
           """.stripMargin)))

}
