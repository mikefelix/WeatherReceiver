package model.output

import java.util.regex.Pattern

import model.WeatherInfo

/**
 * ForecastInfo
 * User: michael.felix
 * Date: 11/26/15
 */
object ForecastInfo {
  val chanceRainPattern: Pattern = Pattern.compile("[Cc]hance of (rain|(thunder)?storms|precipitation) *(around|near)? *(\\d+)%?")
  val chanceSnowPattern: Pattern = Pattern.compile("[Cc]hance of (snow|flurries) *(around|near)? *(\\d+)%?")
}

case class ForecastInfo(title: String, temp: Int, hum: Int, cond: String, cast: String)
  extends WeatherInfo {

  override def displayTemp = (if (night) "↓" else "↑") + temp + "°"

  def getExtra = {
    val m = ForecastInfo.chanceRainPattern.matcher(cast)
    if (m.find) {
      ":" + m.group(4) + "%"
    }
    else {
      val m2 = ForecastInfo.chanceSnowPattern.matcher(cast)
      if (m2.find)
        ":" + m2.group(3) + "%"
      else
        hum + "%"
    }
  }

  val night = title.toLowerCase().indexOf("night") >= 0

  def shortCast = {
    var shortCast = cast
      .replaceAll("((will be )?followed by|will become|becoming|(will give|giving) way to)", "then")
      .replaceAll(" (and|then|with) ", ", ")
      .replaceAll("(near|around|approaching) ", "~")
      .replaceAll("afternoon", "PM")
      .replaceAll("morning", "AM")
      .replaceAll("overnight", "late")
      .replaceAll("cloudiness", "clouds")
      .replaceAll("through(out)?", "thru")
      .replaceAll("(a |the )?possibility of", "possible")
      .replaceAll("(\\d+) to (\\d+) mph", "$1-$2 mph")
      .replaceAll("at (\\d+-\\d+ mph)", "$1")
      .replaceAll("Thunderstorm", "Storm")
      .replaceAll("(shower or )?[Tt]hunderstorm", "storm")
      .replaceAll("(\\w+) in the (PM|AM|afternoon|evening|night|morning)", "$2 $1")
      .replaceAll("([Ss])cattered", "$1catt.")
      .replaceAll("\\.\\.", ".")
      .replaceAll("(Low|high|low|High) (~|around |near )?(\\d+[Ff]?)\\. ?", "")

    while (shortCast.length > 65)
      shortCast = shortCast.replaceFirst("\\. +[^.]+\\.$", ".")

    shortCast
  }
}