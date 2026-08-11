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
import uk.gov.hmrc.domain.Nino

class ClientIdTypeSpec extends AnyWordSpecLike with Matchers {

  "NinoType" should {

    "instantiate with the correct values" in {
      ClientIdType.NinoType.clazz shouldBe classOf[Nino]
      ClientIdType.NinoType.id shouldBe "ni"
      ClientIdType.NinoType.enrolmentId shouldBe "NINO"
    }

    "validate a Nino" in {
      ClientIdType.NinoType.isValid("AA112233A") shouldBe true
    }

    "invalidate a Nino" in {
      ClientIdType.NinoType.isValid("A") shouldBe false
      ClientIdType.NinoType.isValid("") shouldBe false
      ClientIdType.NinoType.isValid("123") shouldBe false
      ClientIdType.NinoType.isValid("AA!!22££A") shouldBe false
    }
  }

  "MtdItIdType" should {

    "instantiate with the correct values" in {
      ClientIdType.MtdItIdType.clazz shouldBe classOf[MtdItId]
      ClientIdType.MtdItIdType.id shouldBe "MTDITID"
      ClientIdType.MtdItIdType.enrolmentId shouldBe "MTDITID"
    }

    "validate an MTDITID" in {
      ClientIdType.MtdItIdType.isValid("ABCDEFG12345678") shouldBe true
    }

    "invalidate an MTDITID" in {
      ClientIdType.MtdItIdType.isValid("") shouldBe false
      ClientIdType.MtdItIdType.isValid("0000000000000000") shouldBe false
      ClientIdType.MtdItIdType.isValid("00000000000000!") shouldBe false
    }
  }
}
