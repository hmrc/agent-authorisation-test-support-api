package uk.gov.hmrc.agentauthorisation.models

import org.joda.time.LocalDate
import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.auth.core.Enrolment
import uk.gov.hmrc.domain.Nino

case class User(
  userId: Option[String] = None,
  affinityGroup: String,
  confidenceLevel: Option[Int] = None,
  credentialStrength: Option[String] = None,
  credentialRole: Option[String] = None,
  nino: Option[Nino] = None,
  principalEnrolments: Seq[Enrolment] = Seq.empty,
  dateOfBirth: Option[LocalDate] = None,
  address: Option[User.Address] = None
)

object User {

  case class Address(
    line1: Option[String] = None,
    line2: Option[String] = None,
    line3: Option[String] = None,
    line4: Option[String] = None,
    postcode: Option[String] = None,
    countryCode: Option[String] = None) {

    def isUKAddress: Boolean = countryCode.contains("GB")

  }

  object Address {
    implicit lazy val formats: Format[Address] = Json.format[Address]
  }

  implicit val formats: Format[User] = Json.format
}
