package services

class Forecast2WeatherService extends ForecastWeatherService {
  override def reformat(forecastText: String) = {
    deserialize[Forecast](forecastText)
      .map(reformatForecast(_, first = false))
      .flatMap(serialize)
  }
}