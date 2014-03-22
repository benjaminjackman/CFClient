package cfclient

import scala.util.Try

object GUIHelper {
  def isValidPort(port: String) = {
    val int = Try(port.toInt)
    int.filter(i => i >= 1024 && i <= 65535).isSuccess
  }

  def isValidPlayerName(playerName: String) =
    playerName.length >= 3 &&
    playerName.length <= 16 &&
    playerName.matches(RegexHelper.alphaDigitsAtLeastOne)

  def isValidChatMessage(chatMessage: String) =
    !chatMessage.contains('"') && !chatMessage.exists(_.isControl)
}