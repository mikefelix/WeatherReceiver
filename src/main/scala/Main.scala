import java.time.LocalDateTime

import actors.{CurrentWeatherActor, ForecastWeatherActor, HistoricalWeatherActor, Refresh}
import akka.actor.{ActorSystem, Props}
import choreography.Get
import com.twitter.util.Future
import services._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
  * RouteMappings
  * User: michael.felix
  * Date: 5/10/16
  */
object Main extends Runner {
  lazy val cache = new ResultsCache
  lazy val currentService = CurrentWeatherService
  lazy val forecast1Service = Forecast1WeatherService
  lazy val forecast2Service = Forecast2WeatherService
  lazy val historicalService = HistoricalWeatherService

  override def startActors() = {
    val system = ActorSystem("weatherSystem")

    val current = system.actorOf(Props(new CurrentWeatherActor(cache)), name = "currentActor")
    val forecast = system.actorOf(Props(new ForecastWeatherActor(cache)), name = "forecastActor")
    val history = system.actorOf(Props(new HistoricalWeatherActor(cache)), name = "historyActor")

    system.scheduler.schedule(1 second, 5 minutes) {
      current ! Refresh
    }

    system.scheduler.schedule(5 seconds, 15 minutes) {
      forecast ! Refresh
    }

    system.scheduler.schedule(1 seconds, 24 hours) {
      history ! Refresh
    }
  }

  implicit def toFuture[A](a: A): Future[A] = Future value a

  def day = LocalDateTime.now.getDayOfMonth
  def month = LocalDateTime.now.getMonthValue

  def routeMappings = {
    case Get("current") => currentService.serializeOutput(cache.get("current")).toString
    case Get("forecast1") => forecast1Service.serializeOutput(cache.get("forecast1")).toString
    case Get("forecast2") => forecast2Service.serializeOutput(cache.get("forecast2")).toString
    case Get("historical") => historicalService.serializeOutput(cache.get(s"historical")).toString
    case Get("all") =>
      s"""
        |{
        |"current":${currentService.serializeOutput(cache.get("current")).toString},
        |"forecast1":${forecast1Service.serializeOutput(cache.get("forecast1")).toString},
        |"forecast2":${forecast1Service.serializeOutput(cache.get("forecast2")).toString},
        |"historical":${historicalService.serializeOutput(cache.get(s"historical")).toString},
        |}
      """.stripMargin
  }
}
