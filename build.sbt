

name := "WeatherReceiver"

version := "1.0"

//lazy val `weatherreceiver` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

//enablePlugins(JavaAppPackaging)

mainClass in Compile := Some("Main")

libraryDependencies ++= Seq( jdbc , anorm , cache , ws )

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

libraryDependencies ++= Seq(
  "com.twitter" %% "finagle-core" % "6.33.0",
  "com.twitter" %% "finagle-http" % "6.33.0",
  "io.argonaut" %% "argonaut" % "6.1",
  "com.typesafe.akka" %% "akka-actor" % "2.4.4",
  "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test"
)