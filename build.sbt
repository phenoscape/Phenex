enablePlugins(JDKPackagerPlugin)

organization  := "org.phenoscape"

name          := "Phenex"

version       := "1.21-beta.3"

mainClass in Compile := Some("org.phenoscape.main.Phenex")

jdkAppIcon := Some(file("launchers/macosx/Seal-of-Phenex.icns"))

publishArtifact in (Compile, packageDoc) := false

publishArtifact in Test := false

jdkPackagerJVMArgs := Seq("-Xmx8g")

//licenses := Seq("BSD-3-Clause" -> url("https://opensource.org/licenses/BSD-3-Clause"))

//homepage := Some(url("https://github.com/phenoscape/sparql-interpolator"))

crossPaths := false // drop off Scala suffix from artifact names.
autoScalaLibrary := false // exclude scala-library from dependencies

fork in Test := true
