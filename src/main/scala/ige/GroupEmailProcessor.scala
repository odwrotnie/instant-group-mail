package ige

import com.typesafe.scalalogging.LazyLogging
import commons.email.EmailOut
import org.apache.commons.mail.EmailException

case class GroupEmailProcessor(ige: GroupEmail)
  extends LazyLogging {

  import GroupMap._

  def process = {
    updateThreadRecipients
    if (ige.freshThread) sendFreshThreadEmail
    else forwardEmail
    ige.email.move(IGE.ARCHIVE_FOLDER)
    GroupMap.serialize
  }

  def updateThreadRecipients: Unit = {
    GroupMap.addHashRecipient(ige.groupHash, ige.emailRecipients)
    logger.debug(s"Updating group ${ ige.groupHash } recipients: ${ groupRecipients(ige.groupHash).mkString(", ") }")
    logger.debug(s"Threads:\n${ GroupMap.hashToRecipientsMapString }")
  }

  private def sendFreshThreadEmail = {
    val eo = EmailOut(IGE.smtp)
    eo.subjectRaw = s"Instant group created - ${ ige.groupHash }"
    eo.text = Some(
      s"""Instant group created - ${ ige.groupHash }
          |every email sent to ${ IGE.groupAddress(ige.groupHash) }
          |will be delivered to ${ groupRecipients(ige.groupHash).mkString(", ") }
      """.stripMargin)
    eo.senderAddress = IGE.groupAddress(ige.groupHash)
    eo.replyTo = Seq(ige.groupAddress)
    eo.recipients = Seq(ige.email.sender)
    eo.send
  }

  private def forwardEmail: Unit = try {
    val eo = EmailOut(IGE.smtp)
    eo.subjectRaw = ige.email.subjectRaw
    logger.debug(s"Forwarding email ${ eo.subjectRaw } to ${ groupRecipients(ige.groupHash).mkString(", ") }")
    eo.text = Some(
      s"""${ ige.email.text.getOrElse("NO CONTENT") }
         |
         |---
         |Instant group - ${ ige.groupHash }
         |every email sent to ${ ige.groupAddress }
         |will be delivered to ${ groupRecipients(ige.groupHash).mkString(", ") }
      """.stripMargin)
    eo.senderAddress = ige.groupAddress
    eo.replyTo = Seq(ige.groupAddress)
    eo.recipientsBCC = groupRecipients(ige.groupHash, Some(ige.sender)).toSeq
    eo.send
  } catch {
    case e: EmailException => logger.debug(s"Error: ${ e.getMessage }")
  }
}
