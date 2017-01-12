package services

import model.input.Forecast

object Forecast2WeatherService extends ForecastWeatherService {
  override def transformInput(res: Forecast) = reformatForecast(res, first = false)
}