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

package uk.gov.hmrc.agentauthorisation.binders

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import uk.gov.hmrc.agentauthorisation.binders.UrlBinders.{NinoBinder, VrnBinder}
import uk.gov.hmrc.agentauthorisation.models.Vrn
import uk.gov.hmrc.domain.Nino

class UrlBindersSpec extends AnyWordSpecLike with Matchers {

  "NinoBinder" should {

    "bind a key-value pair to a Nino" in {
      NinoBinder.bind("nino", "AA001122A") shouldBe Right(Nino("AA001122A"))
    }

    "unbind a Nino to a String" in {
      NinoBinder.unbind("nino", Nino("AA001122A")) shouldBe "AA001122A"
    }

    "fail to bind a key-value pair where the value does not match the Nino format" in {
      NinoBinder.bind("nino", "A") shouldBe Left("Cannot parse parameter 'nino' with value 'A' as 'Nino'")
    }
  }

  "VrnBinder" should {

    "bind a key-value pair to a Vrn" in {
      VrnBinder.bind("vrn", "123456789") shouldBe Right(Vrn("123456789"))
    }

    "unbind a Vrn to a String" in {
      VrnBinder.unbind("vrn", Vrn("123456789")) shouldBe "123456789"
    }

    "fail to bind a key-value pair where the value does not match the Vrn format" in {
      VrnBinder.bind("vrn", "A") shouldBe Left("Cannot parse parameter 'vrn' with value 'A' as 'Vrn'")
    }
  }
}
