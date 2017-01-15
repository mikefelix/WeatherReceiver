package choreography

import com.twitter.finagle.http._
import com.twitter.finagle.{Service, Http => HTTP}
import com.twitter.util.Future

import scala.util.{Failure, Success, Try}

/**
  * RemoteClient
  * User: michael.felix
  * Date: 5/10/16
  */
class RemoteClient(host: String, useTls: Boolean) extends Service[Request, Try[String]] {
  val port = if (useTls) 443 else 80

  val client = if (useTls)
      HTTP.client.withSessionQualifier.noFailFast
        .withTls(host)
        .newService(s"$host:$port")
    else
      HTTP.client.withSessionQualifier.noFailFast
        .newService(s"$host:$port")

//    ClientBuilder()
//    .noFailureAccrual
//    .codec(Http())
//    .hosts(hostAndPort)
//    .tls(useTls)
//    .hostConnectionLimit(1)
//    .tcpConnectTimeout(1.second)
//    .retries(2)
//    .build()

  def apply2(request: Request): Future[Response] = {
    client(request) map { res =>
      val response = Response(Version.Http11, Status.Ok)
      response.contentString = res.contentString
      response
    }
  }

  override def apply(request: Request): Future[Try[String]] = {
     client(request) map { res =>
       if (res.statusCode > 299) {
         println(s"FAIL: ${res.statusCode} (${res.status.reason}) ${res.contentString}")
         Failure(new RuntimeException(res.statusCode + ": " + res.contentString))
       }
       else {
//         println(s"SUCC: ${res.statusCode} (${res.status.reason}) ${res.contentString}")
         Success(res.contentString)
       }
     }
   }

  def post(path: String, body: String, headers: (String, String)*) = doRequest(Method.Post, path, Some(body), headers: _*)
  def get(path: String, headers: (String, String)*) = doRequest(Method.Get, path, None, headers: _*)
  def patch(path: String, body: String, headers: (String, String)*) = doRequest(Method.Patch, path, Some(body), headers: _*)
  def put(path: String, body: String, headers: (String, String)*) = doRequest(Method.Put, path, Some(body), headers: _*)
  def delete(path: String, headers: (String, String)*) = doRequest(Method.Delete, path, None, headers: _*)

  private[this] def doRequest(method: Method, path: String, body: Option[String], headers: (String, String)*): Future[Try[String]] = {
//    println(s"Do request: $method $host:$port/$path {${body.getOrElse("")}} $headers")
    val req = Request(Version.Http11, method, path)
    for (t <- headers){
      req.headerMap.set(t._1, t._2)
    }

    req.host = host
    if (body.nonEmpty)
      req.contentString = body.get

    apply(req)
  }
}
