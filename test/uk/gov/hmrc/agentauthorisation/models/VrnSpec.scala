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
import play.api.libs.json.{JsString, Json}

class VrnSpec extends AnyWordSpecLike with Matchers {

  "Vrn" should {

    val model = Vrn("123456789")
    val json = JsString("123456789")

    "read from JSON" in {
      json.as[Vrn] shouldBe model
    }

    "write to JSON" in {
      Json.toJson(model) shouldBe json
    }
  }

  ".isValid" should {

    "return true for a valid VRN (9 digits)" in {
      Vrn.isValid("123456789") shouldBe true
    }

    "return false when the VRN has more than 9 digits" in {
      Vrn.isValid("1234567890") shouldBe false
    }

    "return false when the VRN is empty" in {
      Vrn.isValid("") shouldBe false
    }

    "return false when the VRN has non-numeric characters" in {
      Vrn.isValid("!23456879") shouldBe false
      Vrn.isValid("12E456789") shouldBe false
      Vrn.isValid("1234567😂9") shouldBe false
    }
  }
}
