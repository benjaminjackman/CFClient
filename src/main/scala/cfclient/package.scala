import scala.language.implicitConversions
import java.io.IOException
import java.io.BufferedReader
import java.io.BufferedWriter
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import rx.lang.scala.Observable
import rx.lang.scala.Observer
import rx.lang.scala.Subscription
import java.net.SocketException

package object cfclient {
  type =?>[-A, +B] = PartialFunction[A, B]

  implicit class RichBufferedReader(bufferedReader: BufferedReader) {
    def toObservable: Observable[String] = {
      Observable.create((observer: Observer[String]) => {
        val thread = new Thread(new Runnable() {
          def run() {
            @volatile var break = false
            while (!Thread.interrupted && !break) {
              try {
                val command = bufferedReader.readLine()
                if (command != null)
                  observer.onNext(command)
              } catch {
                case ioEx: IOException =>
                  bufferedReader.close()
                  break = true
                  observer.onError(ioEx)
              }
            }
            observer.onCompleted()
          }
        })
        thread.start();

        Subscription.apply(thread.interrupt())
      })
    }
  }

  implicit class RichBufferedWriter(writer: BufferedWriter) {
    def writeMessage(message: Message) = {
      try {
        writer.write(message.message)
        writer.flush()
      } catch {
        case se: SocketException =>
          println("Message could not be delivered to server!")
      }
    }
  }

  implicit def functionToActionListener(func: ActionEvent => Unit): ActionListener =
    new ActionListener { def actionPerformed(event: ActionEvent): Unit = func(event) }

  implicit def functionToRunnable(func: () => Any): Runnable =
    new Runnable { def run(): Unit = func() }
}