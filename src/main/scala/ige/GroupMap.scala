package ige

import com.typesafe.scalalogging.LazyLogging
import commons.files.ObjectSerializer
import ige.IGE._

object GroupMap
  extends LazyLogging {

  type M = collection.mutable.Map[String, Set[String]]

  private var hashToRecipientsMap: M = collection.mutable.Map[String, Set[String]]()
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
  def groupRecipients(hash: String, sender: Option[String] = None): Set[String] =
    hashToRecipientsMap.get(hash).get -- sender.toSet

  lazy val serializer = ObjectSerializer[M](RECIPIENTS_FILE)

  def serialize: Unit = {
    logger.debug(s"Serializing groups ${ hashToRecipientsMap.size }")
    serializer.serialize(hashToRecipientsMap)
  }

  def deserialize: Unit = serializer.deserialize match {
    case Some(m) =>
      hashToRecipientsMap = m
      logger.debug(s"Deserialized groups:\n${ hashToRecipientsMapString }")
    case _ =>
      logger.warn(s"Nothing to deserialize")
  }
}
