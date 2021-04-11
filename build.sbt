lazy val programName = "PasswordManager"
lazy val jarFileName = programName + ".jar"

name := programName
version := "0.2"
scalaVersion := "2.12.13"
libraryDependencies += "org.scalafx" %% "scalafx" % "11-R16"

lazy val javaFXModules = Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")
libraryDependencies ++= javaFXModules.map(
  m => "org.openjfx" % s"javafx-$m" % "11.0.2" classifier "mac" classifier "win" classifier "linux"
)

// sbt-assembly settings
test in assembly := {}
assemblyJarName in assembly := jarFileName
mainClass in assembly := Some("passwordManagerMain." + programName)
assemblyOutputPath in assembly := new File("./" + jarFileName)
assemblyMergeStrategy in assembly := {
  case "module-info.class" => MergeStrategy.first
  case "com/sun/javafx/runtime/VersionInfo.class" => MergeStrategy.first
  case "com/sun/javafx/scene/control/skin/caspian/caspian.bss" => MergeStrategy.first
  case "com/sun/javafx/scene/control/skin/caspian/fxvk.bss" => MergeStrategy.first
  case "com/sun/javafx/scene/control/skin/modena/blackOnWhite.bss" => MergeStrategy.first
  case "com/sun/javafx/scene/control/skin/modena/modena.bss" => MergeStrategy.first
  case "com/sun/javafx/scene/control/skin/modena/whiteOnBlack.bss" => MergeStrategy.first
  case "com/sun/javafx/scene/control/skin/modena/yellowOnBlack.bss" => MergeStrategy.first
  case "javafx-swt.jar" => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
