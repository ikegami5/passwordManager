package passwordManagerMain

import java.util.{Timer, TimerTask}

import scalafx.collections.ObservableBuffer
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control._
import scalafx.scene.layout.{HBox, VBox}
import scalafx.scene.paint.Color
import scalafx.scene.text.{Font, Text}
import passwordManagerMain.PasswordManager._

object GenerateTab extends ClosableTab {
  val serviceName: ChoiceBox[String] = new ChoiceBox[String] {
    items = ObservableBuffer(Records.records.map(record => record.serviceName))
    value = if (!items.get().isEmpty) items.get().get(0) else ""
  }
  serviceName.onAction = _ => {
    reload()
  }
  val serviceInfo: TextArea = new TextArea {
    editable = false
  }
  val saltChangeButton: Button = new Button {
    margin = Insets(10)
    text = "パスワード変更"
  }
  saltChangeButton.onAction = _ => {
    Records.appendSalt(Records(serviceName.value()))
    reload()
  }
  val usePreviousSalts: CheckBox = new CheckBox {
    margin = Insets(10)
    text = "以前までのパスワードを使用する"
  }
  usePreviousSalts.onAction = _ => {
    currentSalt.visible = !currentSalt.visible()
  }
  val currentSalt: ChoiceBox[String] = new ChoiceBox[String] {
    margin = Insets(10)
  }
  val masterPassword = new PasswordField()
  val generateButton = new Button("生成")
  class CountdownUntilDeletePassword extends TimerTask {
    var secondsUntilDeletePassword = 30

    override def run(): Unit = {
      if (secondsUntilDeletePassword < 0) {
        writeToClipboard("")
        passwordNotice.text = "パスワードはクリップボードから安全に消去されたよ" + (lineSeparator * 2)
        cancel()
        return
      }
      passwordNotice.text = "パスワードがクリップボードにコピーされたよ。" + lineSeparator +
        "Ctrl + V(Command + V)で貼り付けてね。" + lineSeparator +
        "消去まであと" + secondsUntilDeletePassword + "秒"
      secondsUntilDeletePassword -= 1
    }
  }
  var timer = new Timer()
  var countdownUntilDeletePassword = new CountdownUntilDeletePassword
  def password(): String = {
    val record = Records(serviceName.value())
    val salt = record.salt(currentSalt.selectionModel().selectedIndexProperty().get())
    PasswordGenerator.generate(masterPassword.text(), record, salt)
  }
  generateButton.onAction = _ => {
    if (!(masterPassword.text() matches "[a-zA-Z0-9]{8,}")) {
      masterPassword.text = ""
      new MyAlert("マスターパスワードを入力してね(英数字8文字以上)", AlertType.Error)
    } else if (serviceName.value() == "") {
      masterPassword.text = ""
      new MyAlert("新しいサービスを登録してね", AlertType.Error)
    } else {
      try {
        writeToClipboard(password())
        countdownUntilDeletePassword.cancel()
        countdownUntilDeletePassword = new CountdownUntilDeletePassword
        timer.cancel()
        timer = new Timer()
        timer.schedule(countdownUntilDeletePassword, 0, 1000)
      } catch {
        case e: Exception => new MyAlert(e.getMessage, AlertType.Error)
      } finally {
        masterPassword.text = ""
      }
    }
  }
  val passwordNotice: Text = new Text {
    text = lineSeparator * 2
    margin = Insets(10)
    fill = Color.Green
    font = new Font(18)
  }

  def reload(): Unit = {
    serviceName.value() match {
      case null | "" =>
        serviceInfo.text = ""
        currentSalt.items = ObservableBuffer("現在のパスワード")
      case name =>
        serviceInfo.text = Records(name).serviceInfo
        currentSalt.items = ObservableBuffer(
          Records(name).salt.zipWithIndex.map {
            case (_, index) => if (index == 0) "現在のパスワード" else index + "個前のパスワード"
          }
        )
    }
    usePreviousSalts.selected = false
    currentSalt.visible = false
    currentSalt.value = currentSalt.items.get().get(0)
  }
  reload()

  text = "パスワード生成"
  content = new VBox {
    padding = Insets(20)
    children = Seq(
      new VBox {
        children = Seq(
          new Text {
            text = "パスワードを生成するサービスを選んでね"
          },
          serviceName,
          serviceInfo,
          new HBox {
            alignment = Pos.BottomLeft
            children = Seq(saltChangeButton, usePreviousSalts, currentSalt)
          },
          new HBox {
            alignment = Pos.BaselineLeft
            children = Seq(
              new Text("マスターパスワード(8文字以上)を入力してね   "),
              new Text {
                text = "注意：マスターパスワードは復元不可能だよ" + lineSeparator +
                  "変更も基本的にできないから気をつけてね"
                fill = Color.Red
              }
            )
          },
          masterPassword
        )
      },
      new HBox {
        alignment = Pos.BottomRight
        children = Seq(passwordNotice, generateButton)
      }
    )
  }
}
