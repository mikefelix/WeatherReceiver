package model

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

trait WeatherResult {
  val recordedAt = LocalDateTime.now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
}
