package services

import java.net.InetSocketAddress

import choreography.RouteMatch
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
  type Handler = Request => Future[String]

  def routeMappings: PartialFunction[RouteMatch, Handler]

  def handle(req: Request): Future[Response] = {
      val route = RouteMatch(req.method, req.path)
      if (routeMappings.isDefinedAt(route)) {
        val handler = routeMappings(route)
        val result = handler(req).map { str =>
          val r = Response(Version.Http11, Status.Ok)
          r.contentString = str
          r
        }

        result.onSuccess { r =>
          println(s"-> $r")
        }.onFailure { r =>
          println(s"!-> $r")
        }

        result
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
        upstreamResult
      } ensure { res: Response =>
        res.close()
        request.close()
      }
    }
  }

  val server = ServerBuilder()
    .codec(com.twitter.finagle.http.Http())
    .bindTo(new InetSocketAddress(7690))
    .name("Server")
    .build(service)

  println("Starting...")
  Await.ready(server)
}
