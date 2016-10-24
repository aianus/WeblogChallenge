package com.alexianus.paytm_weblog_challenge

import com.alexianus.aws_elb_log_parser.LogLine
import org.joda.time.{Duration, Interval}

// Immutable Session class
// The loglines that make up the session are stored in the list in reverse chronological order
class Session(val loglines: List[LogLine]) {
  def duration: Duration = {
      if (durationCache == null) {
        durationCache = new Interval(loglines.last.time, loglines.head.time).toDuration
      }
      durationCache
  }
  private var durationCache: Duration = null
}
