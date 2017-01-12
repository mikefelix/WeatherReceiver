package services

import choreography.EnvVar
import choreography.TwitterConverters._
import model.input.SingleHistoricalDatum
import model.output.History
import util.Attempt

import scala.concurrent.Await
import scala.concurrent.duration._

object HistoricalWeatherService extends WeatherService[SingleHistoricalDatum, History] {
  val host = EnvVar("HISTORICAL_HOST")
  val url = EnvVar("HISTORICAL_PATH")
  val token = EnvVar("HISTORICAL_TOKEN")
  val useTls = true

  def transformInput(hist: SingleHistoricalDatum) = throw new UnsupportedOperationException

  def getDay(day: Int, month: Int, year: Int): Attempt[SingleHistoricalDatum] = {
    val u = url
          .replaceAll("YEAR", year.toString)
          .replaceAll("MONTH", month.toString.padTo(2, '0'))
          .replaceAll("DAY", day.toString.padTo(2, '0'))

    val future = client.get(u, "token" -> token)
    val tried = Attempt { Await.result(future, 10 seconds) }

    for {
      res <- tried
      i <- consumeRemote(res)
    } yield i
  }

  override def serializeOutput(history: History): Attempt[String] = Attempt.orFail("Can't serialize history") {
    for {
      low <- history.low
      high <- history.high
      precip <- history.precip
    } yield s"""{"low": $low,"high": $high,"precip": $precip}"""
  }

}