package actors

case object Refresh
case object ReformatCurrent
case class ReformatForecast(first: Boolean)
case class RetrieveDay(day: Int, month: Int, year: Int)
case class CompileHistory(day: Int, month: Int)
