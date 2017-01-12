package services

import choreography.EnvVar
import model.input.Forecast
import model.output.ForecastInfo
import util.Attempt

abstract class ForecastWeatherService extends WeatherService[Forecast, ForecastInfo] {
  val host = EnvVar("FORECAST_HOST")
  val url = EnvVar("FORECAST_PATH")
  val token = EnvVar("FORECAST_TOKEN")
  val useTls = false

  protected def reformatForecast(forecast: Forecast, first: Boolean): Attempt[ForecastInfo] = {
    for {
      simple <- Attempt.notNull(forecast.forecast.simpleforecast).orFail("Missing: simpleforecast")
      textual <- Attempt.notNull(forecast.forecast.txt_forecast).orFail("Missing: text forecast")
      today <- Attempt.notNull(simple.forecastday(0)).orFail("Missing: today forecast")
      tomorrow <- Attempt.notNull(simple.forecastday(1)).orFail("Missing: tomorrow forecast")
      todayText <- Attempt.notNull(textual.forecastday(0)).orFail("Missing: today text forecast")
      tonightText <- Attempt.notNull(textual.forecastday(1)).orFail("Missing: tonight text forecast")
      tomorrowText <- Attempt.notNull(textual.forecastday(2)).orFail("Missing: tomorrow text forecast")
    } yield
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