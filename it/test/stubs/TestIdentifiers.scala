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

package stubs

import uk.gov.hmrc.agentauthorisation.models.Arn

trait TestIdentifiers {

  val arn = Arn("TARN0000001")
  val arn2 = Arn("DARN0002185")
  val nino = "AB123456A"

  val mtdItId = "ABCDEF123456789"
  val serviceITSA = "HMRC-MTD-IT"
  val serviceITSASupp = "HMRC-MTD-IT-SUPP"
  val invitationIdITSA = "ABERULMHCKKW3"
  val identifierITSA = "MTDITID"
  val identifierAltITSA = "ni"
  val userIdITSA = "ITSAClient001"

  val invitationIdVAT = "CZTW1KY6RTAAT"
  val serviceVAT = "HMRC-MTD-VAT"
  val identifierVAT = "VRN"
  val validVrn = "101747696"

  val invalidInvitationId = "ZTSF4OW9CCRPT"
  val unsupportedService = "HMRC-ABC-DEF"

  val pillar2Id = "XMPLR0001234567"
  val cbcId = "XACBC0001234567"
  val cbcUtr = "1234567890"
  val cgtReference = "XMCGTP123456789"
  val trustUtr = "1234567890"
  val trustUrn = "AATRUST00000000"
  val pptReference = "XAPPT0001234567"
  val irvNino = "AB123456A"
}
