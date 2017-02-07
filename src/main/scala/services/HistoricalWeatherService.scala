package services

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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

  def pad(s: String) = if (s.length == 1) "0" + s else s

  def getDay(day: Int, month: Int, year: Int): Attempt[SingleHistoricalDatum] = {
    val u = url
          .replaceAll("YEAR", year.toString)
          .replaceAll("MONTH", pad(month.toString))
          .replaceAll("DAY", pad(day.toString))

    val future = client.get(u, "token" -> token)
    val tried = Attempt { Await.result(future, 10 seconds) }

    for {
      res <- tried
      i <- consumeRemote(res)
    } yield i
  }

  override def serializeOutput(history: History): Attempt[String] = Attempt.orFail("Can't serialize history") {
    for {
      avgLow <- history.avgLow
      avgHigh <- history.avgHigh
      minLow <- history.minLow
      minHigh <- history.minHigh
      maxLow <- history.maxLow
      maxHigh <- history.maxHigh
      precip <- history.precip
    } yield
      s"""{
         |"low": $avgLow,
         |"maxLow": $maxLow,
         |"minLow": $minLow,
         |"maxHigh": $maxHigh,
         |"minHigh": $minHigh,
         |"high": $avgHigh,
         |"precip": $precip,
         |"recordedAt": "${history.recordedAt}"
         |}""".stripMargin
  }


}