package services

object Forecast1WeatherService extends ForecastWeatherService {
  override def reformat(res: ApiResponse) = {
    val des = deserialize[Forecast](res.result)
      .map(reformatForecast(_, first = true))
      .flatMap(serialize)

    des.map(Reformatting(now, _))
  }
}