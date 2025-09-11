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

class MtdItIdSpec extends AnyWordSpecLike with Matchers {

  "MtdItId" should {

    val model = MtdItId("ABCDEFG12345678")
    val json = JsString("ABCDEFG12345678")

    "read from JSON" in {
      json.as[MtdItId] shouldBe model
    }

    "write to JSON" in {
      Json.toJson(model) shouldBe json
    }
  }

  ".isValid" should {

    "return true for a valid MTDITID (between 1 and 15 alphanumeric chars)" in {
      MtdItId.isValid("A") shouldBe true
      MtdItId.isValid("ABCDEFG") shouldBe true
      MtdItId.isValid("ABCDEFG12345678") shouldBe true
    }

    "return false when the ID has more than 15 chars" in {
      MtdItId.isValid("0000000000000000") shouldBe false
    }

    "return false when the ID is empty" in {
      MtdItId.isValid("") shouldBe false
    }

    "return false when the ID has non-alphanumeric characters" in {
      MtdItId.isValid("00000000000000!") shouldBe false
    }
  }
}
