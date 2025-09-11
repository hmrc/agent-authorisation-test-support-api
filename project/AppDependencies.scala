import play.sbt.PlayImport.ws
import sbt.*

object AppDependencies {

  private val bootstrapVersion: String = "10.1.0"
  private val playVersion: String = "play-30"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc"        %% s"bootstrap-backend-$playVersion" % bootstrapVersion,
    "com.typesafe.play"  %% "play-json"                       % "2.10.7",
    "uk.gov.hmrc"        %% s"play-hmrc-api-$playVersion"     % "8.0.0",
    "uk.gov.hmrc"        %% s"domain-$playVersion"            % "11.0.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"        %% s"bootstrap-test-$playVersion"    % bootstrapVersion % Test
  )
}
