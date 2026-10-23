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

package uk.gov.hmrc.agentauthorisation.models

enum Service(
  val id: String,
  val enrolmentService: String,
  val enrolmentIdentifier: String,
  val requiresEnrolmentExpansion: Boolean = false
):
  case MtdIt extends Service("HMRC-MTD-IT", "HMRC-MTD-IT", "MTDITID")
  case MtdItSupp extends Service("HMRC-MTD-IT-SUPP", "HMRC-MTD-IT", "MTDITID")
  case Vat extends Service("HMRC-MTD-VAT", "HMRC-MTD-VAT", "VRN")
  case PersonalIncomeRecord extends Service("PERSONAL-INCOME-RECORD", "HMRC-NI", "NINO")
  case Trust extends Service("HMRC-TERS-ORG", "HMRC-TERS-ORG", "SAUTR")
  case TrustNonTaxable extends Service("HMRC-TERSNT-ORG", "HMRC-TERSNT-ORG", "URN")
  case CapitalGains extends Service("HMRC-CGT-PD", "HMRC-CGT-PD", "CGTPDRef")
  case PlasticPackagingTax extends Service("HMRC-PPT-ORG", "HMRC-PPT-ORG", "EtmpRegistrationNumber")
  case CountryByCountry extends Service("HMRC-CBC-ORG", "HMRC-CBC-ORG", "cbcId", requiresEnrolmentExpansion = true)
  case CountryByCountryNonUk extends Service("HMRC-CBC-NONUK-ORG", "HMRC-CBC-NONUK-ORG", "cbcId")
  case Pillar2 extends Service("HMRC-PILLAR2-ORG", "HMRC-PILLAR2-ORG", "PLRID")

  def enrolmentKey(clientId: String): EnrolmentKey =
    EnrolmentKey(enrolmentService, Seq(Identifier(enrolmentIdentifier, clientId)))

object Service:
  def findById(id: String): Option[Service] = Service.values.find(_.id == id)
