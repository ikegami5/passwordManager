lazy val programName = "PasswordManager"
lazy val jarFileName = programName + ".jar"

name := programName
version := "0.2"
scalaVersion := "2.12.13"
libraryDependencies += "org.scalafx" %% "scalafx" % "11-R16"

// Determine OS version of JavaFX binaries
lazy val osName = System.getProperty("os.name") match {
  case n if n.startsWith("Linux")   => "linux"
  case n if n.startsWith("Mac")     => "mac"
  case n if n.startsWith("Windows") => "win"
  case _ => throw new Exception("Unknown platform!")
}

// Add dependency on JavaFX libraries, OS dependent
lazy val javaFXModules = Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")
libraryDependencies ++= javaFXModules.map(
  m => "org.openjfx" % s"javafx-$m" % "11.0.2" classifier osName
)

// sbt-assembly settings
test in assembly := {}
assemblyJarName in assembly := jarFileName
mainClass in assembly := Some("passwordManagerMain." + programName)
assemblyOutputPath in assembly := new File("./" + jarFileName)
assemblyMergeStrategy in assembly := {
  case "module-info.class" => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
