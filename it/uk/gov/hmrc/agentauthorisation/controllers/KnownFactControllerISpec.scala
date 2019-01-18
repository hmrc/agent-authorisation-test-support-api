package uk.gov.hmrc.agentauthorisation.controllers

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, post, stubFor, urlPathEqualTo}
import play.api.test.FakeRequest
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.agentauthorisation.support.BaseISpec
import uk.gov.hmrc.agentmtdidentifiers.model.Vrn
import uk.gov.hmrc.domain.Nino

class KnownFactControllerISpec extends BaseISpec {

  lazy val controller: KnownFactController = app.injector.instanceOf[KnownFactController]

  val vrn = "703850256"
  val nino = "AB104897B"

  "KnownFactController" should {
    "return MTD-VAT known fact" in {

      givenAuditConnector()
      givenSignedIdToStubs()
      givenUserCreationInStubsSucceeds()
      givenVatCustomerInformationExists(vrn)

      val result = await(controller.prepareMtdVatKnownFact(Vrn(vrn))(FakeRequest()))
      status(result) shouldBe 200
      (jsonBodyOf(result) \ "knownFact").as[String] shouldBe "2017-11-09"
    }

    "return MTD-IT known fact" in {

      givenAuditConnector()
      givenSignedIdToStubs()
      givenUserCreationInStubsSucceeds()
      givenBusinessDetailsExists(nino)

      val result = await(controller.prepareMtdItKnownFact(Nino(nino))(FakeRequest()))
      status(result) shouldBe 200

      (jsonBodyOf(result) \ "knownFact").as[String] shouldBe "WV34 8JW"

    }
  }

  def givenSignedIdToStubs() =
    stubFor(
      post(urlPathEqualTo("/agents-external-stubs/sign-in"))
        .willReturn(aResponse().withStatus(201).withHeader(HeaderNames.AUTHORIZATION, "Bearer 1234567890")))

  def givenUserCreationInStubsSucceeds() =
    stubFor(post(urlPathEqualTo("/agents-external-stubs/users"))
      .willReturn(aResponse().withStatus(201).withHeader(HeaderNames.LOCATION, "/agents-external-stubs/users/abc123")))

  def givenVatCustomerInformationExists(vrn: String) =
    stubFor(
      get(urlPathEqualTo(s"/vat/customer/vrn/$vrn/information"))
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

  def givenBusinessDetailsExists(nino: String) =
    stubFor(
      get(urlPathEqualTo(s"/registration/business-details/nino/$nino"))
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

}
