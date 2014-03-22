package cfclient

import java.net.Socket
import java.nio.charset.StandardCharsets
import java.io.BufferedWriter
import java.io.OutputStreamWriter

trait ClientStates {
  self: Connection =>

  trait ClientState {
    def joinWith(joinMsg: Join): InLobbyState = throw new UnsupportedOperationException
    def socket: Socket = throw new UnsupportedOperationException
    def send(message: Message): Unit = throw new UnsupportedOperationException
    def replyTo(message: Message): Unit = throw new UnsupportedOperationException
    def disconnect(): NotConnectedState = {
      print(s"<<< ${Disconnect.message}")
      writer.writeMessage(Disconnect)
      unsubscribeAll()
      socket.close()
      new NotConnectedState
    }
    def joinFailed: NotJoinedState = throw new UnsupportedOperationException
    def giveup(): InLobbyState = throw new UnsupportedOperationException
    def backToLobby(): InLobbyState = throw new UnsupportedOperationException
    def beginGame(gameBeginMessage: GameBegin): InGameState = throw new UnsupportedOperationException
    def asInGameState: InGameState = asInstanceOf[InGameState]
  }

  trait HasSendAndReply extends ClientState {
    override def send(message: Message) = {
      print(s"<<< ${message.message}")
      writer.writeMessage(message)
    }

    override def replyTo(message: Message) = {
      println(s"<<< ${message.reply} (Reply to $message)")
      message.reply.map(reply => writer.writeMessage(reply))
    }
  }

  class InGameState(override val socket: Socket, val playerIndex: Int, val opponentName: String) extends ClientState with HasSendAndReply {
    override def giveup = {
      writer.writeMessage(Giveup)
      new InLobbyState(socket)
    }
    override def backToLobby =
      new InLobbyState(socket)
  }

  class InLobbyState(override val socket: Socket) extends ClientState with HasSendAndReply {
    assert(socket.isConnected)

    override def beginGame(gameBeginMessage: GameBegin) = {
      replyTo(gameBeginMessage)
      new InGameState(socket, gameBeginMessage.opponentIndex, gameBeginMessage.opponentName)
    }

    override def joinFailed: NotJoinedState =
      new NotJoinedState(socket)
  }

  class NotConnectedState extends ClientState {
    def connect(host: String, port: Int): NotJoinedState = {
      val socket = new Socket(host, port)
      new NotJoinedState(socket)
    }
  }

  class NotJoinedState(override val socket: Socket) extends ClientState {
    override def joinWith(joinMsg: Join): InLobbyState = {
      writer.writeMessage(joinMsg)
      new InLobbyState(socket)
    }
  }
}