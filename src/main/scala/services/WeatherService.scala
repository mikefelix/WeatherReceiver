package services

import java.time.LocalDateTime

import choreography.RemoteClient
import choreography.TwitterConverters._
import com.google.gson.Gson
import com.twitter.util.Future
import util.Attempt

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}

abstract class WeatherService[I : ClassTag, O : ClassTag] {
  val url: String
  val host: String
  val token: String
  val useTls: Boolean

  def transformInput(res: I): Attempt[O]

  def remoteResult: Attempt[String] = Attempt { await(callRemote) }
  def consumeRemote(input: String): Attempt[I] = Attempt { deserialize(input) }
  def serializeOutput(o: O): Attempt[String] = Attempt.orFail("Couldn't serialize output") { serialize(o) }

  def getInput = for {
    res <- remoteResult
    i <- consumeRemote(res)
  } yield i

  def toOutput(i: I) = transformInput(i) map serializeOutput

  protected lazy val client = new RemoteClient(s"$host:${if (useTls) 443 else 80}")
  protected val gson = new Gson

  protected def now = LocalDateTime.now
  protected def hour = now.getHour
  protected def isNight = hour < 7 || hour > 19

  protected def await[T](f: Future[T]) = Try(Await.result(f, 10 seconds))

  protected def callRemote: Future[String] = client.get(url) map {
    case Success(value) => value
    case Failure(exception) => exception.getMessage
  }

  protected def deserialize(text: String) = Try {
    println(s"Deserialize this: $text")
    val d = gson.fromJson(text, implicitly[ClassTag[I]].runtimeClass.asInstanceOf[Class[I]])
    println(s"I made a ${d.getClass.getName}: $d")
    d
  }

  protected def serialize(obj: O) = Try {
    gson.toJson(obj, implicitly[ClassTag[O]].runtimeClass.asInstanceOf[Class[O]])
  }

/*
  protected def readUrl(urlStr: String, token: Option[String] = None): Try[String] = Try {
    val connection = new URL(urlStr).openConnection
    connection.setConnectTimeout(20000)
    getReader(connection) map readLines
  }

  protected def readLines(reader: BufferedReader) = {
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

  private def getReader(connection: URLConnection): Attempt[BufferedReader] = {
    for (i <- 0 to 5) {
      try {
        val is = connection.getInputStream
        val in = new InputStreamReader(is)
        return Attempt.success(new BufferedReader(in))
      }
      catch {
        case e: SocketTimeoutException =>
          println(s"Timeout, trying again.")
      }
    }

    Attempt failure "Read timed out after five tries."
  }
*/
}