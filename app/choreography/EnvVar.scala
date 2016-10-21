package choreography

/**
  * Env
  * User: michael.felix
  * Date: 5/10/16
  */
object EnvVar {
  def apply(name: String) = scala.util.Properties.envOrElse(name, throw new IllegalStateException(s"No $name found in the environment."))
}
