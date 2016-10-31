package actors

import java.time.LocalDateTime

import akka.actor._
import choreography.EnvVar
import services._

import scala.concurrent.Await
import scala.concurrent.duration._
import choreography.TwitterConverters._

import scala.util.{Failure, Success}

class HistoricalWeatherActor(cache: ResultsCache) extends Actor {
  val host = EnvVar("HISTORICAL_HOST")
  val url = EnvVar("HISTORICAL_PATH")
  val token = EnvVar("HISTORICAL_TOKEN")

  override def receive = {
    case Refresh => refresh()
    case RetrieveDay(month, day, year) => retrieveDay(month, day, year)
    case TransformDay(month, day, year) => transformDay(month, day, year)
    case CompileResults(month, day) => compile(month, day)
  }

  def refresh(): Unit = {
    val (month, day, year) = {
      val today = LocalDateTime.now
      (today.getMonth.getValue, today.getDayOfMonth, today.getYear)
    }

    self ! RetrieveDay(month, day, year - 1)
  }

  def retrieveDay(month: Int, day: Int, year: Int): Unit = {
    val response = HistoricalWeatherService.getYearUrl(year).map(_.contentString)
    val text = Await.result(response, 10 seconds)

    cache.put(s"reading|$month-$day-$year", transformed = false, ApiResponse(text))

    val newYear = year - 1
    if (newYear > year - 50)
      context.system.scheduler.scheduleOnce(100 milliseconds, self, RetrieveDay(month, day, newYear))
    else
      self ! CompileResults(month, day)
  }

  def transformDay(month: Int, day: Int, year: Int): Unit = {
    val day = cache(s"reading|$month-$day-$year")
    HistoricalWeatherService.transform(day) match {
      case Failure(exception) => exception.printStackTrace()
      case Success(res) =>
          cache.put(s"history|$month-$day-$year", transformed = true, res)
    }
  }

  def compile(month: Int, day: Int): Unit = {
    val responses = cache.getAll(s"$month-$day.*".r)
    HistoricalWeatherService.
  }

}

case class RetrieveDay(month: Int, day: Int, year: Int)
case class TransformDay(month: Int, day: Int, year: Int)
case class CompileResults(month: Int, day: Int)
