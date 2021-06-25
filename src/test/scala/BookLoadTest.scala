import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

class BookLoadTest extends Simulation {

  private val baseUrl = "http://localhost:5000"
  private val addBookEndpoint = "/book/1000/HarryPotter/JKRowling"
  private val getBooksEndpoint = "/books"
  private val contentType = "application/json"

  private val addBookCount = 1000
  private val addBookUsers = System.getProperty("ADD_USERS", "5").toInt

  private val getBookCount = 100
  private val getBookUsers = System.getProperty("QUERY_USERS", "0").toInt

  private val httpConf = http.baseUrl(baseUrl)
    .acceptHeader("application/json;charset=UTF-8")

  private val addBookTest = repeat(addBookCount) {
    exec(http("add-book-test")
      .post(addBookEndpoint)
      .header("Content-Type", contentType)
      .body(StringBody(
        s"""
           | {
           |  "firstName": "test-firstName",
           |  "lastName": "test-lastName"
           | }
         """.stripMargin
      )).check(status.is(200)))
  }

  private val getBooksTest = repeat(getBookCount) {
    exec(http("get-books-test")
      .get(getBooksEndpoint)
      .header("Content-Type", contentType)
      .check(status.is(200)))
  }

  private val addBooksScenarioIteration1 = scenario("AddBookSimulation - Iteration 1")
    .exec(addBookTest)
    .exec(getBooksTest)

  private val addBooksScenarioIteration2 = scenario("AddBookSimulation - Iteration 2")
    .exec(addBookTest)

  private val addBooksScenarioIteration3 = scenario("AddBookSimulation - Iteration 3")
    .exec(addBookTest)

  private val getBooksScenario = scenario("GetBooksSimulation")
    .exec(getBooksTest)

  /*setUp(
//    addBooksScenario.inject(constantUsersPerSec(addBookUsers).during(10.seconds)).throttle(reachRps(100).in(10.seconds), holdFor(2.minute)),
//    getBooksScenario.inject(constantUsersPerSec(getBookUsers).during(10.seconds)).throttle(reachRps(10).in(10.seconds), holdFor(2.minute)),
//    addBooksScenario.inject(constantUsersPerSec(addBookUsers).during(60.seconds)),
//    getBooksScenario.inject(constantUsersPerSec(getBookUsers).during(60.seconds))
//    addBooksScenario.inject(rampConcurrentUsers(1).to(addBookUsers).during(10.seconds),
//    getBooksScenario.inject(rampConcurrentUsers(1).to(getBookUsers).during(10.seconds)
    addBooksScenario.inject(atOnceUsers(addBookUsers)),
    getBooksScenario.inject(atOnceUsers(getBookUsers))
  ).protocols(httpConf)*/

  setUp(
      addBooksScenarioIteration1.inject(
        incrementConcurrentUsers(addBookUsers)
          .times(8)
          .eachLevelLasting(20 seconds)
          .separatedByRampsLasting(10 seconds)
          .startingFrom(0)
      ),
    getBooksScenario.inject(
      incrementConcurrentUsers(getBookUsers)
        .times(8)
        .eachLevelLasting(20 seconds)
        .separatedByRampsLasting(10 seconds)
        .startingFrom(0)
    )
  ).maxDuration(120 seconds).protocols(httpConf)

  /*
  .andThen(
        addBooksScenarioIteration2.inject(
          incrementConcurrentUsers(addBookUsers)
            .times(8)
            .eachLevelLasting(20 seconds)
            .separatedByRampsLasting(10 seconds)
            .startingFrom(0)
        )
      )
      .andThen(
        addBooksScenarioIteration3.inject(
          incrementConcurrentUsers(addBookUsers)
            .times(8)
            .eachLevelLasting(20 seconds)
            .separatedByRampsLasting(10 seconds)
            .startingFrom(0)
        )
      )
   */
}