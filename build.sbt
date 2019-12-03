enablePlugins(JDKPackagerPlugin)

organization  := "org.phenoscape"

name          := "Phenex"

version       := "1.21.2"

mainClass in Compile := Some("org.phenoscape.main.Phenex")

jdkAppIcon := Some(file("launchers/macosx/Seal-of-Phenex.icns"))

publishArtifact in (Compile, packageDoc) := false

publishArtifact in Test := false

jdkPackagerJVMArgs := Seq("-Xmx8g")

licenses := Seq("MIT" -> url("https://opensource.org/licenses/MIT"))

homepage := Some(url("https://wiki.phenoscape/wiki/Phenex"))

crossPaths := false // drop off Scala suffix from artifact names.
autoScalaLibrary := false // exclude scala-library from dependencies

fork in Test := true
