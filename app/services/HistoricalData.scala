package services

/**
  * History
  * User: michael.felix
  * Date: 5/27/16
  */
case class HistoricalData(results: Seq[Reading]){
  def low = results.find(_.datatype == "TMIN").map(_.value)
  def high = results.find(_.datatype == "TMAX").map(_.value)
  def precip = results.find(_.datatype == "PRCP").map(_.value)
}

case class Reading(date: String, datatype: String, value: Float)
