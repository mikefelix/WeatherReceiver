package services

import com.twitter.util.Future

/**
 * WeatherService
 * User: michael.felix
 * Date: 11/26/15
 */
class CurrentWeatherService extends WeatherService {
  val host = "api.wunderground.com"
  val url = "/api/4ff057a10c9613a4/conditions/q/UT/Murray.json"

  override def getInfo: Future[String] = {
    client.get(url) map (_.contentString) map { text =>

      val tryTransform = for {
        cond <- deserialize[Conditions](text)
        ref = reformatConditions(cond)
        ser <- serialize(ref)
      } yield ser

      tryTransform getOrElse "Could not retrieve current conditions."
    }
  }

  private def reformatConditions(conditions: Conditions) =
    CurrentInfo(conditions.current_observation.icon,
        java.lang.Float.parseFloat(conditions.current_observation.temp_f).round,
        conditions.current_observation.relative_humidity.replaceAll("[^0-9.]", "").toInt,
        isNight)

}