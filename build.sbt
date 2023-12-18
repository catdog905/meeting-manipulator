ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.12"

Compile / compile / scalacOptions ++= Seq(
  // "-Werror",
  // "-Xfatal-warnings",
  "-Dhttp.proxyHost=127.0.0.1",
  "-Dhttp.proxyPort=2080",
  "-Dhttps.proxyHost=127.0.0.1",
  "-Dhttps.proxyPort=2080"
  // "-Wdead-code",
  // "-Wextra-implicit",
  // "-Wnumeric-widen",
  // "-Wunused",
  // "-Wvalue-discard",
  // "-Xlint",
  // "-Xlint:-byname-implicit",
  // "-Xlint:-implicit-recursion",
  // "-unchecked"
)

val CatsVersion = "3.3.4"
val Http4sVersion = "0.23.7"
val CirceVersion = "0.14.1"
val SttpVersion = "3.4.0"
val Log4CatsVersion = "2.2.0"
val DoobieVersion = "1.0.0-RC1"
val CirisVersion = "2.3.1"

lazy val root = (project in file("."))
  .settings(
    name := "MeetingReminder",
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir" %% "tapir-core" % "1.4.0",
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % "1.4.0",
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % "1.4.0",
      "org.typelevel" %% "cats-effect" % "3.5.0",
      "org.tpolecat" %% "doobie-core" % "1.0.0-RC2",
      "org.tpolecat" %% "doobie-hikari" % "1.0.0-RC2", // HikariCP transactor.
      "org.tpolecat" %% "doobie-postgres" % "1.0.0-RC2", // Postgres driver 42.3.1 + type mappings.
      "tf.tofu" %% "tofu-logging" % "0.12.0.1",
      "tf.tofu" %% "tofu-logging-derivation" % "0.12.0.1",
      "tf.tofu" %% "tofu-logging-layout" % "0.12.0.1",
      "tf.tofu" %% "tofu-logging-logstash-logback" % "0.12.0.1",
      "tf.tofu" %% "tofu-logging-structured" % "0.12.0.1",
      "tf.tofu" %% "tofu-core-ce3" % "0.12.0.1",
      "tf.tofu" %% "tofu-doobie-logging-ce3" % "0.12.0.1",
      "com.softwaremill.sttp.client3" %% "core" % "3.8.15",
      "tf.tofu" %% "derevo-circe" % "0.13.0",
      "io.estatico" %% "newtype" % "0.4.4",
      "com.github.pureconfig" %% "pureconfig" % "0.17.4",
      "org.http4s" %% "http4s-ember-server" % "0.23.19",
      "org.scalatest" %% "scalatest" % "3.2.15" % Test,
      "org.typelevel" %% "cats-effect-testing-scalatest" % "1.5.0" % Test,
      "com.bot4s" %% "telegram-core" % "5.6.3",
      "com.softwaremill.sttp.client3" %% "async-http-client-backend-cats" % SttpVersion,
      "io.github.liquibase4s" %% "liquibase4s-core" % "1.0.0",
      "io.github.liquibase4s" %% "liquibase4s-cats-effect" % "1.0.0",
      "com.github.nscala-time" %% "nscala-time" % "2.32.0",
      "com.opentable.components" % "otj-pg-embedded" % "1.0.1" % Test,
      "com.h2database" % "h2" % "2.1.214" % Test,
      "com.softwaremill.sttp.tapir" %% "tapir-derevo" % "1.9.2",
      "dev.bgahagan" %% "scala-regex-interpolation" % "1.0.0",
      "org.testcontainers" % "testcontainers" % "1.17.6" % Test,
      "org.testcontainers" % "postgresql" % "1.17.6" % Test
    ),
    dependencyOverrides += "io.circe" %% "circe-core" % "0.14.5",
    scalacOptions ++= Seq("-Ymacro-annotations")
  )
