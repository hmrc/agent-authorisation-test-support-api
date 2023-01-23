/*
 * Copyright 2023 HM Revenue & Customs
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

import play.api.libs.json._

case class EnrolmentKey(service: String, identifiers: Seq[Identifier]) {
  lazy val tag = s"$service~${identifiers.sorted.mkString("~")}"
  override def toString: String = tag
}

object EnrolmentKey {
  implicit val writes: Writes[EnrolmentKey] = new Writes[EnrolmentKey] {
    override def writes(ek: EnrolmentKey): JsValue = JsString(ek.toString)
  }

  implicit val reads: Reads[EnrolmentKey] = new Reads[EnrolmentKey] {
    override def reads(json: JsValue): JsResult[EnrolmentKey] = json match {
      case JsString(value) => parse(value).fold(JsError.apply, JsSuccess.apply(_))
      case _               => JsError("STRING_VALUE_EXPECTED")
    }
  }

  private def parse(s: String): Either[String, EnrolmentKey] = {
    val parts = s.split("~")
    if (parts.nonEmpty && parts.size >= 3 && parts.size % 2 == 1) {
      val service = parts.head
      val identifiers = parts.tail.sliding(2, 2).map(a => Identifier(a(0), a(1))).toSeq
      Right(EnrolmentKey(service, identifiers))
    } else Left("INVALID_ENROLMENT_KEY")
  }

}
