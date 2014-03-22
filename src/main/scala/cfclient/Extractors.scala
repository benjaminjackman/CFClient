package cfclient

trait Extractors {
  object Int {
    def unapply(s: String): Option[Int] = util.Try(s.toInt).toOption
  }
}

object Extractors extends Extractors