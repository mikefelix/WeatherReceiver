package actors

import akka.actor._
import services.{ApiResponse, CurrentWeatherService, ResultsCache}

import scala.util.{Failure, Success}

class CurrentWeatherActor(cache: ResultsCache) extends Actor {
  override def receive = {

    case Refresh =>
      CurrentWeatherService.remoteResult match {
        case Failure(exception) =>
          exception.printStackTrace()
        case Success(value) =>
          cache.put("current", transformed = false, value)
          self ! ReformatCurrent
      }

    case ReformatCurrent =>
      val value = cache("current", transformed = false)
      val reformatted = CurrentWeatherService.reformat(value.asInstanceOf[ApiResponse])

      // TODO: handle failures
      cache.put("current", transformed = true, reformatted.get)

  }
}

case object Refresh
case object ReformatCurrent
