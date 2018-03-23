package passwordManagerMain

class Record(info: String) {
  val data: Array[String] = info.split(",", -1).map(s => s.trim)
  val serviceName: String = data(0).replace("\\C", ",")
  val serviceInfo: String = data(1).replace("\\C", ",").replace("\\n", "\n")
  val passwordHasCapital: Boolean = data(2).toBoolean
  val passwordHasNumeral: Boolean = data(3).toBoolean
  val symbolsInPassword: String = data(4).replace("\\C", ",")
  val lengthOfPassword: Int = data(5).toInt
  val salt = data(6)

  override def toString: String = info
}

object Record {

  def apply(serviceName: String, serviceInfo: String, passwordHasCapital: Boolean,
             passwordHasNumeral: Boolean, symbolsInPassword: String, lengthOfPassword: Int,
             salt: String): Record = {
    new Record(Seq(serviceName.replace(",", "\\C"), serviceInfo.replace("\n", "\\n").replace(",", "\\C"),
      passwordHasCapital.toString, passwordHasNumeral.toString,
      symbolsInPassword.replace(",", "\\C"), lengthOfPassword.toString, salt).mkString(", "))
  }

}
