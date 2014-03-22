package cfclient

import org.scalatest.junit.AssertionsForJUnit
import org.junit.Test

import GUIHelper._

class GUIHelperTest extends AssertionsForJUnit {

  val validPorts = List(1024, 5319, 50123, 65535).map(_.toString)
  val invalidPorts = List(-1, 0, 1, 1023, 65536, "", "abc").map(_.toString)

  val validPlayerNames = List("Foo", "foo_", "--bar", "0123")
  val invalidPlayerNames = List("", "?", " ", "abcäöü")

  @Test def testIsValidPort {
    validPorts.foreach(port => assert(isValidPort(port), port))
    invalidPorts.foreach(port => assert(!isValidPort(port), port))
  }

  @Test def testIsValidPlayerName {
    validPlayerNames.foreach(name => assert(isValidPlayerName(name), name))
    invalidPlayerNames.foreach(name => assert(!isValidPlayerName(name), name))
  }

}