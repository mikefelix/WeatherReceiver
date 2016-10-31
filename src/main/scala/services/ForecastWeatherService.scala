package services

import choreography.EnvVar

abstract class ForecastWeatherService extends WeatherService {
  val host = EnvVar("FORECAST_HOST")
  val url = EnvVar("FORECAST_PATH")
  val token = EnvVar("FORECAST_TOKEN")
  val cacheKey = "forecast"

  protected def reformatForecast(forecast: Forecast, first: Boolean): ForecastInfo = {
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