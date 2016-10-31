package services

import choreography.EnvVar

import scala.util.Try

object HistoricalWeatherService extends WeatherService {
  val host = EnvVar("HISTORICAL_HOST")
  val url = EnvVar("HISTORICAL_PATH")
  val token = EnvVar("HISTORICAL_TOKEN")

  def getYearUrl(year: Int) = {
    val (month, day, year) = {
      val today = now
      (today.getMonth.getValue, today.getDayOfMonth, today.getYear)
    }

    def yearUrl(year: Int) = url
      .replaceAll("YEAR", year.toString)
      .replaceAll("MONTH", month.toString)
      .replaceAll("DAY", day.toString)

    client.get(yearUrl(year), "token" -> token)
  }

  def transform(apiResult: ApiResponse) = for {
    hist <- deserialize[HistoricalData](apiResult.result)
    low <- hist.low
    high <- hist.high
    precip <- hist.precip
  } yield History(low, high, precip)

  def average(seq: Seq[Float]) = if (seq.isEmpty)
    Float.NaN
  else
    seq.sum / seq.size

  override def reformat(responses: Set[ApiResponse]) = Try(
    s"""
       |{
       | "low": ${responses.average(s => s _.low)}
       | "high": ${average(highs)}
       | "precip": ${average(precips)}
       |}
      """.stripMargin
  )

  implicit class AverageFloat(set: Set[ApiResponse]){
    def average(func: ApiResponse => Float) =
      if (set.isEmpty)
        Float.NaN
      else {
        set.map(func(_)).sum / set.size
      }
  }
}