package com.alexianus.aws_elb_log_parser

import java.net.URI

import com.github.nscala_time.time.Imports._

import scala.util.Try
import scala.util.parsing.combinator._

case class Socket(ip: String,
                  port: Int)

case class HttpRequest(method: String,
                       uri: URI,
                       protocol: String)

case class LogLine(time: DateTime,
                   elb: String,
                   client: Socket,
                   backend: Option[Socket],
                   request_processing_time: Option[Double],
                   backend_processing_time: Option[Double],
                   response_processing_time: Option[Double],
                   elb_status_code: Option[Int],
                   backend_status_code: Option[Int],
                   received_bytes: Option[Int],
                   sent_bytes: Option[Int],
                   request: Option[HttpRequest],
                   user_agent: Option[String],
                   ssl_cipher: Option[String],
                   ssl_protocol: Option[String])

object LogLine {
  def parse(str: String): Option[LogLine] = Try {
      LogLineParser.parse(LogLineParser.log_line, str).get
    }.toOption
}

object LogLineParser extends RegexParsers {
  def quote_delimited_string: Parser[String] = "\"" ~> """[^"]*""".r <~ "\"" ^^ { _.toString }
  def space_delimited_string: Parser[String] = """\S+""".r ^^ { _.toString }
  def optional_space_delimited_string: Parser[Option[String]] = space_delimited_string ^^ {
    case "-" => None
    case str: String => Some(str)
  }
  def optional_int: Parser[Option[Int]] = space_delimited_string ^^ {
    case "-1" => None
    case "-" => None
    case str: String => Some(str.toInt)
  }
  def int: Parser[Int] = """[-+]?\d+""".r ^^ { _.toInt }
  def optional_double: Parser[Option[Double]] = space_delimited_string ^^ {
    case "-1" => None
    case "-" => None
    case str: String => Some(str.toDouble)
  }
  def ip_address: Parser[String] = """(?:[0-9]{1,3}\.){3}[0-9]{1,3}""".r ^^ { _.toString }
  def socket: Parser[Socket] = (ip_address <~ ":") ~ int ^^ {
    case ip_address ~ int => Socket(ip_address, int)
  }
  def timestamp: Parser[DateTime] = space_delimited_string ^^ { s => DateTime.parse(s) }
  def elb: Parser[String] = space_delimited_string
  def client: Parser[Socket] = socket
  def backend: Parser[Option[Socket]] = ("-" | socket) ^^ {
    case "-" => None
    case s: Socket => Some(s)
  }
  def request_processing_time: Parser[Option[Double]] = optional_double
  def backend_processing_time: Parser[Option[Double]] = optional_double
  def response_processing_time: Parser[Option[Double]] = optional_double
  def elb_status_code: Parser[Option[Int]] = optional_int
  def backend_status_code: Parser[Option[Int]] = optional_int
  def received_bytes: Parser[Option[Int]] = optional_int
  def sent_bytes: Parser[Option[Int]] = optional_int
  def request: Parser[Option[HttpRequest]] = quote_delimited_string ^^ {
    case "- - - " => None
    case s => {
      val sections = s.split(" ")
      Some(HttpRequest(sections(0), URI.create(sections(1)), sections(2)))
    }
  }
  def user_agent: Parser[Option[String]] = quote_delimited_string ^^ {
    case "-" => None
    case "" => None
    case s => Some(s)
  }
  def ssl_cipher: Parser[Option[String]] = optional_space_delimited_string
  def ssl_protocol: Parser[Option[String]] = optional_space_delimited_string
  def log_line: Parser[LogLine] = (timestamp ~ elb ~ client ~ backend ~ request_processing_time ~ backend_processing_time ~ response_processing_time ~ elb_status_code ~ backend_status_code ~ received_bytes ~ sent_bytes ~ request ~ user_agent ~ ssl_cipher ~ ssl_protocol) ^^ {
    case timestamp ~ elb ~ client ~ backend ~ request_processing_time ~ backend_processing_time ~ response_processing_time ~ elb_status_code ~ backend_status_code ~ received_bytes ~ sent_bytes ~ request ~ user_agent ~ ssl_cipher ~ ssl_protocol => LogLine(
        timestamp, elb, client, backend, request_processing_time, backend_processing_time, response_processing_time, elb_status_code, backend_status_code, received_bytes, sent_bytes, request, user_agent, ssl_cipher, ssl_protocol
    )
  }
}

