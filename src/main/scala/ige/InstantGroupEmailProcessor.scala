package ige

import com.typesafe.scalalogging.LazyLogging
import commons.email.EmailOut
import org.apache.commons.mail.EmailException

case class InstantGroupEmailProcessor(ige: InstantGroupEmail)
  extends LazyLogging {

  import InstantGroupEmail._

  def process = {
    updateThreadRecipients
    if (ige.freshThread) sendFreshThreadEmail
    else forwardEmail
    ige.email.move(IGE.ARCHIVE_FOLDER)
    InstantGroupEmail.serialize
  }

  def updateThreadRecipients: Unit = {
    InstantGroupEmail.addHashRecipient(ige.threadHash, ige.emailRecipients)
    logger.debug(s"Updating thread ${ ige.threadHash } recipients: ${ threadRecipients(ige.threadHash).mkString(", ") }")
    logger.debug(s"Threads:\n${ InstantGroupEmail.hashToRecipientsMapString }")
  }

  private def sendFreshThreadEmail = {
    val eo = EmailOut(IGE.smtp)
    eo.subjectRaw = s"Instant group created - ${ ige.threadHash }"
    eo.text = Some(
      s"""Instant group created - ${ ige.threadHash }
          |every email sent to ${ IGE.threadAddress(ige.threadHash) }
          |will be delivered to ${ threadRecipients(ige.threadHash).mkString(", ") }
      """.stripMargin)
    eo.senderAddress = IGE.threadAddress(ige.threadHash)
    eo.replyTo = Seq(ige.threadAddress)
    eo.recipients = Seq(ige.email.sender)
    eo.send
  }

  private def forwardEmail: Unit = try {
    val eo = EmailOut(IGE.smtp)
    eo.subjectRaw = ige.email.subjectRaw
    logger.debug(s"Forwarding email ${ eo.subjectRaw } to ${ threadRecipients(ige.threadHash).mkString(", ") }")
    eo.text = Some(
      s"""${ ige.email.text.getOrElse("NO CONTENT") }
         |
         |---
         |Instant group - ${ ige.threadHash }
         |every email sent to ${ ige.threadAddress }
         |will be delivered to ${ threadRecipients(ige.threadHash).mkString(", ") }
      """.stripMargin)
    eo.senderAddress = ige.threadAddress
    eo.replyTo = Seq(ige.threadAddress)
    eo.recipientsBCC = threadRecipients(ige.threadHash, Some(ige.sender)).toSeq
    eo.send
  } catch {
    case e: EmailException => logger.debug(s"Error: ${ e.getMessage }")
  }
}
