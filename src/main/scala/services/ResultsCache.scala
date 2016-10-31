package services

import scala.collection.mutable
import scala.collection.Set
import scala.util.matching.Regex

/**
  * WeatherCache
  * User: michael.felix
  * Date: 10/30/16
  */
class ResultsCache {
  val cache = new mutable.HashMap[String, WeatherResult]()

  def getAll(r: Regex): Set[WeatherResult] = {
    cache.keySet.filter(r.findFirstIn(_).nonEmpty).map(key => cache(key))
  }

  def apply(kind: String, transformed: Boolean = true): WeatherResult = {
    val state = if (transformed) "post" else "pre"
    cache(s"$kind|$state")
  }

  def put(kind: String, transformed: Boolean = true, result: WeatherResult) = {
    val state = if (transformed) "post" else "pre"
    cache.put(state, result)
  }
}
