package cfclient

import regex._
import Extractors._
import java.lang.{Error => _}

object MessageParser {
  type Parser[T] = String =?> T

  val cfcs: Parser[CFCS.type] = {
    case "CFCS" => CFCS
  }

  val join: Parser[Join] = {
    case gr"""JOIN $protocolName(.*) ${ Int(version) }(\d+) $userName($alphaDigits) $clientExtensions(.*)\n\r""" =>
      Join(protocolName, version, userName, clientExtensions)
  }

  val welcome: Parser[Welcome] = {
    case gr"""WELCOME ${ Int(version) }($digits) "$serverName($alphaDigit)"\s*$commonExtensions(.*)""" =>
      Welcome(serverName, version, commonExtensions)
  }

  val ok: Parser[Ok] = {
    case gr"""OK $msgName($alphaDigits)""" => Ok(msgName)
  }

  val errorNameTaken: Parser[ErrorNameTaken] = {
    case gr"""ERROR JOIN TAKEN $msgText(.*)""" => ErrorNameTaken(msgText)
  }

  val error: Parser[Error] = {
    case gr"""ERROR $msgName($alphaDigits)$msgText(.*)""" => Error(msgName, msgText)
  }

  val disconnect: Parser[Disconnect.type] = {
    case "DISCONNECT" => Disconnect
  }

  val ping: Parser[Ping.type] = {
    case "PING" => Ping
  }

  val pong: Parser[Pong.type] = {
    case "PONG" => Pong
  }

  val lobbyUpdate: Parser[LobbyUpdate] = {
    case gr"""LOBBY-UPDATE enter "$playerName($alphaDigits)".*""" => LobbyUpdateEnter(playerName)
    case gr"""LOBBY-UPDATE leave "$playerName($alphaDigits)".*""" => LobbyUpdateLeave(playerName)
  }

  val challengedBy: Parser[ChallengedBy] = {
    case gr"""CHALLENGED-BY "$playerName($alphaDigits)"""" => ChallengedBy(playerName)
  }

  val challengeRevokedBy: Parser[ChallengeRevokedBy] = {
    case gr"""CHALLENGE-REVOKED-BY "$playerName($alphaDigits)"""" => ChallengeRevokedBy(playerName)
  }

  val challengeDeniedBy: Parser[ChallengeDeniedBy] = {
    case gr"""CHALLENGE-DENIED-BY "$playerName($alphaDigits)"""" => ChallengeDeniedBy(playerName)
  }

  val gameBegin: Parser[GameBegin] = {
    case gr"""GAME-BEGIN ${Int(playerIndex)}($digits) "$playerName($alphaDigits)"""" => GameBegin(playerIndex, playerName)
  }

  val gaveup: Parser[Gaveup] = {
    case gr"""GAVEUP "$playerName($alphaDigits)"""" => Gaveup(playerName)
  }

  val chatMessage: Parser[ChatMessage] = {
    case gr"""CHAT-MESSAGE "$playerName($alphaDigits)"\s+"$chatMessage(.*)"""" => ChatMessage(playerName, chatMessage)
    case gr"""CHAT-MESSAGE $playerName($alphaDigits)\s+"$chatMessage(.*)"""" => ChatMessage(playerName, chatMessage)
  }

  val state: Parser[State] = {
    case gr"""STATE ${Int(matchState)}(\d) ${Int(initToken)}(\d) $fieldDescription(.*)""" => State(matchState, initToken, fieldDescription.split(',').map(_.toInt))
  }

  val unknownMessage: Parser[UnknownMessage] = {
    case msg => UnknownMessage(msg)
  }

  val allMessageParser: Parser[Message] = List(cfcs, join, welcome, ok, errorNameTaken, error, disconnect, ping, pong, lobbyUpdate, challengedBy, challengeRevokedBy, challengeDeniedBy, gameBegin, state, gaveup, chatMessage).reduce(_ orElse _)

  val clientMessageParser: Parser[Message] = List(cfcs, welcome, ok, errorNameTaken, error, disconnect, pong, lobbyUpdate, challengedBy, challengeRevokedBy, challengeDeniedBy, gameBegin, state, gaveup, chatMessage, unknownMessage).reduce(_ orElse _)

  def parse(input: String): Message = clientMessageParser(input)
  def parseWith(parser: Parser[_], input: String) = parser(input)
}
