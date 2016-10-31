import actors.Refresh
import akka.actor.{ActorSystem, Props}
import choreography.Get
import com.twitter.util.Future
import services._

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * RouteMappings
  * User: michael.felix
  * Date: 5/10/16
  */
object Main extends Runner {
  lazy val cache = new ResultsCache
  lazy val currentService = new CurrentWeatherService
  lazy val forecast1Service = new Forecast1WeatherService
  lazy val forecast2Service = new Forecast2WeatherService
  lazy val historicalService = new HistoricalWeatherService

  override def startActors() = {
    val system = ActorSystem("weatherSystem")

    val current = system.actorOf(Props(new CurrentWeatherActor(cache, currentService)), name = "currentActor")

    system.scheduler.schedule(1 second, 5 minutes) {
      current ! Refresh
    }
/*

    system.scheduler.schedule(5 seconds, 5 minutes) {
      cache.put("forecast", Await.result(forecast1Service.info, 20 seconds))
    }

    system.scheduler.schedule(1 seconds, 24 hours) {
      cache.put("historical", Await.result(historicalService.info, 60 seconds))
    }
*/
  }

  implicit def toFuture[A](a: A): Future[A] = Future value a

  def routeMappings = {
    case Get("current") => cache("current").result
    case Get("forecast1") => cache("forecast1").result
    case Get("forecast2") => cache("forecast2").result
    case Get("historical") => cache("historical").result
  }
}
