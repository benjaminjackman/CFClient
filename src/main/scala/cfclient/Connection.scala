package cfclient

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.net.ConnectException
import java.awt.EventQueue
import scala.beans.BeanProperty
import scala.concurrent.duration._
import rx.lang.scala.Observable
import rx.lang.scala.Observer
import rx.lang.scala.Subscription
import scala.collection.mutable.ArrayBuffer
import java.awt.EventQueue.{ invokeLater => onGUIThread }
import java.util.ArrayList
import scala.collection.mutable.HashSet

class Connection(host: String, port: Int, val gui: MainGUI) extends ClientStates with GUIActions {

  @volatile @BeanProperty
  var clientState: ClientState = (new NotConnectedState).connect(host, port)
  @volatile
  var lastMessageFromServer = -1L

  val pendingSentChallenges = HashSet[String]()
  val pendingReceivedChallenges = HashSet[String]()

  val reader = new BufferedReader(new InputStreamReader(clientState.socket.getInputStream, StandardCharsets.UTF_8))
  val writer = new BufferedWriter(new OutputStreamWriter(clientState.socket.getOutputStream, StandardCharsets.UTF_8))

  val socketInputObservable = reader.toObservable.publish.refCount
  val socketInputDebugSubscription = socketInputObservable.subscribe(input => println(s">>> $input"))

  val messageObservable = socketInputObservable.map(input => MessageParser.parse(input)).publish.refCount
  val messageDebugSubscription = messageObservable.subscribe(message => { println(s">>> $message"); clientState.replyTo(message) })
  val messageGUISubscription = messageObservable.subscribe(allGUIActions)

  val stateChanges: Message =?> Unit = {
    case gameBeginMessage: GameBegin => beginGame(gameBeginMessage)
    case errorNameTakenMessage: ErrorNameTaken => handleErrorNameTaken()
    case gaveupMessage: Gaveup => handleGaveup()
    case _ =>
  }

  val messageStateSubscription = messageObservable
    .subscribe(stateChanges)
  val messageTimeoutUpdateSubscription = messageObservable.subscribe(_ => lastMessageFromServer = System.nanoTime())

  val pingObservable = Observable.interval(9.seconds)
  val pingSubscription = pingObservable.subscribe(_ => clientState.send(Ping))

  val timeoutObservable = Observable.interval(12.seconds)
  val timeoutSubscription = timeoutObservable.subscribe(_ => if (timeoutCondition) {println("!!! No message received within timeout"); gui.disconnect()})

  def timeoutCondition = (System.nanoTime() - lastMessageFromServer) > 12.seconds.toNanos && isJoined

  //  socketInputObservable.connect
  //  messageObservable.connect

  val subscriptions = List(
    socketInputDebugSubscription,
    messageDebugSubscription,
    messageGUISubscription,
    messageStateSubscription,
    pingSubscription)

  def send(message: Message): Unit = clientState.send(message)
  def isConnected = !clientState.isInstanceOf[NotConnectedState]
  def isJoined = !clientState.isInstanceOf[NotConnectedState] && !clientState.isInstanceOf[NotJoinedState]

  def disconnect() = clientState = clientState.disconnect()

  def unsubscribeAll(): Unit = {
    subscriptions.map(_.unsubscribe())
    println("!!! Observables unsubscribed.")
  }

  def handleErrorNameTaken() =
    clientState = clientState.joinFailed

  def handleGaveup() =
    clientState = clientState.backToLobby()

  def challenge(opponentName: String) = {
    clientState.send(Challenge(opponentName));
    pendingSentChallenges add opponentName
  }

  def acceptChallenge(opponentName: String) = {
    clientState.send(AcceptChallenge(opponentName));
    pendingReceivedChallenges remove opponentName
    pendingReceivedChallenges.foreach(denyChallenge)
    pendingSentChallenges.foreach(revokeChallenge)
  }

  def denyChallenge(opponentName: String) = {
    if (pendingReceivedChallenges contains opponentName) {
      clientState.send(DenyChallenge(opponentName));
      pendingReceivedChallenges remove opponentName
    } else {
      // Challenge has already been revoked, show message
      onGUIThread(() => gui.signalChallengeBy(opponentName))
    }
  }

  def revokeChallenge(opponentName: String) = {
    clientState.send(RevokeChallenge(opponentName));
    pendingSentChallenges remove opponentName
  }

  def giveup = {
    clientState = clientState.giveup
  }

  def beginGame(gameBeginMessage: GameBegin) = {
    val inGameState = clientState.beginGame(gameBeginMessage)
    clientState = inGameState
  }

  def isInGame: Boolean = clientState.isInstanceOf[InGameState]
}