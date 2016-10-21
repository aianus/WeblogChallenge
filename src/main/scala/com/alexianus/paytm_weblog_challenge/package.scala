package com.alexianus

import com.alexianus.aws_elb_log_parser.LogLine
import com.github.nscala_time.time.Imports._
import org.joda.time.DateTime
import com.alexianus.paytm_weblog_challenge.Session
import org.joda.time.format.PeriodFormatterBuilder

package object paytm_weblog_challenge {
  def sessionizeFoldOp(sessionTTL: Duration)(memo: List[Session], logline: LogLine) : List[Session] = {
    val previousTimestamp = memo.headOption.map(_.loglines.head.time).getOrElse(new DateTime(0))
    if (logline.time > previousTimestamp + sessionTTL) {
      // Create a new session (sublist) whenever a new logline
      // is past the session TTL
      new Session(List(logline)) :: memo
    } else {
      // Otherwise, add it onto the current session
      new Session(logline :: memo.head.loglines) :: memo.tail
    }
  }

  implicit class Sessionize(it: Iterable[LogLine]) {
    def sessionizeByTTL(sessionTTL: Duration): List[Session] = {
      it.toSeq.sortBy(_.time).foldLeft(List.empty[Session])(sessionizeFoldOp(sessionTTL))
    }
  }

  implicit class FormatDuration(duration: Duration) {
    val hm = new PeriodFormatterBuilder()
      .printZeroAlways()
      .minimumPrintedDigits(2) // gives the '01'
      .appendHours()
      .appendSeparator(":")
      .appendMinutes()
      .appendSeparator(":")
      .appendSeconds()
      .toFormatter();

    def formatted: String = {
      hm.print(duration.toPeriod)
    }
  }
}
