package services

import choreography.EnvVar

object CurrentWeatherService extends WeatherService {
  val host = EnvVar("CURRENT_HOST")
  val token = EnvVar("CURRENT_TOKEN")
  val url = EnvVar("CURRENT_PATH")

  override def reformat(res: ApiResponse) = {
    val des = deserialize[Conditions](res.result)
          .map(new CurrentInfo(_))
          .flatMap(serialize)

    des.map(Reformatting(now, _))
  }

}