import uk.gov.hmrc.DefaultBuildSettings

val appName = "agent-authorisation-test-support-api"

ThisBuild / majorVersion := 1
ThisBuild / scalaVersion := "3.7.4"

val scalaCOptions = Seq(
  "-Werror",
  "-feature",
  "-Wconf:src=target/.*:s", // silence warnings from compiled files
  "-Wconf:src=.*routes.*:s", // silence warnings from routes files
  "-Wconf:src=.*html.*:w" // silence html warnings as they are wrong
)

ThisBuild / scalacOptions ~= (_.filterNot(Set("-deprecation", "-unchecked", "-encoding", "UTF-8", "utf8")))

lazy val root = (project in file("."))
  .settings(
    name := appName,
    organization := "uk.gov.hmrc",
    PlayKeys.playDefaultPort := 9443,
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    routesImport ++= Seq("uk.gov.hmrc.agentauthorisation.binders.UrlBinders.given"),
    scalacOptions ++= scalaCOptions,
    Compile / scalafmtOnCompile := true,
    Test / scalafmtOnCompile := true,
    Compile / unmanagedResourceDirectories += baseDirectory.value / "resources"
  )
  .settings(
    Test / parallelExecution := false,
    CodeCoverageSettings.scoverageSettings,
    Compile / scalacOptions := (Compile / scalacOptions).value.distinct,
    Test / scalacOptions := (Test / scalacOptions).value.distinct
  )
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(root % "test->test") // the "test->test" allows reusing test code and test dependencies
  .settings(DefaultBuildSettings.itSettings())
  .settings(libraryDependencies ++= AppDependencies.test)
  .settings(
    Compile / scalafmtOnCompile := true,
    Test / scalafmtOnCompile := true,
    Compile / scalacOptions := (Compile / scalacOptions).value.distinct,
    Test / scalacOptions := (Test / scalacOptions).value.distinct
  )
