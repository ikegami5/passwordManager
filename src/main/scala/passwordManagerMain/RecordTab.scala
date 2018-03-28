package passwordManagerMain

import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.{Button, CheckBox, TextArea, TextField}
import scalafx.scene.layout.{HBox, VBox}
import scalafx.scene.text.Text

object RecordTab extends ClosableTab {
  val serviceName = new TextField()
  val serviceInfo = new TextArea()
  val passwordHasCapital: CheckBox = new CheckBox {
    text = "大文字を1文字以上含める"
    selected = true
  }
  val passwordHasNumeral: CheckBox = new CheckBox {
    text = "数字を1文字以上含める"
    selected = true
  }
  val passwordHasSymbol = new CheckBox("記号を1文字以上含める")
  passwordHasSymbol.onAction = _ => {
    symbolsInPasswordBox.visible = !symbolsInPasswordBox.visible()
    symbolsInPassword.text = ""
  }
  val symbolsInPassword = new TextField()
  val symbolsInPasswordBox: HBox = new HBox {
    alignment = Pos.BottomLeft
    children = Seq(
      new Text("使える記号を入力してね(スペースやコンマ等も記号と判定されるよ)："),
      symbolsInPassword
    )
  }
  symbolsInPasswordBox.visible = false
  val lengthOfPassword = new TextField()
  val recordButton = new Button("登録")
  recordButton.onAction = _ => {
    class InputException(message: String) extends Exception(message)
    try {
      if (serviceName.text().isEmpty) throw new InputException("サービス名が空だよ")
      if (passwordHasSymbol.selected() && !(symbolsInPassword.text() matches """[ !"#$%&'()*+,-./:;<=>?@\[\\¥\]^_`{|}~]+"""))
        throw new InputException("使う記号の欄にちゃんとした物を入力してね")
      if (lengthOfPassword.text().toInt < 4) throw new InputException("パスワードの長さは最短4文字だよ")
      Records.append(Record(serviceName.text(), serviceInfo.text(), passwordHasCapital.selected(),
        passwordHasNumeral.selected(), symbolsInPassword.text(), lengthOfPassword.text().toInt))
    } catch {
      case e: InputException => new MyAlert(e.getMessage, AlertType.Error)
      case _: Exception => new MyAlert("正しい値を入力してね", AlertType.Error)
    }
  }

  def reset(): Unit = {
    serviceName.text = ""
    serviceInfo.text = ""
    passwordHasCapital.selected = true
    passwordHasNumeral.selected = true
    passwordHasSymbol.selected = false
    symbolsInPassword.text = ""
    symbolsInPasswordBox.visible = false
    lengthOfPassword.text = ""
  }

  text = "サービス登録"
  content = new VBox {
    padding = Insets(20)
    children = Seq(
      new VBox {
        children = Seq(
          new Text("サービス名（区別しやすい名前を入れてね）"),
          serviceName,
          new Text("ログイン時に必要な情報など（アカウント名とか）"),
          serviceInfo,
          new Text("パスワードに含める条件"),
          new HBox {
            children = Seq(passwordHasCapital, passwordHasNumeral, passwordHasSymbol)
          },
          symbolsInPasswordBox,
          new HBox {
            alignment = Pos.BottomLeft
            children = Seq(
              new Text("パスワードの最大文字数："),
              lengthOfPassword,
              new Text("文字")
            )
          }
        )
      },
      new VBox {
        alignment = Pos.BottomRight
        children = recordButton
      }
    )
  }
}
