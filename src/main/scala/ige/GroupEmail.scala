package ige

import commons.email.EmailIn
import commons.random.Lipsum
import commons.text.Pattern
import ige.IGE._

case class GroupEmail(email: EmailIn) {

  import GroupMap._

  def hash(address: String): Option[String] = WITH_HASH_REGEX.findFirstMatchIn(address).map(_.group(1))
  def sender: String = email.sender

  lazy val groupAddress = IGE.groupAddress(groupHash)
  lazy val groupHash = emailHashIsFresh._1
  lazy val freshThread = emailHashIsFresh._2
  lazy val emailHashIsFresh: (String, Boolean) = email.recipients.flatMap(hash).headOption match {
    case Some(h) => (h, false)
    case _ => (Lipsum.alphanumeric(5).toLowerCase, true)
  }
  lazy val emailRecipients: Set[String] = (email.sender :: email.recipientsAll)
    .map(email => Pattern.pickFirstEmail(email).get).filterNot(_.matches(WITH_OR_WITHOUT_HASH_PATTERN)).toSet

  override def toString: String = s"[$groupHash] - ${ email.toString } (${ emailRecipients.mkString(", ") })"
}
