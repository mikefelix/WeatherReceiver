import choreography.{Get, RouteMatch}
import services._

/**
  * RouteMappings
  * User: michael.felix
  * Date: 5/10/16
  */
object Main extends Runner {
  var currentService = new CurrentWeatherService
  var forecast1Service = new Forecast1WeatherService
  var forecast2Service = new Forecast2WeatherService
  var historicalService = new HistoricalWeatherService

  def routeMappings: PartialFunction[RouteMatch, Handler] = {
    case Get("current") => { req =>
      currentService.getInfo
    }
    case Get("forecast1") => { req =>
      forecast1Service.getInfo
    }
    case Get("forecast2") => { req =>
      forecast2Service.getInfo
    }
    case Get("historical") => { req =>
      historicalService.getInfo
    }
  }
}
