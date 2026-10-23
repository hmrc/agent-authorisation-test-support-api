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

package uk.gov.hmrc.agentauthorisation.connectors

import play.api.http.HeaderNames
import play.api.http.Status.NO_CONTENT
import play.api.libs.json.Json
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import play.utils.UriEncoding
import sttp.model.Uri
import sttp.model.Uri.UriContext
import uk.gov.hmrc.agentauthorisation.connectors.AgentsExternalStubsConnector.EnrolmentsFromKnownFactsResponse
import uk.gov.hmrc.agentauthorisation.models.{EnrolmentKey, Identifier}
import uk.gov.hmrc.agentauthorisation.util.HttpAPIMonitor
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, HttpResponse, SessionId}
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.play.bootstrap.metrics.Metrics

import java.net.URL
import java.nio.charset.StandardCharsets
import javax.inject.{Inject, Named, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AgentsExternalStubsConnector @Inject() (
  @Named("agents-external-stubs-baseUrl") baseUrl: URL,
  http: HttpClientV2,
  val metrics: Metrics
)(using val ec: ExecutionContext)
    extends HttpAPIMonitor {

  private val AgentsExternalStubsUri = uri"$baseUrl/agents-external-stubs/"

  def signInAndGetSessionHeaders(userId: String)(using sessionHeaders: HeaderCarrier): Future[HeaderCarrier] =
    http
      .post(uri"$AgentsExternalStubsUri/sign-in".toJavaUri.toURL)
      .withBody(Json.obj("planetId" -> "hmrc", "userId" -> userId))
      .execute[HttpResponse]
      .map { response =>
        val authorizationHeader = response.header(HeaderNames.AUTHORIZATION).map(Authorization(_))
        val sessionIdHeader = response.header("X-Session-ID").map(SessionId(_))
        HeaderCarrier(authorization = authorizationHeader, sessionId = sessionIdHeader)
      }

  def getUserIdForNino(nino: String)(using sessionHeaders: HeaderCarrier): Future[String] = {
    val encodedNino = encodePathSegment(nino)
    val uri = uri"$AgentsExternalStubsUri/users/nino/$encodedNino"

    http
      .get(uri.toJavaUri.toURL)
      .execute[HttpResponse]
      .map { response =>
        (response.json \ "userId").as[String]
      }
  }

  def getUserIdForEnrolment(enrolmentKey: String)(using sessionHeaders: HeaderCarrier): Future[String] = {
    val encodedKnownFact = encodePathSegment(enrolmentKey)
    val uri = uri"$AgentsExternalStubsUri/known-facts/$encodedKnownFact"

    http
      .get(uri.toJavaUri.toURL)
      .execute[HttpResponse]
      .map { response =>
        (response.json \ "user" \ "userId").as[String]
      }
  }

  def findEnrolmentKey(service: String, identifier: Identifier)(using
    sessionHeaders: HeaderCarrier
  ): Future[Option[EnrolmentKey]] =
    http
      .post(uri"$baseUrl/enrolment-store-proxy/enrolment-store/enrolments".toJavaUri.toURL)
      .withBody(Json.obj("service" -> service, "knownFacts" -> Json.arr(Json.toJson(identifier))))
      .execute[HttpResponse]
      .map { response =>
        if response.status == NO_CONTENT then None
        else
          response.json
            .as[EnrolmentsFromKnownFactsResponse]
            .enrolments
            .headOption
            .map(enrolment => EnrolmentKey(service, enrolment.identifiers))
      }

  private def encodePathSegment(pathSegment: String): String =
    UriEncoding.encodePathSegment(pathSegment, StandardCharsets.UTF_8.name)

}

object AgentsExternalStubsConnector:
  private case class IdentifiersAndVerifiers(identifiers: Seq[Identifier])
  private object IdentifiersAndVerifiers:
    given play.api.libs.json.Reads[IdentifiersAndVerifiers] =
      (play.api.libs.json.__ \ "identifiers").read[Seq[Identifier]].map(IdentifiersAndVerifiers.apply)

  private case class EnrolmentsFromKnownFactsResponse(enrolments: Seq[IdentifiersAndVerifiers])
  private object EnrolmentsFromKnownFactsResponse:
    given play.api.libs.json.Reads[EnrolmentsFromKnownFactsResponse] = Json.reads[EnrolmentsFromKnownFactsResponse]
