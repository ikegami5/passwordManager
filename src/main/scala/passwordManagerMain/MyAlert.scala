package passwordManagerMain

import scalafx.scene.control.Alert
import scalafx.scene.control.Alert.AlertType

class MyAlert(message: String, alertType: AlertType) extends Alert(alertType) {
  contentText = message
  showAndWait()
}
