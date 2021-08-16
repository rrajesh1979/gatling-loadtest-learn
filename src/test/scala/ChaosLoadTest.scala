import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

import scala.language.postfixOps

class ChaosLoadTest extends Simulation {
  private val USER_COUNT = 100

  val httpProtocol = http
    .baseUrl("http://ng-web-elb-1370228387.us-east-2.elb.amazonaws.com") // Here is the root for all relative URLs
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8") // Here are the common headers
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")

  val scn = scenario("Load Test UI") // A scenario is a chain of requests and pauses
    .exec(http("homePage")
      .get("/"))

//  setUp(scn.inject(
//    atOnceUsers(USER_COUNT)
//  ).protocols(httpProtocol))

  setUp(
    scn.inject(
      incrementConcurrentUsers(USER_COUNT)
        .times(5)
        .eachLevelLasting(20 seconds)
        .separatedByRampsLasting(10 seconds)
        .startingFrom(0)
    )
  ).maxDuration(60 seconds).protocols(httpProtocol)
}
