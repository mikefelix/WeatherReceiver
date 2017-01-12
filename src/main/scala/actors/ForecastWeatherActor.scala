package actors

import akka.actor._
import model.input.Forecast
import model.output.ForecastInfo
import services._
import util.{Attempt, Failed, Succeeded}

class ForecastWeatherActor(cache: ResultsCache) extends Actor {
  override def receive = {

    case Refresh =>
      Forecast1WeatherService.getInput match {
        case Failed(why) => println(s"Failed to refresh forecasts: $why")
        case Succeeded(value) =>
          cache.put("forecast_input", value)
          self ! ReformatForecast(true)
          self ! ReformatForecast(false)
      }

    case ReformatForecast(first) =>
      val which = if (first) "1" else "2"
      val value = cache.get[Forecast]("forecast_input")
      val service: ForecastWeatherService = if (first) Forecast1WeatherService else Forecast2WeatherService
      val input: Attempt[ForecastInfo] = service.transformInput(value)

      input match {
        case Succeeded(info) =>
          cache.put(s"forecast$which", info)
        case Failed(why) =>
          val old = cache.get[ForecastInfo](s"forecast$which")
          cache.put(s"forecast$which", old.copy(cond = old.cond + s" (Refresh failed: $why)"))
      }
  }
}

