package services

import model.input.Forecast

object Forecast1WeatherService extends ForecastWeatherService {
  override def transformInput(res: Forecast) = reformatForecast(res, first = true)
}