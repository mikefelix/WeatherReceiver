package services

import com.twitter.util.Future

class HistoricalWeatherService extends WeatherService {
  val host = "www.ncdc.noaa.gov"
  val url = "/cdo-web/api/v2/data?datasetid=GHCND&stationid=GHCND:USC00421759" +
    "&datatypeid=TMIN&datatypeid=TMAX&datatypeid=PRCP" +
    "&limit=100&units=standard" +
    "&startdate=$startyear-$startmonth-$startday&enddate=$endyear-$endmonth-$endday"

  val NOAA_TOKEN = "LHjOaHFgSNaaRzeYcTgFQzClofYkaspD"

  def getInfo: Future[String] = {
    val (month, day, year) = {
      val today = now
      (today.getMonth.getValue, today.getDayOfMonth, today.getYear)
    }

    def rurl(year: Int) = url
             .replaceAll("\\$startyear", year.toString)
             .replaceAll("\\$(start|end)month", month.toString)
             .replaceAll("\\$(start|end)day", day.toString)

    val responseFutures = ((year - 50) until year).map(y => client.get(rurl(y), "token" -> NOAA_TOKEN))

    Future collect responseFutures map { responses =>

      val data = for {
        resp <- responses
        tried = deserialize[HistoricalData](resp.contentString)
        opt <- tried.toOption
      } yield opt

      def collectMeasurement(getFloat: HistoricalData => Option[Float]) = for {
        d <- data
        meas <- getFloat(d)
      } yield meas

      val lows = collectMeasurement(_.low)
      val highs = collectMeasurement(_.high)
      val precips = collectMeasurement(_.precip)

      def average(seq: Seq[Float]) = if (seq.isEmpty)
        Float.NaN
      else
        seq.sum / seq.size

      s"""
        |{
        | "low": ${average(lows)}
        | "high": ${average(highs)}
        | "precip": ${average(precips)}
        |}
      """.stripMargin
    }
  }
}