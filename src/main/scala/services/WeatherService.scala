package services

import java.io.{BufferedReader, IOException, InputStreamReader}
import java.net.{MalformedURLException, SocketTimeoutException, URL, URLConnection}
import java.time.LocalDateTime

import choreography.RemoteClient
import com.google.gson.Gson
import com.twitter.finagle.http.Response
import com.twitter.util.Future

import scala.annotation.tailrec
import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}

abstract class WeatherService {
  val url: String
  val host: String
  def getInfo: Future[String]

  protected lazy val client = new RemoteClient(s"$host:80")
  protected val gson = new Gson
  protected def now = LocalDateTime.now
  protected def hour = now.getHour
  protected def isNight = hour < 7 || hour > 19

  protected def callRemote: Future[Response] = client.get(url)

  protected def deserialize[A : ClassTag](text: String) = Try {
    gson.fromJson(text, implicitly[ClassTag[A]].runtimeClass.asInstanceOf[Class[A]])
  }

  protected def serialize[A : ClassTag](obj: A) = Try {
    gson.toJson(obj, implicitly[ClassTag[A]].runtimeClass.asInstanceOf[Class[A]])
  }

  protected def readUrl(urlStr: String, token: Option[String] = None): Try[String] = {
    try {
      val connection = new URL(urlStr).openConnection
      connection.setConnectTimeout(20000)
      getReader(connection) map readLines
    }
    catch {
      case mue: MalformedURLException => {
        Failure(mue)
      }
      case ioe: IOException => {
        Failure(ioe)
      }
    } 
  }

  def readLines(reader: BufferedReader) = {
    val sb = new StringBuilder
    try {
      readLine(sb, reader)
    }
    finally {
      reader.close()
    }
    
    sb.toString()
  }

  @tailrec
  private def readLine(sb: StringBuilder, reader: BufferedReader): Unit = {
    val s = reader.readLine
    if (s != null) {
      sb append s
      readLine(sb, reader)
    }
  }

  private def getReader(connection: URLConnection): Try[BufferedReader] = {
    for (i <- 0 to 5) {
      try {
        val is = connection.getInputStream
        val in = new InputStreamReader(is)
        return Success(new BufferedReader(in))
      }
      catch {
        case e: SocketTimeoutException => {}
      }
    }

    Failure(new IOException("Read timed out after five tries."))
  }
}