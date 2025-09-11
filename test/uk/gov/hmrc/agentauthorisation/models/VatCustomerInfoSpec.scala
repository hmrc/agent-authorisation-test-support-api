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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.Json

import java.time.LocalDate

class VatCustomerInfoSpec extends AnyWordSpecLike with Matchers {

  "VatCustomerInfo" should {

    "read from JSON" when {

      "there is a registration date present" in {
        val model = VatCustomerInfo(Some(LocalDate.parse("2020-01-01")))
        val json = Json.obj(
          "approvedInformation" -> Json.obj(
            "customerDetails" -> Json.obj(
              "effectiveRegistrationDate" -> "2020-01-01"
            )
          )
        )
        json.as[VatCustomerInfo] shouldBe model
      }

      "there is no registration date present" in {
        val model = VatCustomerInfo(None)
        val json = Json.obj(
          "approvedInformation" -> Json.obj(
            "customerDetails" -> Json.obj()
          )
        )
        json.as[VatCustomerInfo] shouldBe model
      }

      "there is no 'approvedInformation' field" in {
        val model = VatCustomerInfo(None)
        val json = Json.obj("unrecognisedField" -> "yep")
        json.as[VatCustomerInfo] shouldBe model
      }
    }
  }
}
