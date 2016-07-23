package ige

import com.typesafe.scalalogging.LazyLogging
import commons.email._

object IGE
  extends LazyLogging {

  val LOGIN = "instantgroupmail"
  val DOMAIN = "gmail.com"
  val INBOX_FOLDER = "Inbox"
  val ARCHIVE_FOLDER = "Archive"
  val RECIPIENTS_FILE = "recipients.map"

  val ADDRESS = s"$LOGIN@$DOMAIN"
  val WITH_OR_WITHOUT_HASH_PATTERN = """instantgroupmail(\+(\w+))?@gmail.com""" //s"""$LOGIN(\+(\w+))?@$DOMAIN"""
  val WITH_HASH_PATTERN = """instantgroupmail\+(\w+)@gmail.com"""
  val WITH_HASH_REGEX = WITH_HASH_PATTERN.r

  def groupAddress(hash: String) = s"instantgroupmail+$hash@gmail.com"

  lazy val smtp = SMTPServer("smtp.gmail.com", ADDRESS, "InstantGM")
  lazy val inbox = IMAPServer("imap.gmail.com", ADDRESS, "InstantGM", INBOX_FOLDER)
  lazy val archive = IMAPServer("imap.gmail.com", ADDRESS, "InstantGM", ARCHIVE_FOLDER)

  GroupMap.deserialize

  def processInbox: Unit = {
    inbox.messages foreach { message =>
      println(s" > $message")
      val ige = GroupEmail(message)
      val p = GroupEmailProcessor(ige)
      p.process
    }
  }

  def start: Unit = while(true) {
    processInbox
    Thread.sleep(1000)
  }
}
