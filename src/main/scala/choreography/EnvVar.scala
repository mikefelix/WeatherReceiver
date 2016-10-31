package choreography

/**
  * Env
  * User: michael.felix
  * Date: 5/10/16
  */
object EnvVar {
  val subRegex = "\\{\\{([a-zA-Z0-9_]+)\\}\\}".r

  def apply(name: String) = {
    var prop = scala.util.Properties.envOrNone(name)
      .getOrElse(throw new IllegalStateException(s"No $name found in the environment."))

    val matches = subRegex.findAllIn(prop).matchData

    matches.foreach { m =>
      val sub = scala.util.Properties.envOrNone(m.subgroups.head)
      if (sub.isEmpty)
        throw new IllegalStateException(s"No $sub found in the environment.")

      prop = prop.replace(m.matched, sub.get)
    }

    prop
  }
}
