package com.alexianus.aws_elb_log_parser

import com.github.nscala_time.time.Imports._

class LogLineParserSpec extends UnitSpec {
    describe("LogLineParserSpec") {
      // https://docs.aws.amazon.com/elasticloadbalancing/latest/classic/access-log-collection.html#access-log-entry-format
      it("should parse an HTTP entry") {
        val http_entry = "2015-05-13T23:39:43.945958Z my-loadbalancer 192.168.131.39:2817 10.0.0.1:80 0.000073 0.001048 0.000057 200 200 0 29 \"GET http://www.example.com:80/example_path/ HTTP/1.1\" \"curl/7.38.0\" - -"
        val result = LogLineParser.parse(LogLineParser.log_line, http_entry)
        result shouldBe 'successful

        val log_line = result.get
        log_line.time shouldBe DateTime.parse("2015-05-13T23:39:43.945958Z")
        log_line.elb shouldBe "my-loadbalancer"
        log_line.client shouldBe Socket("192.168.131.39", 2817)
        log_line.backend shouldBe Some(Socket("10.0.0.1", 80))
        log_line.request_processing_time.get shouldBe (0.000073 +- 0.000001)
        log_line.backend_processing_time.get shouldBe (0.001048 +- 0.000001)
        log_line.response_processing_time.get shouldBe (0.000057 +- 0.000001)
        log_line.elb_status_code shouldBe Some(200)
        log_line.backend_status_code shouldBe Some(200)
        log_line.received_bytes shouldBe Some(0)
        log_line.sent_bytes shouldBe Some(29)

        val request = log_line.request.get
        request.method shouldBe "GET"
        request.protocol shouldBe "HTTP/1.1"
        request.uri.getPath shouldBe "/example_path/"

        log_line.user_agent shouldBe Some("curl/7.38.0")
        log_line.ssl_cipher shouldBe None
        log_line.ssl_protocol shouldBe None
      }

      it("should parse an HTTPS entry") {
        val https_entry = "2015-05-13T23:39:43.945958Z my-loadbalancer 192.168.131.39:2817 10.0.0.1:80 0.000086 0.001048 0.001337 200 200 0 57 \"GET https://www.example.com:443/ HTTP/1.1\" \"curl/7.38.0\" DHE-RSA-AES128-SHA TLSv1.2"
        val result = LogLineParser.parse(LogLineParser.log_line, https_entry)
        result shouldBe 'successful

        val log_line = result.get
        log_line.ssl_cipher shouldBe Some("DHE-RSA-AES128-SHA")
        log_line.ssl_protocol shouldBe Some("TLSv1.2")
      }

      it("should parse a TCP entry") {
        val https_entry = "2015-05-13T23:39:43.945958Z my-loadbalancer 192.168.131.39:2817 10.0.0.1:80 0.001069 0.000028 0.000041 - - 82 305 \"- - - \" \"-\" - -"
        val result = LogLineParser.parse(LogLineParser.log_line, https_entry)
        result shouldBe 'successful

        val log_line = result.get
        log_line.backend_status_code shouldBe None
        log_line.elb_status_code shouldBe None
        log_line.request shouldBe None
        log_line.user_agent shouldBe None
        log_line.ssl_cipher shouldBe None
        log_line.ssl_protocol shouldBe None
      }
    }
}
