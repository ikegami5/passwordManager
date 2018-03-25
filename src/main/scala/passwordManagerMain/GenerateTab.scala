package passwordManagerMain

import scalafx.collections.ObservableBuffer
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control._
import scalafx.scene.layout.{HBox, VBox}
import scalafx.scene.paint.Color
import scalafx.scene.text.{Font, Text}

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
  generateButton.onAction = _ => {

  }
  val passwordNotice: Text = new Text {
    fill = Color.Green
    font = new Font(24)
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
                text = "注意：マスターパスワードは復元不可能だよ\n" +
                  "変更も基本的にできないから気をつけてね"
                fill = Color.Red
              }
            )
          },
          masterPassword
        )
      },
      new VBox {
        alignment = Pos.BottomRight
        children = Seq(generateButton, passwordNotice)
      }
    )
  }
}
