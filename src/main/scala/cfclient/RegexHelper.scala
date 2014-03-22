package cfclient

object RegexHelper {
  val digit = """\d"""

  val digits = """\d+"""

  val alphaDigits = """[A-Za-z0-9-_]*"""

  val alphaDigitsAtLeastOne = """[A-Za-z0-9-_]+"""

  val alphaDigitDQuotes = """[A-Za-z0-9-_\" ]*"""
}
