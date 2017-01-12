package model.output

import model.WeatherInfo
import model.input.Conditions

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

