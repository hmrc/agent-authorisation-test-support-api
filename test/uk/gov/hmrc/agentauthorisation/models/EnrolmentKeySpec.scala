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
import play.api.libs.json.{JsBoolean, JsResultException, JsString, Json, JsonValidationError}

class EnrolmentKeySpec extends AnyWordSpecLike with Matchers {

  "EnrolmentKey" should {

    val model = EnrolmentKey("HMRC-MTD-VAT", Seq(Identifier("VRN", "123456789")))
    val json = JsString("HMRC-MTD-VAT~VRN~123456789")

    "read from JSON" in {
      json.as[EnrolmentKey] shouldBe model
    }

    "write to JSON" in {
      Json.toJson(model) shouldBe json
    }

    "fail to read from JSON" when {

      "enrolment key is not in the correct format" in {
        val enrolmentJsonWithoutIdentifiers = JsString("HMRC-MTD-VAT")
        val ex = intercept[JsResultException](enrolmentJsonWithoutIdentifiers.as[EnrolmentKey])
        ex.errors.head._2 shouldBe Seq(JsonValidationError(List("INVALID_ENROLMENT_KEY")))
      }

      "the json value is not a string" in {
        val jsonBoolean = JsBoolean(true)
        val ex = intercept[JsResultException](jsonBoolean.as[EnrolmentKey])
        ex.errors.head._2 shouldBe Seq(JsonValidationError(List("STRING_VALUE_EXPECTED")))
      }
    }
  }
}
