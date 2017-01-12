package services

import choreography.EnvVar
import model.input.Conditions
import model.output.CurrentInfo
import util.Attempt

object CurrentWeatherService extends WeatherService[Conditions, CurrentInfo] {
  val host = EnvVar("CURRENT_HOST")
  val token = EnvVar("CURRENT_TOKEN")
  val url = EnvVar("CURRENT_PATH")
  val useTls = false

  override def transformInput(cond: Conditions) = Attempt.success(new CurrentInfo(cond))

}