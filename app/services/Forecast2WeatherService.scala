package services

class Forecast2WeatherService extends ForecastWeatherService {
  def getInfo = getForecast(first = false)
}