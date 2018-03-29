package passwordManagerMain

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

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

  def lineSeparator: String = System.lineSeparator()

  def writeToClipboard(str: String): Unit = {
    val clipboard = Toolkit.getDefaultToolkit.getSystemClipboard
    val stringSelection = new StringSelection(str)
    clipboard.setContents(stringSelection, stringSelection)
  }

  override def stopApp(): Unit = {
    writeToClipboard("")
    GenerateTab.countdownUntilDeletePassword.cancel()
    GenerateTab.timer.cancel()
    super.stopApp()
  }
}
