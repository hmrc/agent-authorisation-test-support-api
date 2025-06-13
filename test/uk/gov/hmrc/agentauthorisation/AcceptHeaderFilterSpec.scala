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

package uk.gov.hmrc.agentauthorisation

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.mvc.Results._
import play.api.mvc.{AnyContentAsEmpty, Call, RequestHeader, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class AcceptHeaderFilterSpec extends AnyWordSpecLike with Matchers {

  lazy val system: ActorSystem = ActorSystem()
  implicit lazy val materializer: Materializer = Materializer(system)

  case class TestAcceptHeaderFilter(supportedVersion: Seq[String]) extends AcceptHeaderFilter(supportedVersion) {
    def response(f: RequestHeader => Future[Result])(rh: RequestHeader): Future[Result] = super.apply(f)(rh)
  }

  object TestAcceptHeaderFilter {

    val testHeaderVersion: String => Seq[(String, String)] =
      (testVersion: String) => Seq("Accept" -> s"application/vnd.hmrc.$testVersion+json")

    def fakeHeaders(headers: Seq[(String, String)]): FakeRequest[AnyContentAsEmpty.type] =
      FakeRequest().withHeaders(headers: _*)

    def fakeHeaders(call: Call, headers: Seq[(String, String)]): FakeRequest[AnyContentAsEmpty.type] =
      FakeRequest(call).withHeaders(headers: _*)

    def toResult(result: Result): RequestHeader => Future[Result] = (_: RequestHeader) => Future.successful(result)
  }

  import TestAcceptHeaderFilter._

  "AcceptHeaderFilter" should {
    "return None" when {
      "when no errors found in request" in {
        val supportedVersions: Seq[String] = Seq("1.0")
        val fakeTestHeader = fakeHeaders(testHeaderVersion("1.0"))
        TestAcceptHeaderFilter(supportedVersions).response(toResult(Ok("")))(fakeTestHeader).futureValue shouldBe Ok("")
      }

      "uri is /ping/ping with no headers" in {
        val call = Call("GET", "/ping/ping")
        val fakeTestHeader = fakeHeaders(call, testHeaderVersion("1.0"))
        TestAcceptHeaderFilter(Seq.empty).response(toResult(Ok("")))(fakeTestHeader).futureValue shouldBe Ok("")
      }
    }

    "return Some" when {
      "request had no Accept Header" in {
        val supportedVersions: Seq[String] = Seq("1.0")
        val fakeTestHeader = fakeHeaders(Seq.empty)
        val result = TestAcceptHeaderFilter(supportedVersions).response(toResult(Ok))(fakeTestHeader)
        contentAsString(result) shouldBe """{"code":"ACCEPT_HEADER_INVALID","message":"Missing 'Accept' header."}"""
      }

      "request had an invalid Accept Header" in {
        val supportedVersions: Seq[String] = Seq("1.0")
        val fakeTestHeader = fakeHeaders(Seq("Accept" -> s"InvalidHeader"))
        val result = TestAcceptHeaderFilter(supportedVersions).response(toResult(Ok))(fakeTestHeader)
        contentAsString(result) shouldBe """{"code":"ACCEPT_HEADER_INVALID","message":"Invalid 'Accept' header."}"""
      }

      "request used an unsupported version" in {
        val supportedVersions: Seq[String] = Seq("1.0")
        val fakeTestHeader = fakeHeaders(testHeaderVersion("0.0"))
        val result = TestAcceptHeaderFilter(supportedVersions).response(toResult(Ok))(fakeTestHeader)
        contentAsString(result) shouldBe """{"code":"BAD_REQUEST","message":"Missing or unsupported version number."}"""
      }

      "request used an unsupported content-type" in {
        val supportedVersions: Seq[String] = Seq("1.0")
        val fakeTestHeader = fakeHeaders(Seq("Accept" -> s"application/vnd.hmrc.1.0+xml"))
        val result = TestAcceptHeaderFilter(supportedVersions).response(toResult(Ok))(fakeTestHeader)
        contentAsString(result) shouldBe """{"code":"BAD_REQUEST","message":"Missing or unsupported content-type."}"""
      }
    }
  }
}
