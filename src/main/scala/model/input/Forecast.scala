package model.input

import model.WeatherResult

/**
  * ForecastInfo
  * User: michael.felix
  * Date: 11/26/15
  */
object Forecast {

  class Date {
    val month: Int = 0
    val day: Int = 0
    val hour: Int = 0
  }

  class Wind {
    val mph: String = null
    val dir: String = null
  }

  class Precip {
    val in: Float = 0.0f
  }

  class Temp {
    val fahrenheit: String = null
  }

  class SimpleForecastDay {
    val date: Forecast.Date = null
    val high: Forecast.Temp = null
    val low: Forecast.Temp = null
    val conditions: String = null
    val icon: String = null
    val qpf_allday: Forecast.Precip = null
    val snow_allday: Forecast.Precip = null
    val avewind: Forecast.Wind = null
    val maxwind: Forecast.Wind = null
    val avehumidity: Int = 0
    val maxhumidity: Int = 0
  }

  class TextForecastDay {
    val period: Int = 0
    val icon: String = null
    val title: String = null
    val fcttext: String = null
  }

  class SimpleForecast {
    val forecastday: Array[Forecast.SimpleForecastDay] = null
  }

  class TextForecast {
    val date: String = null
    val forecastday: Array[Forecast.TextForecastDay] = null
  }

  class FCast {
    val txt_forecast: Forecast.TextForecast = null
    val simpleforecast: Forecast.SimpleForecast = null
  }

}

class Forecast extends WeatherResult {
  var forecast: Forecast.FCast = _
}