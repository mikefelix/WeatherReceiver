package services

import com.twitter.util.Future

abstract class ForecastWeatherService extends WeatherService {
  val host = "api.wunderground.com"
  val url = "/api/4ff057a10c9613a4/forecast/q/UT/Murray.json"

  protected def getForecast(first: Boolean): Future[String] = {
    client.get(url) map (_.contentString) map { text =>

      val tryTransform = for {
        fore <- deserialize[Forecast](text)
        ref = reformatForecast(fore, first)
        ser <- serialize(ref)
      } yield ser

      tryTransform getOrElse "Could not retrieve forecast."
    }
  }

  private def reformatForecast(forecast: Forecast, first: Boolean): ForecastInfo = {
    val simple = forecast.forecast.simpleforecast
    val textual = forecast.forecast.txt_forecast
    val today = simple.forecastday(0)
    val tomorrow = simple.forecastday(1)
    val todayText = textual.forecastday(0)
    val tonightText = textual.forecastday(1)
    val tomorrowText = textual.forecastday(2)

    if (hour < 16) {
      if (first)
        new ForecastInfo(todayText.title, today.high.fahrenheit.toInt, today.avehumidity, todayText.icon, todayText.fcttext)
      else
        new ForecastInfo(tonightText.title, tomorrow.low.fahrenheit.toInt, today.avehumidity, tonightText.icon, tonightText.fcttext)
    }
    else {
      if (first)
        new ForecastInfo(tonightText.title, today.low.fahrenheit.toInt, today.avehumidity, tonightText.icon, tonightText.fcttext)
      else
        new ForecastInfo(tomorrowText.title, tomorrow.high.fahrenheit.toInt, tomorrow.avehumidity, tomorrowText.icon, tomorrowText.fcttext)
    }
  }

}