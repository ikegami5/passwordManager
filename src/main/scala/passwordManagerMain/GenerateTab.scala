package passwordManagerMain

import scalafx.collections.ObservableBuffer
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Button, ChoiceBox, PasswordField, TextArea}
import scalafx.scene.layout.{HBox, VBox}
import scalafx.scene.paint.{Color, Paint}
import scalafx.scene.text.{Font, Text}

object GenerateTab extends ClosableTab {
  val serviceName: ChoiceBox[String] = new ChoiceBox[String] {
    items = ObservableBuffer(Records.records.map(record => record.serviceName))
    value = if (!items.get().isEmpty) items.get().get(0) else ""
  }
  serviceName.onAction = _ => {
    serviceInfo.text = if (serviceName.value() == null) "" else Records(serviceName.value()).serviceInfo
  }
  val serviceInfo: TextArea = new TextArea {
    text = if (serviceName.value() == "") "" else Records(serviceName.value()).serviceInfo
    editable = false
  }
  val masterPassword = new PasswordField()
  val generateButton = new Button("生成")
  generateButton.onAction = _ => {

  }
  val passwordNotice: Text = new Text {
    fill = Color.Green
    font = new Font(24)
  }

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
