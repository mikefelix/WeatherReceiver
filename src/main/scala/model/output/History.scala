package model.output

import model.input.HistoricalData

/**
  * History
  * User: michael.felix
  * Date: 10/31/16
  */
case class History(data: Seq[HistoricalData]) extends HistoricalData {
  implicit class AverageFloats(set: Iterable[Option[Float]]){
    def average = {
      val floats = for {
        opt <- set
        some <- opt
      } yield some

      println(s"Averaging ${floats.size} readings.")
      if (floats.nonEmpty)
        Some(floats.sum / floats.size)
      else
        None
    }

  }

  override val low: Option[Float] = data.map(_.low).average
  override val high: Option[Float] = data.map(_.high).average
  override val precip: Option[Float] = data.map(_.precip).average
}
