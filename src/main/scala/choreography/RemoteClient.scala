package choreography

import com.twitter.finagle.http._
import com.twitter.finagle.{Http => HTTP, Service}
import com.twitter.util.Future

/**
  * RemoteClient
  * User: michael.felix
  * Date: 5/10/16
  */
class RemoteClient(hostEnvParam: String) extends Service[Request, Response] {
  val hostAndPort = hostEnvParam
  val (host, port) = {
    val arr = hostAndPort.split(":")
    (arr(0), arr(1))
  }

  val client = HTTP
    .client.withSessionQualifier.noFailFast
//    .withTls(host)
    .newService(hostAndPort)
//    ClientBuilder()
//    .noFailureAccrual
//    .codec(Http())
//    .hosts(hostAndPort)
//    .tls(host)
//    .hostConnectionLimit(1)
//    .tcpConnectTimeout(1.second)
//    .retries(2)
//    .build()
2
  override def apply(request: Request) = {
    client(request) map { res =>
      val response = Response(Version.Http11, Status.Ok)
      response.contentString = res.contentString
      response
    }
  }

  def post(path: String, body: String, headers: (String, String)*) = doRequest(Method.Post, path, Some(body), headers: _*)
  def get(path: String, headers: (String, String)*) = doRequest(Method.Get, path, None, headers: _*)
  def patch(path: String, body: String, headers: (String, String)*) = doRequest(Method.Patch, path, Some(body), headers: _*)
  def put(path: String, body: String, headers: (String, String)*) = doRequest(Method.Put, path, Some(body), headers: _*)
  def delete(path: String, headers: (String, String)*) = doRequest(Method.Delete, path, None, headers: _*)

  private[this] def doRequest(method: Method, path: String, body: Option[String], headers: (String, String)*): Future[Response] = {
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
