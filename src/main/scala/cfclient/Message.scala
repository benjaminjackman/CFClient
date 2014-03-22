package cfclient

import MessageParser._
import RegexHelper._

trait Message {
  def message: String = ""
  def reply: Option[Message] = None
}

case object CFCS extends Message

case object Join {
  def withName(playerName: String) = new Join(playerName = playerName)
}
case class Join(protocolName: String = "CFCS", version: Int = 1, playerName: String, extensions: String = " CHAT") extends Message {
  override def message = s"""JOIN $protocolName $version "$playerName"$extensions\r\n"""
}

case class Welcome(serverName: String, version: Int, commonExtensions: String) extends Message {
  override def reply = Some(Ok("WELCOME"))
}

case class Ok(messageName: String) extends Message {
  override def message = s"""OK $messageName\r\n"""
}

case class Error(messageName: String, messageText: String) extends Message
case class ErrorNameTaken(messageText: String) extends Message

case object Disconnect extends Message {
  override def message = "DISCONNECT\r\n"
}

case object Ping extends Message {
  override def message = "PING\r\n"
}

case object Pong extends Message

sealed trait LobbyUpdate extends Message {
  override def reply = Some(Ok("LOBBY-UPDATE"))
  def opponentName: String
}

case class LobbyUpdateEnter(opponentName: String) extends LobbyUpdate

case class LobbyUpdateLeave(opponentName: String) extends LobbyUpdate

case class Challenge(opponentName: String) extends Message {
  override def message = s"""CHALLENGE "$opponentName"\r\n"""
}

case class AcceptChallenge(opponentName: String) extends Message {
  override def message = s"""ACCEPT "$opponentName"\r\n"""
}

case class DenyChallenge(opponentName: String) extends Message {
  override def message = s"""DENY "$opponentName"\r\n"""
}

case class RevokeChallenge(opponentName: String) extends Message {
  override def message = s"""REVOKE "$opponentName"\r\n"""
}

case class ChallengedBy(opponentName: String) extends Message {
  override def reply = Some(Ok("CHALLENGED-BY"))
}

case class ChallengeRevokedBy(playerName: String) extends Message {
  override def reply = Some(Ok("CHALLENGE-REVOKED-BY"))
}

case class ChallengeDeniedBy(playerName: String) extends Message {
  override def reply = Some(Ok("CHALLENGE-DENIED-BY"))
}

case class GameBegin(opponentIndex: Int, opponentName: String) extends Message {
  override def reply = Some(Ok("GAME-BEGIN"))
}

case object Giveup extends Message {
  override def message = s"""GIVEUP\r\n"""
}

case class Gaveup(playerName: String) extends Message {
  override def reply = Some(Ok("GAVEUP"))
}

case class MakeMove(column: Int) extends Message {
  override def message = s"""MAKE-MOVE $column\r\n"""
}

case class State(matchState: Int, initToken: Int, fieldDescription: Array[Int]) extends Message {
  override def reply = Some(Ok("STATE"))
}

case class SendChatMessage(messageText: String) extends Message {
  override def message = s"""SEND-CHAT-MESSAGE "$messageText"\r\n"""
}

case class ChatMessage(opponentName: String, chatMessage: String) extends Message {
  override def reply = Some(Ok("CHAT-MESSAGE"))
}

case class UnknownMessage(msg: String) extends Message {
  override def message = ???
}
