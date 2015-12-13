package controllers

import play.api.mvc._
import play.api.mvc.Results._
import services.WeatherService

import scala.util.{Try, Success, Failure}

object WeatherController {
  def current() = weatherAction(WeatherService.getCurrentWeather)
  def forecast1() = weatherAction(WeatherService.getForecast1)
  def forecast2() = weatherAction(WeatherService.getForecast2)

  private def weatherAction(call: => Try[String]) = Action {
    call match {
      case Failure(exception) => InternalServerError
      case Success(info) => Ok(info)
    }
  }
}