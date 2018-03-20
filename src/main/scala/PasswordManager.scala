import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.layout.VBox
import scalafx.scene.text.Text

object PasswordManager extends JFXApp {

  private val recordTab = new Tab {
    text = "登録"
    closable = false
    content = new VBox {
      padding = Insets(20)
      children = Seq(
        new VBox {
          children = Seq(
            new Text {
              text = "サービス名（区別しやすい名前を入れてね）"
            },
            new TextField {

            },
            new Text {
              text = "ログイン時に必要な情報など（アカウント名とか）"
            },
            new TextArea {

            }
          )
        },
        new VBox {
          alignment = Pos.BottomRight
          children = new Button {
            text = "登録"
          }
        }
      )
    }
  }

  stage = new PrimaryStage {
    title = "Password Manager"
    scene = new Scene {
      content = new TabPane() += recordTab
    }
  }
}
