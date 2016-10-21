package com.alexianus.paytm_weblog_challenge

import com.alexianus.aws_elb_log_parser.LogLine
import com.github.nscala_time.time.Imports._
import org.apache.spark.{SparkConf, SparkContext}

object Solution {
  def main(args: Array[String]) {
    val conf = new SparkConf()
      .setAppName("PaytmWeblogChallengeSolution")
    val sc = new SparkContext(conf)
    val input_path = java.net.URLDecoder.decode(args(0), "UTF-8")

    val raw_weblogs = sc.textFile(input_path)
    System.out.println(s"Total number of raw log lines: ${raw_weblogs.count}")

    // Parse all the log lines, discarding invalid lines
    val parsed_weblogs = raw_weblogs.flatMap(line => LogLine.parse(line))
    System.out.println(s"Total number of parsed log lines: ${parsed_weblogs.count}")

    // Group the requests by client
    val weblogs_by_client = parsed_weblogs.groupBy(_.client.ip)

    // 1. Sessionize
    val sessions_by_client = weblogs_by_client.mapValues { loglines_per_client =>
      loglines_per_client.sessionizeByTTL(15.minutes)
    }
    sessions_by_client.persist

    // Ungroup, parts 2-3 don't depend on the grouping
    val sessions = sessions_by_client.values.flatMap(identity[List[Session]])
    System.out.println(s"Total number of sessions: ${sessions.count}")

    // 2. Calculate the average session duration
    val average_session_duration = Duration.millis(
      sessions.map(_.duration.getMillis).mean.toLong
    )
    System.out.println(s"2. Average session time: ${average_session_duration.formatted}")

    // 3. Calculate the average number of distinct URIs visited per session
    val distinct_uris_per_session = sessions.map { session =>
      session.loglines.flatMap(_.request.map(_.uri)).distinct.length
    }
    val average_distinct_uris_per_session = distinct_uris_per_session.mean
    System.out.println(s"3. Average distinct uris per session: $average_distinct_uris_per_session")

    // 4. Find the clients with the longest session times
    val client_max_session_durations = sessions_by_client.mapValues(_.max(Session.orderingByDuration.reverse).duration)
    val top_10_engaged_clients = client_max_session_durations.sortBy(_._2, false).take(10)
    System.out.println("4. Most engaged clients:")
    top_10_engaged_clients.foreach { case (ip, duration) =>
      System.out.println(s"$ip ${duration.formatted}")
    }
  }
}
