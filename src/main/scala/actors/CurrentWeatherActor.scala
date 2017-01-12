package actors

import akka.actor._
import model.input.Conditions
import model.output.CurrentInfo
import services.{CurrentWeatherService, ResultsCache}
import util.{Failed, Succeeded}

import scala.util.{Failure, Success}

class CurrentWeatherActor(cache: ResultsCache) extends Actor {
  override def receive = {

    case Refresh =>
      CurrentWeatherService.getInput match {
        case Failed(why) => println(s"Failed to refresh current: $why")
        case Succeeded(value) =>
          cache.put("current_input", value)
          self ! ReformatCurrent
      }

    case ReformatCurrent =>
      val value = cache.get[Conditions]("current_input")
      CurrentWeatherService.transformInput(value) match {
        case Succeeded(currentInfo) =>
          cache.put("current", currentInfo)
        case Failed(why) =>
          val old = cache.get[CurrentInfo]("current")
          cache.put("current", old.copy(cond = old.cond + s" (Refresh failed: $why)"))
      }
  }
}

