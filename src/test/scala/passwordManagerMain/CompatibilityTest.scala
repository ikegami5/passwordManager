package passwordManagerMain

import scala.io.Source
import scala.collection.mutable.ArrayBuffer
import scala.reflect.ClassTag

/** Tests the same generator that the GUI calls; never initializes the application. */
object CompatibilityTest {
  def main(args: Array[String]): Unit = {
    val failures = ArrayBuffer.empty[String]
    var checks = 0
    def check(label: String)(condition: => Boolean): Unit = {
      checks += 1
      try {
        if (!condition) failures += label
      } catch {
        case e: Exception => failures += s"$label (${e.getClass.getSimpleName})"
      }
    }
    def throws[T <: Throwable: ClassTag](body: => Any): Boolean = {
      try { body; false }
      catch { case e: Throwable => implicitly[ClassTag[T]].runtimeClass.isInstance(e) }
    }
    def lines(name: String): Vector[String] = {
      val stream = Option(getClass.getResourceAsStream(s"/compatibility/$name"))
        .getOrElse(throw new IllegalStateException(s"Missing fixture: $name"))
      val source = Source.fromInputStream(stream, "UTF-8")
      try source.getLines().toVector finally source.close()
    }

    val rows = lines("records.txt")
    val expected = lines("expected.txt")
    val masterLines = lines("master.txt")
    require(rows.size == 67 && expected.size == 67 && masterLines.size == 1,
      "Expected exactly 67 fixture pairs and one master password")
    val master = masterLines.head
    val records = rows.map(new Record(_))
    def generate(record: Record, salt: String): String =
      PasswordGenerator.generate(master, record, salt)

    // Exact GUI baselines: never replace them with output from this implementation.
    records.zipWithIndex.foreach { case (record, index) =>
      val label = s"GUI sample ${index + 1}"
      check(s"$label: service order") { record.serviceName == (index + 1).toString }
      check(s"$label: supplied output length") { expected(index).length == record.lengthOfPassword }
      check(s"$label: exact output") { generate(record, record.salt.head) == expected(index) }
      check(s"$label: original row retained") { record.toString == rows(index) }
      check(s"$label: newest salt selected") {
        record.salt.head == rows(index).split(",", -1).last.trim
      }
    }
    // Reverse order exercises the shared Random state between different services.
    records.reverse.foreach { record =>
      check(s"sample ${record.serviceName}: repeat after other services") {
        generate(record, record.salt.head) == expected(record.serviceName.toInt - 1)
      }
      if (record.salt.size > 1) {
        val oldSalt = record.salt.last
        val before = generate(record, oldSalt)
        generate(record, record.salt.head)
        check(s"sample ${record.serviceName}: historical salt reproducibility") {
          generate(record, oldSalt) == before
        }
      }
    }

    val baseSalt = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz01"
    def row(capital: Boolean, numeral: Boolean, symbols: String, length: Int): Record =
      new Record(s"synthetic, , $capital, $numeral, $symbols, $length, $baseSalt")
    for {
      capital <- Seq(false, true)
      numeral <- Seq(false, true)
      symbols <- Seq("", "!@#", "\\S\\C-[]^$.*+?(){}|/\\")
      length <- Seq(4, 16, 64)
    } {
      val record = row(capital, numeral, symbols, length)
      // This fixed short input exhausts the legacy 101-candidate search.
      // Valid character requirements do not guarantee successful generation.
      if (capital && numeral && symbols.nonEmpty && symbols != "!@#" && length == 4) {
        check("legacy short-password candidate exhaustion") {
          try { generate(record, baseSalt); false }
          catch {
            case e: Exception => e.getMessage ==
              "パスワードが生成できなかったよ" + System.lineSeparator() + "パスワード変更を試してみてね"
          }
        }
      } else check(s"synthetic constraints: $capital/$numeral/${symbols.length}/$length") {
        val result = generate(record, baseSalt)
        val allowed = "abcdefghijklmnopqrstuvwxyz" +
          (if (capital) "ABCDEFGHIJKLMNOPQRSTUVWXYZ" else "") +
          (if (numeral) "0123456789" else "") + record.symbolsInPassword
        result.length == length && result.forall(allowed.contains(_)) &&
          result.exists(c => c >= 'a' && c <= 'z') &&
          (!capital || result.exists(c => c >= 'A' && c <= 'Z')) &&
          (!numeral || result.exists(c => c >= '0' && c <= '9')) &&
          (record.symbolsInPassword.isEmpty || result.exists(record.symbolsInPassword.contains(_)))
      }
    }
    check("impossible one-character requirements fail") {
      throws[Exception] { generate(row(true, true, "!", 1), baseSalt) }
    }

    val parsed = new Record(" name\\Ctest , first\\nsecond\\Cend , false , true , \\S\\C! , 16 , old , new ")
    check("record escapes and trim") {
      parsed.serviceName == "name,test" &&
        parsed.serviceInfo == "first" + System.lineSeparator() + "second,end" &&
        !parsed.passwordHasCapital && parsed.passwordHasNumeral &&
        parsed.symbolsInPassword == " ,!" && parsed.lengthOfPassword == 16 &&
        parsed.salt == List("new", "old")
    }
    check("empty fields retained") {
      val record = new Record("empty, , false, false, , 4, salt")
      record.serviceInfo.isEmpty && record.symbolsInPassword.isEmpty
    }
    Seq("\n", "\r\n", "\r").foreach { separator =>
      check(s"newline normalization ${separator.map(_.toInt).mkString(",")}") {
        LineSeparators.unifyLineSeparator("a" + separator + "b") ==
          "a" + System.lineSeparator() + "b"
      }
    }
    val created = Record("名前,例", "説明\r\n次,行", true, false, "!! ,", 100)
    check("record creation, deduplication and length cap") {
      created.serviceName == "名前,例" &&
        created.serviceInfo == "説明" + System.lineSeparator() + "次,行" &&
        created.lengthOfPassword == 64 && created.symbolsInPassword.toSet == Set('!', ' ', ',') &&
        created.symbolsInPassword.length == 3
    }
    val restored = new Record(created.toString)
    check("record serialization round trip") {
      restored.serviceName == created.serviceName && restored.serviceInfo == created.serviceInfo &&
        restored.passwordHasCapital == created.passwordHasCapital &&
        restored.passwordHasNumeral == created.passwordHasNumeral &&
        restored.symbolsInPassword == created.symbolsInPassword &&
        restored.lengthOfPassword == created.lengthOfPassword && restored.salt == created.salt
    }
    val appended = parsed.appendSalt()
    check("salt append preserves old history and original record") {
      appended.salt.tail == parsed.salt && parsed.salt == List("new", "old") &&
        appended.serviceName == parsed.serviceName && appended.symbolsInPassword == parsed.symbolsInPassword
    }
    check("generated salt format") {
      (Seq(Record.generateSalt(), created.salt.head, appended.salt.head)).forall(_.matches("[a-zA-Z0-9]{64}"))
    }
    check("malformed record rejected") { throws[IndexOutOfBoundsException] { new Record("bad") } }
    check("invalid boolean rejected") {
      throws[IllegalArgumentException] { new Record("bad, , invalid, true, , 16, salt") }
    }
    check("invalid length rejected") {
      throws[NumberFormatException] { new Record("bad, , true, true, , invalid, salt") }
    }

    println(s"Compatibility checks: $checks, passed: ${checks - failures.size}, failed: ${failures.size}")
    failures.foreach(label => println(s"FAIL: $label"))
    if (failures.nonEmpty) throw new AssertionError("Compatibility checks failed; fixture values were not modified")
  }
}
