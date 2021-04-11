package passwordManagerMain

import java.math.BigInteger
import java.security.MessageDigest
import java.util.{Timer, TimerTask}

import scalafx.collections.ObservableBuffer
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control._
import scalafx.scene.layout.{HBox, VBox}
import scalafx.scene.paint.Color
import scalafx.scene.text.{Font, Text}
import passwordManagerMain.PasswordManager._

import scala.util.Random

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
    val salt = Records(serviceName.value()).salt(currentSalt.selectionModel().selectedIndexProperty().get())
    def hash(str: String, saltIsBack: Boolean): String = {
      val md = MessageDigest.getInstance("SHA-512")
      val saltyStr = if (saltIsBack) str + salt else salt + str
      md.update(saltyStr.getBytes)
      val byteArray = md.digest()
      val buffer = new StringBuffer
      for (byte <- byteArray) {
        val binaryStr = Integer.toBinaryString(byte & 0xff)
        buffer.append("0" * (8 - binaryStr.length)).append(binaryStr)
      }
      buffer.toString
    }
    val hashTwice: String => String = (x: String) => hash(hash(x, saltIsBack = true), saltIsBack = false)
    val hash8times: String => String = (hashTwice compose hashTwice) compose (hashTwice compose hashTwice)
    val hash32times: String => String = (hash8times compose hash8times) compose (hash8times compose hash8times)
    val hash64times: String => String = hash32times compose hash32times
    val hashedString = hash64times(masterPassword.text())
    def arrangeInPasswordFormat(str: String): String = {
      val record = Records(serviceName.value())
      val passwordLength: Int = record.lengthOfPassword
      val initialSeed = Integer.parseInt(str.substring(456, 484), 2) + Integer.parseInt(str.substring(484), 2)
      val alphabets = ('a' to 'z').toList.mkString
      val capitalAlphabets = if (record.passwordHasCapital) alphabets.toUpperCase else ""
      val numerals = if (record.passwordHasNumeral) "0123456789" else ""
      val symbols = record.symbolsInPassword
      def lettersList(seed: Int): List[String] = {
        Random.setSeed(seed)
        Random.shuffle((alphabets + capitalAlphabets + numerals + symbols).toList.map((_: Char).toString))
      }
      for (addSeed <- 0 to 100) {
        val tmpPassword = (for (i <- 0 until passwordLength) yield {
          val wordLength: Int = str.length / passwordLength
          val wordString = str.substring(wordLength * i, wordLength * i + wordLength - 1)
          val tmpLettersList = lettersList(initialSeed + addSeed)
          tmpLettersList((BigInt(new BigInteger(wordString, 2)) % tmpLettersList.length).toInt)
        }).mkString
        def isProperPassword: Boolean = {
          (tmpPassword matches ".*[a-z].*") &&
            (if (record.passwordHasCapital) (tmpPassword matches (".*[" + capitalAlphabets + "].*")) else true) &&
            (if (record.passwordHasNumeral) (tmpPassword matches (".*[" + numerals + "].*")) else true) &&
            (if (symbols.isEmpty()) true else (tmpPassword matches (".*[" + symbols.replaceAll("[\\Q-/\\^$*+?.()|[]{}\\E]", "\\\\$0") + "].*")))
        }
        if (isProperPassword) return tmpPassword
      }
      throw new Exception("パスワードが生成できなかったよ" + lineSeparator + "パスワード変更を試してみてね")
    }
    arrangeInPasswordFormat(hashedString)
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
