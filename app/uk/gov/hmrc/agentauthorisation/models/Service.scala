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

import uk.gov.hmrc.domain.TaxIdentifier

sealed abstract class Service(
  val id: String,
  val invitationIdPrefix: Char,
  val enrolmentKey: String,
  val supportedSuppliedClientIdType: ClientIdType[_ <: TaxIdentifier],
  val supportedClientIdType: ClientIdType[_ <: TaxIdentifier]
)

object Service {
  case object MtdIt extends Service("HMRC-MTD-IT", 'A', "HMRC-MTD-IT", NinoType, MtdItIdType)
  case object MtdItSupp extends Service("HMRC-MTD-IT-SUPP", 'L', "HMRC-MTD-IT-SUPP", NinoType, MtdItIdType)
}
