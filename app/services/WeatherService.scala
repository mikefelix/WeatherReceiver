package services

import java.io.{BufferedReader, IOException, InputStreamReader}
import java.net.{MalformedURLException, SocketTimeoutException, URL, URLConnection}
import java.util.Calendar

import com.google.gson.Gson

import scala.annotation.tailrec
import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}

/**
 * WeatherService
 * User: michael.felix
 * Date: 11/26/15
 */
object WeatherService {
  val CURR_URL = "http://api.wunderground.com/api/4ff057a10c9613a4/conditions/q/UT/Murray.json"
  val FORE_URL = "http://api.wunderground.com/api/4ff057a10c9613a4/forecast/q/UT/Murray.json"

  val gson = new Gson

  def getForecast1 = getForecast(true)

  def getForecast2 = getForecast(false)

  def getCurrentWeather: Try[String] = for {
      text <- readUrl(WeatherService.CURR_URL)
      conditions <- deserialize[Conditions](text)
      info = reformatConditions(conditions)
      output <- serialize(info)
    } yield output

  private def reformatConditions(conditions: Conditions) =
    new CurrentInfo(conditions.current_observation.icon,
        java.lang.Float.parseFloat(conditions.current_observation.temp_f).round,
        conditions.current_observation.relative_humidity.replaceAll("[^0-9.]", "").toInt,
        isNight(Calendar.getInstance.get(Calendar.HOUR_OF_DAY)))

  private def deserialize[A : ClassTag](text: String) = Try {
    gson.fromJson(text, implicitly[ClassTag[A]].runtimeClass.asInstanceOf[Class[A]])
  }

  private def serialize[A : ClassTag](obj: A) = Try {
    gson.toJson(obj, implicitly[ClassTag[A]].runtimeClass.asInstanceOf[Class[A]])
  }

  private def getForecast(first: Boolean) = for {
      text <- readUrl(WeatherService.FORE_URL)
      forecast <- deserialize[Forecast](text)
      info = reformatForecast(forecast, first)
      output <- serialize(info)
    } yield output

  private def reformatForecast(forecast: Forecast, first: Boolean): ForecastInfo = {
    val simple = forecast.forecast.simpleforecast
    val textual = forecast.forecast.txt_forecast
    val today = simple.forecastday(0)
    val tomorrow = simple.forecastday(1)
    val todayText = textual.forecastday(0)
    val tonightText = textual.forecastday(1)
    val tomorrowText = textual.forecastday(2)
    val hour = Calendar.getInstance.get(Calendar.HOUR_OF_DAY)

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

  private def isNight(hour: Int) = hour < 7 || hour > 19

  private def readUrl(urlStr: String): Try[String] = {
    try {
      val connection = new URL(urlStr).openConnection
      connection.setConnectTimeout(20000)
      getReader(connection) map readLines
    }
    catch {
      case mue: MalformedURLException => {
        Failure(mue)
      }
      case ioe: IOException => {
        Failure(ioe)
      }
    } 
  }

  def readLines(reader: BufferedReader) = {
    val sb = new StringBuilder
    try {
      readLine(sb, reader)
    }
    finally {
      reader.close()
    }
    
    sb.toString()
  }

  @tailrec
  private def readLine(sb: StringBuilder, reader: BufferedReader): Unit = {
    val s = reader.readLine
    if (s != null) {
      sb append s
      readLine(sb, reader)
    }
  }

  private def getReader(connection: URLConnection): Try[BufferedReader] = {
    for (i <- 0 to 5) {
      try {
        val is = connection.getInputStream
        val in = new InputStreamReader(is)
        return Success(new BufferedReader(in))
      }
      catch {
        case e: SocketTimeoutException => {}
      }
    }

    Failure(new IOException("Read timed out after five tries."))
  }
}