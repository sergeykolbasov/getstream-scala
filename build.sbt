name := "getstream-scala-client"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
	"org.json4s" %% "json4s-jackson" % "3.2.11",
	"org.json4s" %% "json4s-ext" % "3.2.11",
	"com.twitter" %% "finagle-http" % "6.24.0",
	"com.typesafe" % "config" % "1.2.1",
	"joda-time" % "joda-time" % "2.7",
	"org.apache.httpcomponents" % "httpcore" % "4.4",
	"org.apache.httpcomponents" % "httpclient" % "4.4"
)
