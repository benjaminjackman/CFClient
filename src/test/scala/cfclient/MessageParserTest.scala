package cfclient

import org.scalatest.junit.AssertionsForJUnit
import org.junit.Test

import MessageParser._

class MessageParserTest extends AssertionsForJUnit {

  val welcome1 = """WELCOME 1 "My little CF server""""
  val welcome2 = """WELCOME 1 "My little CF server" """
  val welcome3 = """WELCOME 1 "My little CF server" NO-PING"""

  val disconnect1 = "DISCONNECT"

  val okLobbyUpdate1 = "OK LOBBY-UPDATE"

  val errorLobbyUpdate1 = "ERROR LOBBY-UPDATE"

  val lobbyUpdate1 = """LOBBY-UPDATE enter "Me""""

  val chatMessage1 = """CHAT-MESSAGE "MyName" "Hi, hier ist Simon!""""

  val messages = List(welcome1, welcome2, disconnect1, okLobbyUpdate1, errorLobbyUpdate1, lobbyUpdate1, chatMessage1)

  @Test def testMessages {
    messages.map(MessageParser.parse)
  }

  @Test def testWelcome {
    parseWith(welcome, welcome1)
    parseWith(welcome, welcome2)
    parseWith(welcome, welcome3)
  }

  @Test def testDisconnect {
    parseWith(disconnect, disconnect1)
  }

  @Test def testOk {
    parseWith(ok, okLobbyUpdate1)
  }

  @Test def testError {
    parseWith(error, errorLobbyUpdate1)
  }

  @Test def testLobbyUpdate {
    parseWith(lobbyUpdate, lobbyUpdate1)
  }

  @Test def testChatMessage {
    parseWith(chatMessage, chatMessage1)
  }

}