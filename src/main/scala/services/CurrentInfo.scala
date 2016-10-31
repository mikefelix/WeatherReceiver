package services

/**
 * WeatherInfo
 * User: michael.felix
 * Date: 11/26/15
 */
case class CurrentInfo(cond: String, temp: Int, hum: Int, night: Boolean) extends WeatherInfo {
  def this(c: Conditions) = {
    this(c.current_observation.icon,
      java.lang.Float.parseFloat(c.current_observation.temp_f).round,
      c.current_observation.relative_humidity.replaceAll("[^0-9.]", "").toInt,
      c.isNight)
  }
}

trait WeatherInfo {
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