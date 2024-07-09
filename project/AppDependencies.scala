import play.sbt.PlayImport.ws
import sbt.*

object AppDependencies {

  private val bootstrapVersion: String = "8.6.0"
  private val playVersion: String = "play-30"

  lazy val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc"        %% s"bootstrap-backend-$playVersion" % bootstrapVersion,
    "uk.gov.hmrc"        %% "agent-mtd-identifiers"           % "2.0.0",
    "com.typesafe.play"  %% "play-json"                       % "2.10.5",
    "uk.gov.hmrc"        %% s"play-hmrc-api-$playVersion"     % "8.0.0",
    "com.github.blemale" %% "scaffeine"                       % "5.2.1"
  )

  def test: Seq[ModuleID] = Seq(
    "org.scalatestplus.play" %% "scalatestplus-play"           % "7.0.1"          % Test,
    "uk.gov.hmrc"            %% s"bootstrap-test-$playVersion" % bootstrapVersion % Test,
    "org.mockito"            %% "mockito-scala-scalatest"      % "1.17.37"        % Test
  )

}
