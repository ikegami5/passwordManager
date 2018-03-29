package passwordManagerMain

import java.io._
import java.util.NoSuchElementException

import passwordManagerMain.PasswordManager._
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.Alert.AlertType

import scala.io.Source

object Records {
  val file = new File("./data.txt")
  var records: List[Record] = readFromFile()

  def readFromFile(): List[Record] = {
    val source = Source.fromFile(file, "UTF-8")
    try {
      source.getLines.toList.map(s => new Record(s))
    } catch {
      case _: Exception =>
        new MyAlert("読み込みが失敗したよ", AlertType.Error)
        Nil
    } finally {
      source.close()
    }
  }

  def writeToFile(): Unit = {
    val osw = new OutputStreamWriter(new FileOutputStream(file), "UTF-8")
    val writer = new BufferedWriter(osw)
    try {
      writer.write(this.toString)
    } finally {
      writer.close()
    }
  }

  def contains(serviceName: String): Boolean = {
    records.foldLeft(false) {
      (b, record) => b || record.serviceName == serviceName
    }
  }

  def find(serviceName: String): Option[Record] = {
    records.find {
      record => record.serviceName == serviceName
    }
  }

  def append(record: Record): Unit = {
    if (!contains(record.serviceName)) {
      records = records :+ record
      writeToFile()
      GenerateTab.serviceName.items = ObservableBuffer(Records.records.map(record => record.serviceName))
      GenerateTab.serviceName.value = GenerateTab.serviceName.items.get().get(0)
      new MyAlert("サービスの登録が成功したよ", AlertType.Information)
      RecordTab.reset()
    } else {
      new MyAlert("そのサービスはすでにあるよ", AlertType.Error)
    }
  }

  def indexOf(record: Record): Int = {
    records.indexWhere(r => r.serviceName == record.serviceName)
  }

  def update(index: Int, record: Record): Unit = {
    records = records.updated(index, record)
  }

  def appendSalt(record: Record): Unit = {
    update(indexOf(record), apply(record.serviceName).appendSalt())
    writeToFile()
  }

  def apply(serviceName: String): Record = {
    find(serviceName) match {
      case Some(record) => record
      case None => throw new NoSuchElementException
    }
  }

  override def toString: String = records.map(_.toString).mkString(lineSeparator)

}
