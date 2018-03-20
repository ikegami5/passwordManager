package passwordManagerMain

import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.control._

object PasswordManager extends JFXApp {
  stage = new PrimaryStage {
    title = "Password Manager"
    scene = new Scene {
      content = new TabPane() += GenerateTab += RecordTab
    }
  }
}
