package controllers

import choreography.TwitterConverters._
//import com.twitter.util.{Future => TFuture}
import play.api.mvc.Results.Ok
import play.api.mvc._
import services._

import scala.concurrent.Future

object WeatherController {
  val currentWeatherService = new CurrentWeatherService
  val forecast1WeatherService = new Forecast1WeatherService
  val forecast2WeatherService = new Forecast2WeatherService



  def current = Action { req =>
    val res: Future[Result] = currentWeatherService.getInfo map { t =>
      Ok(t)
    }

    res
    Ok("eff off")
    Future.successful(Ok("hi"))
  }

//  def forecast1() = weatherAction(forecast1WeatherService.getInfo)
//  def forecast2() = weatherAction(forecast2WeatherService.getInfo)
}
