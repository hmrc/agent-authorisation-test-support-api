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

class IdentifierSpec extends AnyWordSpecLike with Matchers {

  "Identifier" should {

    val model = Identifier("VRN", "123456789")
    val json = Json.obj("key" -> "VRN", "value" -> "123456789")

    "read from JSON" in {
      json.as[Identifier] shouldBe model
    }

    "write to JSON" in {
      Json.toJson(model) shouldBe json
    }

    "convert to a String in the expected format" in {
      model.toString shouldBe "VRN~123456789"
    }

    "have a custom ordering based on the `key` field" in {
      val identifierA = Identifier("A", "123")
      val identifierM = Identifier("M", "123")
      val identifierZ = Identifier("Z", "123")
      val identifiers = Seq(identifierM, identifierZ, identifierA)

      identifiers.sorted shouldBe Seq(identifierA, identifierM, identifierZ)
    }
  }
}
