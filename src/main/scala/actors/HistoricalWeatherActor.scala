package actors

import java.time.LocalDateTime

import akka.actor._
import choreography.EnvVar
import model.input.HistoricalData
import model.output.History
import services._
import util.{Failed, Succeeded}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class HistoricalWeatherActor(cache: ResultsCache) extends Actor {
  val host = EnvVar("HISTORICAL_HOST")
  val url = EnvVar("HISTORICAL_PATH")
  val token = EnvVar("HISTORICAL_TOKEN")

  override def receive = {
    case Refresh => refresh()
    case RetrieveDay(day, month, year) =>
      retrieveDay(day, month, year)
      if (year < LocalDateTime.now.getYear)
        context.system.scheduler.scheduleOnce(100 milliseconds, self, RetrieveDay(day, month, year + 1))
      else
        self ! CompileHistory(day, month)
    case CompileHistory(day, month) => compile(day, month)
  }

  def refresh(): Unit = {
    val (day, month, year) = {
      val today = LocalDateTime.now
      (today.getDayOfMonth, today.getMonth.getValue, today.getYear)
    }

    self ! RetrieveDay(day, month, year - 50)
  }

  def retrieveDay(day: Int, month: Int, year: Int): Unit = {
    HistoricalWeatherService.getDay(day, month, year) match {
      case Failed(why) => println(s"Failed to retrieve reading|$day-$month-$year: $why")
      case Succeeded(value) => cache.put(s"reading|$day-$month-$year", value)
    }
  }

  def compile(day: Int, month: Int): Unit = {
    val responses = cache.getAll[HistoricalData](s"$day-$month.*".r)
    val history = History(responses)
    cache.put(s"history|$day-$month", history)
  }

}
