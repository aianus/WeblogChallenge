package com.alexianus.paytm_weblog_challenge

import com.alexianus.aws_elb_log_parser.LogLine
import com.github.nscala_time.time.Imports._

class SessionizeSpec extends UnitSpec {
  describe("SessionizeSpec") {
    it("should group items into the same session when the difference in time is less than the TTL") {
      val http_entry1 = "2015-05-13T23:39:43.945958Z my-loadbalancer 192.168.131.39:2817 10.0.0.1:80 0.000073 0.001048 0.000057 200 200 0 29 \"GET http://www.example.com:80/example_path/ HTTP/1.1\" \"curl/7.38.0\" - -"
      val http_entry2 = "2015-05-13T23:43:43.945958Z my-loadbalancer 192.168.131.39:2817 10.0.0.1:80 0.000073 0.001048 0.000057 200 200 0 29 \"GET http://www.example.com:80/example_path/ HTTP/1.1\" \"curl/7.38.0\" - -"
      val log_lines = Array(http_entry1, http_entry2).flatMap(entry => LogLine.parse(entry))
      log_lines.length shouldBe 2

      val sessions = log_lines.toIterable.sessionizeByTTL(15.minutes)

      sessions.length shouldBe 1
      sessions.head.loglines.head.time shouldBe DateTime.parse("2015-05-13T23:43:43.945958Z")
      sessions.head.loglines.last.time shouldBe DateTime.parse("2015-05-13T23:39:43.945958Z")
    }

    it("should group items into different sessions when the difference in time is greater than the TTL") {
      val http_entry1 = "2015-05-13T23:39:43.945958Z my-loadbalancer 192.168.131.39:2817 10.0.0.1:80 0.000073 0.001048 0.000057 200 200 0 29 \"GET http://www.example.com:80/example_path/ HTTP/1.1\" \"curl/7.38.0\" - -"
      val http_entry2 = "2015-05-13T23:43:43.945958Z my-loadbalancer 192.168.131.39:2817 10.0.0.1:80 0.000073 0.001048 0.000057 200 200 0 29 \"GET http://www.example.com:80/example_path/ HTTP/1.1\" \"curl/7.38.0\" - -"
      val log_lines = Array(http_entry1, http_entry2).flatMap(entry => LogLine.parse(entry))
      log_lines.length shouldBe 2

      val sessions = log_lines.toIterable.sessionizeByTTL(2.minutes)

      sessions.length shouldBe 2
      sessions.head.loglines.head.time shouldBe DateTime.parse("2015-05-13T23:43:43.945958Z")
      sessions.last.loglines.head.time shouldBe DateTime.parse("2015-05-13T23:39:43.945958Z")
    }
  }
}
