import play.sbt.PlayImport.ws
import sbt.*

object AppDependencies {

  private val bootstrapVersion: String = "10.1.0"
  private val playVersion: String = "play-30"

  lazy val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc"        %% s"bootstrap-backend-$playVersion" % bootstrapVersion,
    "uk.gov.hmrc"        %% "agent-mtd-identifiers"           % "2.2.0",
    "com.typesafe.play"  %% "play-json"                       % "2.10.6",
    "uk.gov.hmrc"        %% s"play-hmrc-api-$playVersion"     % "8.0.0"
  )

  def test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"        %% s"bootstrap-test-$playVersion"    % bootstrapVersion % Test
  )
}
