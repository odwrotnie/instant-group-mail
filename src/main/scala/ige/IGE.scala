package ige

import com.typesafe.scalalogging.LazyLogging
import commons.email._

object IGE
  extends LazyLogging {

  val ADDRESS = "instantgroupmail@gmail.com"
  val ARCHIVE_FOLDER = "Archive"
  val EMAIL_REGEX = """([a-zA-Z0-9_\.\-\+])+\@(([a-zA-Z0-9\-])+\.)+([a-zA-Z0-9]{2,4})+""".r
  val WITH_OR_WITHOUT_HASH_PATTERN = """instantgroupmail(\+(\w+))?@gmail.com"""
  val WITHOUTH_HASH_PATTERN = """instantgroupmail@gmail.com"""
  val WITH_HASH_PATTERN = """instantgroupmail\+(\w+)@gmail.com"""
  val WITH_HASH_REGEX = WITH_HASH_PATTERN.r
  val RECIPIENTS_FILE = "recipients.map"
  def groupAddress(hash: String) = s"instantgroupmail+$hash@gmail.com"

  val smtp = SMTPServer("smtp.gmail.com", ADDRESS, "InstantGM")
  val inbox = IMAPServer("imap.gmail.com", ADDRESS, "InstantGM", "Inbox")
  val archive = IMAPServer("imap.gmail.com", ADDRESS, "InstantGM", ARCHIVE_FOLDER)

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
