package uk.gov.hmrc.agentauthorisation.models
import play.api.libs.json.Json.reads
import play.api.libs.json.Reads
import uk.gov.hmrc.agentmtdidentifiers.model.MtdItId

case class BusinessDetails(businessData: Array[BusinessData], mtdbsa: Option[MtdItId])

case class BusinessData(businessAddressDetails: BusinessAddressDetails)

case class BusinessAddressDetails(countryCode: String, postalCode: Option[String])

object BusinessDetails {
  implicit val businessAddressDetailsReads: Reads[BusinessAddressDetails] = reads[BusinessAddressDetails]
  implicit val businessDataReads: Reads[BusinessData] = reads[BusinessData]
  implicit val businessDetailsReads: Reads[BusinessDetails] = reads[BusinessDetails]
}
