package services

import java.net.InetSocketAddress

import choreography.{EnvVar, RouteMatch}
import com.twitter.finagle.Service
import com.twitter.finagle.builder.ServerBuilder
import com.twitter.finagle.http.{Request, Response, Status, Version}
import com.twitter.util.{Await, Future}

/**
  * RouteMappings
  * User: michael.felix
  * Date: 5/10/16
  */
abstract class Runner extends App {
  def routeMappings: PartialFunction[RouteMatch, String]
  def startActors(): Unit
  def handle(req: Request): Future[Response] = {
      val route = RouteMatch(req.method, req.path)
      if (routeMappings.isDefinedAt(route)) {
        val result = Response(Version.Http11, Status.Ok)
        result.contentString = routeMappings(route)
        Future.value(result)
      }
      else {
        Future.value(Response(Status.NotFound))
      }
    }

  val service = new Service[Request, Response] {
    def apply(request: Request): Future[Response] = {
      val downstreamResult = handle(request)

      downstreamResult map { res =>
        val upstreamResult = Response(Version.Http11, res.status)
        upstreamResult.contentString = res.contentString
        upstreamResult.headerMap.add("Access-Control-Allow-Origin", "*")
        upstreamResult
      } ensure { res: Response =>
        res.close()
        request.close()
      }
    }
  }

  val server = ServerBuilder()
    .codec(com.twitter.finagle.http.Http())
    .bindTo(new InetSocketAddress(EnvVar("PORT").toInt))
    .name("Server")
    .build(service)

  println("Starting actors...")
  startActors()
  println("Starting server...")
  Await.ready(server)
}
