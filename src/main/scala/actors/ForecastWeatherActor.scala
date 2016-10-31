package actors

import akka.actor._
import services.{ApiResponse, CurrentWeatherService, Forecast1WeatherService, ResultsCache}

import scala.util.{Failure, Success}

class ForecastWeatherActor(cache: ResultsCache) extends Actor {
  override def receive = {

    case Refresh =>
      Forecast1WeatherService.remoteResult match {
        case Failure(exception) =>
          exception.printStackTrace()
        case Success(value) =>
          cache.put("forecast", transformed = false, value)
          self ! ReformatForecast(true)
          self ! ReformatForecast(false)
      }

    case ReformatForecast(first) =>
      val value = cache("forecast", transformed = false)
      val reformatted = CurrentWeatherService.reformat(value.asInstanceOf[ApiResponse])

      // TODO: handle failures
      val which = if (first) "1" else "2"
      cache.put(s"forecast$which", transformed = true, reformatted.get)

  }
}

case class ReformatForecast(first: Boolean)