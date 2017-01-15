package services

import model.WeatherResult

import scala.collection.mutable
import scala.util.matching.Regex

/**
  * WeatherCache
  * User: michael.felix
  * Date: 10/30/16
  */
class ResultsCache {
  val cache = new mutable.HashMap[String, WeatherResult]()

  def getAll[T <: WeatherResult](r: Regex): Seq[T] = {
    cache.keySet.filter(r.findFirstIn(_).nonEmpty).map(key => cache(key).asInstanceOf[T]).toSeq
  }

  def get[T](kind: String): T = {
    cache(kind).asInstanceOf[T]
  }

  def put(kind: String, result: WeatherResult) = {
    println(s"Storing: $kind/${result.recordedAt}")
    cache.put(kind, result)
  }
}
