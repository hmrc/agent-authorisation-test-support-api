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

package uk.gov.hmrc.agentauthorisation.models

import org.joda.time.LocalDate
import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.domain.Nino

case class User(
  userId: Option[String] = None,
  affinityGroup: String,
  confidenceLevel: Option[Int] = None,
  credentialStrength: Option[String] = None,
  credentialRole: Option[String] = None,
  nino: Option[Nino] = None,
  principalEnrolments: Seq[User.Enrolment] = Seq.empty,
  dateOfBirth: Option[LocalDate] = None,
  address: Option[User.Address] = None
)

object User {

  case class Identifier(key: String, value: String)
  case class Enrolment(key: String, identifiers: Option[Seq[Identifier]] = None)

  case class Address(
    line1: Option[String] = None,
    line2: Option[String] = None,
    line3: Option[String] = None,
    line4: Option[String] = None,
    postcode: Option[String] = None,
    countryCode: Option[String] = None)

  implicit val format1: Format[Identifier] = Json.format[Identifier]
  implicit val format2: Format[Enrolment] = Json.format[Enrolment]
  implicit val format3: Format[Address] = Json.format[Address]
  implicit val formats: Format[User] = Json.format
}
