import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Future
import services.{Forecast, ForecastInfo}

import scala.util.Try

/*
import com.twitter.finagle.http.Response
import com.twitter.util.Future

def get(y: Int): Future[Response] = Future.value(Response())

val a = for {
  year <- 1 to 50
  res <- get(year)
} yield res
val b: Future[Seq[Response]] = Future.collect(a)
println(b)*/

private def deserialize(s: String): Try[Forecast] = Try {
   new Forecast
 }

def serialize(f: ForecastInfo) = Try("")

def reformatForecast(forecast: Forecast) = new ForecastInfo("", 1, 1, "", "")

for {
  res <- Future.value(Response(status = Status.Accepted))
  text = res.contentString
  forecast <- deserialize(text)
  info = reformatForecast(forecast)
  output <- serialize(info)
} yield output