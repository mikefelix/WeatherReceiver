package services

/**
  * History
  * User: michael.felix
  * Date: 5/27/16
  */
case class HistoricalData(results: Seq[Reading]){
  val low = measurement("TMIN")
  val high = measurement("TMAX")
  val precip = measurement("PRCP")

  private def measurement(which: String) = Option(results).flatMap(_.find(_.datatype == which)).map(_.value)
}

case class Reading(date: String, datatype: String, value: Float)
