package cfclient

import java.awt.EventQueue.{ invokeLater => onGUIThread }

trait GUIActions {
  self: Connection =>

    type GUIAction = Message =?> Unit

    val errorNameTaken: GUIAction = {
      case ErrorNameTaken(_) => onGUIThread(() => gui.signalErrorNameTaken())
    }

    val challengedBy: GUIAction = {
      case ChallengedBy(opponentName) => onGUIThread(() => gui.signalChallengeBy(opponentName))
    }

    val challengeDeniedBy: GUIAction = {
      case ChallengeDeniedBy(opponentName) => onGUIThread(() => gui.signalChallengeDeniedBy(opponentName))
    }

    val gameBegin: GUIAction = {
      case GameBegin(opponentIndex, opponentName) => onGUIThread(() => gui.replaceOpponentListWithGameGrid(opponentName))
    }

    val lobbyUpdateEnter: GUIAction = {
      case LobbyUpdateEnter(opponentName) => onGUIThread(() => gui.addOpponent(opponentName))
    }

    val lobbyUpdateLeave: GUIAction = {
      case LobbyUpdateLeave(opponentName) => onGUIThread(() => gui.removeOpponent(opponentName))
    }

    val chatMessage: GUIAction = {
      case ChatMessage(opponentName, chatMessage) => onGUIThread(() => gui.addChatMessage(opponentName, chatMessage))
    }

    val gaveup: GUIAction = {
      case Gaveup(opponentName) => onGUIThread(() => gui.signalGaveup(opponentName))
    }

    val state: GUIAction = {
      case State(matchState, initToken, fieldDescription) => onGUIThread(() => gui.updateGameState(matchState, initToken, fieldDescription))
    }

    val ignoreMessage: GUIAction = {
      case _ =>
    }

    val allGUIActions = List(errorNameTaken, challengedBy, challengeDeniedBy, gameBegin, lobbyUpdateEnter, lobbyUpdateLeave, chatMessage, state, gaveup, ignoreMessage) reduce (_ orElse _)
}