package services

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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

  def currentDateTime = LocalDateTime.now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

  def transformInput(res: I): Attempt[O]

  def remoteResult: Attempt[String] = Attempt { await(callRemote) }
  def consumeRemote(input: String): Attempt[I] = Attempt { deserialize(input) }
  def serializeOutput(o: O): Attempt[String] = Attempt.orFail("Couldn't serialize output") { serialize(o) }

  def getInput = for {
    res <- remoteResult
    i <- consumeRemote(res)
  } yield i

  def toOutput(i: I) = transformInput(i) map serializeOutput

  protected lazy val client = new RemoteClient(host, useTls)
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
    gson.fromJson(text, implicitly[ClassTag[I]].runtimeClass.asInstanceOf[Class[I]])
  }

  protected def serialize(obj: O) = Try {
    gson.toJson(obj, implicitly[ClassTag[O]].runtimeClass.asInstanceOf[Class[O]])
  }
}