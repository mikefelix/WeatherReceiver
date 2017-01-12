package model.input

import util.Attempt

import scala.collection.JavaConverters._
import model.WeatherResult

class SingleHistoricalDatum extends HistoricalData {
  var results: java.util.ArrayList[Reading] = _
  lazy val low: Option[Float] = measurement("TMIN")
  lazy val high: Option[Float] = measurement("TMAX")
  lazy val precip: Option[Float] = measurement("PRCP")

  private def measurement(which: String) = Option(results.asScala).flatMap(_.find(_.datatype == which)).map(_.value)
}

case class Reading(date: String, datatype: String, value: Float)

trait HistoricalData extends WeatherResult {
  val low: Option[Float]
  val high: Option[Float]
  val precip: Option[Float]

  override def toString = s"low: $low, high: $high, precip: $precip"
}

object HistoricalData {
  def attemptAverage(seq: Iterable[Option[Float]]): Attempt[Float] = if (seq.isEmpty) {
    Attempt failure "No readings to average"
  }
  else {
    val somes = seq.filter(_.nonEmpty).map(_.get)
    Attempt.success(somes.sum / somes.size)
  }
}