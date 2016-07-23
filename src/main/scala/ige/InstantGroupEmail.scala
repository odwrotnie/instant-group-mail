package ige

import java.io._

import com.typesafe.scalalogging.LazyLogging
import commons.email.EmailIn
import commons.random.Lipsum
import commons.text.Pattern
import ige.IGE._

import scala.util.Try

object InstantGroupEmail
  extends LazyLogging {

  private var hashToRecipientsMap = collection.mutable.Map[String, Set[String]]()
  def addHashRecipient(hash: String, recipients: Set[String]) = (recipients, hashToRecipientsMap.get(hash)) match {
    case (newRecipients, Some(oldRecipients)) if newRecipients.nonEmpty =>
      hashToRecipientsMap += hash -> (oldRecipients ++ newRecipients)
      logger.debug(s"Added ${ newRecipients.mkString(", ") }:\n${ hashToRecipientsMapString }")
    case (newRecipients, None) if newRecipients.nonEmpty =>
      hashToRecipientsMap += hash -> newRecipients
      logger.debug(s"Added ${ newRecipients.mkString(", ") }:\n${ hashToRecipientsMapString }")
    case (newRecipients, _) if newRecipients.isEmpty =>
      logger.warn(s"Recipients are empty")
  }
  def hashToRecipientsMapString: String = hashToRecipientsMap map {
    case (hash, recipients) => s"  - $hash - ${ recipients.mkString(", ") }"
  } mkString "\n"
  def threadRecipients(hash: String, sender: Option[String] = None): Set[String] =
    hashToRecipientsMap.get(hash).get -- sender.toSet

  def serialize: Unit = {
    logger.debug(s"Serializing threads:\n${ hashToRecipientsMapString }")
    val fos = new FileOutputStream(RECIPIENTS_FILE)
    val oos = new ObjectOutputStream(fos)
    oos.writeObject(hashToRecipientsMap)
    oos.close()
  }

  def deserialize: Unit = {
    val fis = new FileInputStream(RECIPIENTS_FILE)
    val ois = new ObjectInputStream(fis)
    hashToRecipientsMap = ois.readObject().asInstanceOf[collection.mutable.Map[String, Set[String]]]
    ois.close()
    logger.debug(s"Deserialized threads:\n${ hashToRecipientsMapString }")
  }
}

case class InstantGroupEmail(email: EmailIn) {

  import InstantGroupEmail._

  def hash(address: String): Option[String] = WITH_HASH_REGEX.findFirstMatchIn(address).map(_.group(1))
  def sender: String = email.sender

  lazy val threadAddress = IGE.threadAddress(threadHash)
  lazy val threadHash = emailHashIsFresh._1
  lazy val freshThread = emailHashIsFresh._2
  lazy val emailHashIsFresh: (String, Boolean) = email.recipients.flatMap(hash).headOption match {
    case Some(h) => (h, false)
    case _ => (Lipsum.alphanumeric(5), true)
  }
  lazy val emailRecipients: Set[String] = (email.sender :: email.recipientsAll)
    .map(email => Pattern.pickFirst(EMAIL_REGEX)(email).get).filterNot(_.matches(WITH_OR_WITHOUT_HASH_PATTERN)).toSet

  override def toString: String = s"[$threadHash] - ${ email.toString } (${ emailRecipients.mkString(", ") })"
}
