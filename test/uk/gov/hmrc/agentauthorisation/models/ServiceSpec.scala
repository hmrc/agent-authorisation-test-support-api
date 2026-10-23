/*
 * Copyright 2026 HM Revenue & Customs
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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class ServiceSpec extends AnyWordSpecLike with Matchers:

  "Service" should:
    val expectedMappings = Seq(
      ("HMRC-MTD-IT", "HMRC-MTD-IT~MTDITID~ABCDEF123456789", false),
      ("HMRC-MTD-IT-SUPP", "HMRC-MTD-IT~MTDITID~ABCDEF123456789", false),
      ("HMRC-MTD-VAT", "HMRC-MTD-VAT~VRN~123456789", false),
      ("PERSONAL-INCOME-RECORD", "HMRC-NI~NINO~AB123456A", false),
      ("HMRC-TERS-ORG", "HMRC-TERS-ORG~SAUTR~1234567890", false),
      ("HMRC-TERSNT-ORG", "HMRC-TERSNT-ORG~URN~AATRUST00000000", false),
      ("HMRC-CGT-PD", "HMRC-CGT-PD~CGTPDRef~XMCGTP123456789", false),
      ("HMRC-PPT-ORG", "HMRC-PPT-ORG~EtmpRegistrationNumber~XAPPT0001234567", false),
      ("HMRC-CBC-ORG", "HMRC-CBC-ORG~cbcId~XACBC0001234567", true),
      ("HMRC-CBC-NONUK-ORG", "HMRC-CBC-NONUK-ORG~cbcId~XACBC0001234567", false),
      ("HMRC-PILLAR2-ORG", "HMRC-PILLAR2-ORG~PLRID~XMPLR0001234567", false)
    )

    expectedMappings.foreach { case (serviceId, expectedEnrolmentKey, requiresExpansion) =>
      s"map $serviceId to its client enrolment" in:
        val service = Service.findById(serviceId).get

        service.enrolmentKey(expectedEnrolmentKey.split("~").last).toString.shouldBe(expectedEnrolmentKey)
        service.requiresEnrolmentExpansion.shouldBe(requiresExpansion)
    }

    "not match an unsupported service" in:
      Service.findById("HMRC-UNSUPPORTED").shouldBe(None)
