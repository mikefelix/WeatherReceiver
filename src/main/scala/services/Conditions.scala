package services

import java.time.LocalDateTime

/**
  * Conditions
  * User: michael.felix
  * Date: 10/21/16
  */
class Conditions {
  private[services] val current_observation: CurrentObservation = null

  val now = LocalDateTime.now
  val hour = now.getHour
  val isNight = hour < 7 || hour > 19

  class CurrentObservation {
    private[services] val icon: String = null
    private[services] val weather: String = null
    private[services] val temp_f: String = null
    private[services] val feelslike_f: String = null
    private[services] val relative_humidity: String = null
    private[services] val wind_string: String = null
    private[services] val wind_dir: String = null
    private[services] val precip_today_in: String = null
  }
}
