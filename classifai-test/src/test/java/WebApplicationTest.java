import io.reactiverse.junit5.web.WebClientOptionsInject;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.reactiverse.junit5.web.TestRequest.*;

// tag::testExtensions[]
@ExtendWith({
        VertxExtension.class,
})
// end::testExtensions[]
public class WebApplicationTest {

  // tag::webClientOptions[]
  @WebClientOptionsInject
  public WebClientOptions opts = new WebClientOptions()
          .setDefaultPort(9000)
          .setDefaultHost("localhost");
  // end::webClientOptions[]

  String deploymentId;

  // tag::setUp[]
  @BeforeEach
  void setUp(Vertx vertx, VertxTestContext testContext) {
    testContext
            .assertComplete(vertx.deployVerticle(new WebApplicationVerticle()))
            .onComplete(ar -> {
              deploymentId = ar.result();
              testContext.completeNow();
            });
  }
  // end::setUp[]

  // tag::tearDown[]
  @AfterEach
  void tearDown(Vertx vertx, VertxTestContext testContext) {
    testContext
            .assertComplete(vertx.undeploy(deploymentId))
            .onComplete(ar -> testContext.completeNow());
  }
  // end::tearDown[]

  // tag::testGetPetOk[]
  @Test
  public void testGetPetOk(WebClient client, VertxTestContext testContext) {
    testRequest(client.get("/pet/1"))
            .expect(
                    statusCode(200),
                    responseHeader("x-pet-id", "1"),
                    jsonBodyResponse(new JsonObject().put("id", 1).put("name", "Fufi").put("tag", "ABC"))
            )
            .send(testContext);
  }
  // end::testGetPetOk[]

  // tag::testGetPetErrors[]
  @Test
  public void testGetPetBadRequest(WebClient client, VertxTestContext testContext) {
    testRequest(client.get("/pet/bad"))
            .expect(
                    statusCode(400),
                    emptyResponse()
            )
            .send(testContext);
  }

  @Test
  public void testGetPetNotFound(WebClient client, VertxTestContext testContext) {
    testRequest(client.get("/pet/10"))
            .expect(
                    statusCode(404),
                    emptyResponse()
            )
            .send(testContext);
  }
  // end::testGetPetErrors[]

  // tag::testPostAndGetPet[]
  @Test
  public void testPostAndGetPet(WebClient client, VertxTestContext testContext) {
    final JsonObject newPet = new JsonObject().put("id", 5).put("name", "Pippo");

    final Checkpoint checkpoint = testContext.checkpoint(2);

    testRequest(client.post("/pet"))
            .expect(
                    statusCode(202),
                    emptyResponse()
            )
            .sendJson(newPet, testContext, checkpoint)
            .onComplete(ar -> // Executed after the first response is completed
                    testRequest(client.get("/pet/5"))
                            .expect(
                                    statusCode(200),
                                    responseHeader("x-pet-id", "5"),
                                    jsonBodyResponse(newPet)
                            )
                            .send(testContext, checkpoint)
            );
  }

  // end::testPostAndGetPet[]

}
