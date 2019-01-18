package uk.gov.hmrc.agentauthorisation.models
import org.joda.time.LocalDate
import play.api.libs.json.{JsObject, Reads, __}

case class VatCustomerInfo(effectiveRegistrationDate: Option[LocalDate])

object VatCustomerInfo {
  implicit val vatCustomerInfoReads: Reads[VatCustomerInfo] = {
    (__ \ "approvedInformation").readNullable[JsObject].map {
      case Some(approvedInformation) =>
        val maybeDate =
          (approvedInformation \ "customerDetails" \ "effectiveRegistrationDate").asOpt[String].map(LocalDate.parse)
        VatCustomerInfo(maybeDate)
      case None =>
        VatCustomerInfo(None)
    }
  }
}
