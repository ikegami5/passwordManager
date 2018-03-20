package passwordManagerMain

import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Button, CheckBox, TextArea, TextField}
import scalafx.scene.layout.{HBox, VBox}
import scalafx.scene.text.Text

object RecordTab extends ClosableTab {
  text = "サービス登録"
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

          },
          new Text("パスワードに含める条件"),
          new HBox {
            children = Seq(
              new CheckBox {
                text = "大文字を1文字以上含める"
              },
              new CheckBox {
                text = "数字を1文字以上含める"
              },
              new CheckBox {
                text = "記号を1文字以上含める"
              }
            )
          },
          new HBox {
            alignment = Pos.BottomLeft
            children = Seq(
              new Text("使える記号をスペースやコンマ等なしで入力してね："),
              new TextField {

              }
            )
          },
          new HBox {
            alignment = Pos.BottomLeft
            children = Seq(
              new Text("パスワードの最大文字数："),
              new TextField {

              },
              new Text("文字")
            )
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
