import com.twitter.util.Await
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}
import services.CurrentWeatherService

/**
 * add your integration spec here.
 * An integration test will fire up a whole play application in a real (or headless) browser
 */
//@RunWith(classOf[JUnitRunner])
class IntegrationSpec extends FlatSpec with Matchers with BeforeAndAfter {
  val currentService = new CurrentWeatherService

  it should "proxy GET requests to order" in {
      val f = currentService.info
      val res = Await result f
      println(res)
    }
}
