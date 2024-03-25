import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;
public class MovieRecommendationTest extends Simulation {
    private HttpProtocolBuilder httpProtocol = http.baseUrl("http://128.2.205.107:8082").acceptHeader("application/json");
    private static final Integer USER_COUNT = Integer.parseInt(System.getProperty("USER_COUNT", "100"));
    private static final Integer RAMP_DURATION = Integer.parseInt(System.getProperty("RAMP_DURATION", "4"));

    @Override
    public void before() {
        System.out.printf("Running the test for %d users and ramping over %d seconds\n", USER_COUNT, RAMP_DURATION);
    }
    private static FeederBuilder.FileBased<Object> jsonFeederSuccessCase = jsonFile("data/recommendationJsonFileSuccess.json").random();
    private static FeederBuilder.FileBased<Object> jsonFeederFailureCase = jsonFile("data/recommendationJsonFileFailure.json").random();
    private ChainBuilder successChainBuilder = feed(jsonFeederSuccessCase)
            .exec(http("Get the recommendation for #{userId}")
                    .get("/recommend/#{userId}").check(status().shouldBe(200)));
    private ChainBuilder failureChainBuilder = feed(jsonFeederFailureCase).exec(http("Get the recommendation for #{userId}")
            .get("/recommend/#{userId}").check(status().shouldBe(400)));
    private ScenarioBuilder scenarioBuilder = scenario("Movie recommendation stress test ")
            .exec(successChainBuilder).pause(5).exec(failureChainBuilder);

    {
        setUp(scenarioBuilder.injectOpen(
                nothingFor(5),rampUsers(USER_COUNT).during(RAMP_DURATION)
        ).protocols(httpProtocol));
    }
}