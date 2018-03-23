package passwordManagerMain

import scalafx.collections.ObservableBuffer
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Button, ChoiceBox, PasswordField, TextArea}
import scalafx.scene.layout.{HBox, VBox}
import scalafx.scene.paint.Color
import scalafx.scene.text.Text

object GenerateTab extends ClosableTab {
  text = "パスワード生成"
  content = new VBox {
    padding = Insets(20)
    children = Seq(
      new VBox {
        children = Seq(
          new Text {
            text = "パスワードを生成するサービスを選んでね"
          },
          new ChoiceBox[String] {
            items = ObservableBuffer("aaaaaaaaaaaaaa", "bbbbbbbbbbbbb")
            value = items.get().get(0)
          },
          new TextArea {
            editable = false
          },
          new HBox {
            alignment = Pos.BaselineLeft
            children = Seq(
              new Text("マスターパスワードを入力してね   "),
              new Text {
                text = "注意：マスターパスワードは復元不可能だよ\n" +
                  "変更も基本的にできないから気をつけてね"
                fill = Color.Red
              }
            )
          },
          new PasswordField {

          }
        )
      },
      new VBox {
        alignment = Pos.BottomRight
        children = Seq(
          new Button {
            text = "生成"
          },
          new Text {
            text = ""
          }
        )
      }
    )
  }
}
