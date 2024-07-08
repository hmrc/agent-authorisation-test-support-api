import play.sbt.PlayImport.ws
import sbt.*

object AppDependencies {

  private val bootstrapVersion: String = "8.6.0"

  lazy val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-backend-play-30" % bootstrapVersion,
    "uk.gov.hmrc"       %% "agent-mtd-identifiers"     % "2.0.0",
    "com.typesafe.play" %% "play-json"                 % "2.9.4",
    "uk.gov.hmrc"       %% "play-hmrc-api-play-30"     % "8.0.0",
    "com.github.blemale" %% "scaffeine"                % "5.2.1"
  )

  def test = Seq(
    "org.scalatestplus.play" %% "scalatestplus-play"      % "6.0.1"      % Test,
    "uk.gov.hmrc"            %% "bootstrap-test-play-30"  % bootstrapVersion % Test,
    "org.mockito"            %% "mockito-scala-scalatest" % "1.17.31"    % Test
  )

}
