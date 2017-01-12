package model.input

import java.time.LocalDateTime

import model.WeatherResult

/**
  * Conditions
  * User: michael.felix
  * Date: 10/21/16
  */
class Conditions extends WeatherResult {
  val current_observation: CurrentObservation = null

  val isNight = {
    val hour = LocalDateTime.now.getHour
    hour < 7 || hour > 19
  }

  class CurrentObservation {
    val icon: String = null
    val weather: String = null
    val temp_f: String = null
    val feelslike_f: String = null
    val relative_humidity: String = null
    val wind_string: String = null
    val wind_dir: String = null
    val precip_today_in: String = null
  }
}
