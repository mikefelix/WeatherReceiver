package model

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
  * WeatherInfo
  * User: michael.felix
  * Date: 10/31/16
  */
trait WeatherInfo extends WeatherResult {
  val cond: String
  val temp: Int
  val hum: Int
  def night: Boolean

  def displayTemp = temp + (if (temp >= 100) "°" else "°F")

  def getIcon = if (night)
      "nt_" + cond
    else
      cond

  val humStr = if (hum > 0) hum + "%" else ""

}
