package services

class Forecast1WeatherService extends ForecastWeatherService {
  def getInfo = getForecast(first = true)
}