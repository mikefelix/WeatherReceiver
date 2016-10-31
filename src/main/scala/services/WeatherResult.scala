package services

import java.time.LocalDateTime

trait WeatherResult {
  val time: LocalDateTime
  val result: String
}

object ApiResponse {
  def apply(text: String) = ApiResponse(LocalDateTime.now, text)
}

case class ApiResponse(time: LocalDateTime, result: String) extends WeatherResult

object Reformatting{
  def apply(text: String) = Reformatting(LocalDateTime.now, text)
}

case class Reformatting(time: LocalDateTime, result: String) extends WeatherResult
