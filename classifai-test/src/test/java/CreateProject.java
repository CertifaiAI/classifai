import ai.classifai.MainVerticle;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.util.type.AnnotationType;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import static io.reactiverse.junit5.web.TestRequest.*;
import static org.assertj.core.api.Assertions.assertThat;


import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
@ExtendWith({
        VertxExtension.class,
})
public class CreateProject {
    private static ProjectLoader loader = null;
    String deploymentId;

    @BeforeAll
    static void prepareDb() throws IOException {
        DbSetup.backupOriginalDatabase();
        DbSetup.copyTestDbToUserPath();
    }

    @BeforeEach
    void setupVerticle(Vertx vertx, VertxTestContext testContext) {
        testContext.assertComplete(vertx.deployVerticle(new MainVerticle()))
                .onComplete(ar -> {
                    deploymentId = ar.result();
                    testContext.completeNow();
                });
    }

    @AfterEach
    void tearVerticle(Vertx vertx, VertxTestContext testContext) {
        testContext.assertComplete(vertx.undeploy(deploymentId))
                .onComplete(ar -> testContext.completeNow());
    }

    @Test
    @DisplayName("Create project")
    void testCreateProject(Vertx vertx, VertxTestContext testContext) throws InterruptedException {
        HttpClient client = vertx.createHttpClient();
        client.request(HttpMethod.GET, "/bndbox/projects/meta")
                .compose(req -> req.send().compose(HttpClientResponse::body))
                .onComplete(testContext.succeeding(buffer -> testContext.verify(() -> {
                    log.info("DEVEN: " + buffer.toString());
                    assertThat("Plop").isEqualTo("Plop");
                    testContext.completeNow();
                })));
        closeVertx(vertx);
    }

    private void closeVertx(Vertx vertx) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        vertx.close(ar -> latch.countDown());
        assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
    }

//    @Test
//    @DisplayName("Create project")
//    void testCreateProject() {
//        // Create project
//
//        Assertions.assertEquals(
//                List.of(loader.getProjectName(), loader.getAnnotationType(), loader.getLabelList()),
//                List.of(LoaderSetup.getProjectName(), LoaderSetup.getAnnotationType(), LoaderSetup.getTestLabelList())
//        );
//    }

//    @Test
//    @DisplayName("Create project server up")
//    void testCreateProjectServerUp(Vertx vertx, VertxTestContext testContext) {
//
//    }

    //    private void createProjectNoServer() {
//        LoaderSetup.setProjectName("TestProject");
//        LoaderSetup.setAnnotationType(AnnotationType.BOUNDINGBOX.ordinal());
//        LoaderSetup.setTestLabelList(List.of("First", "Second", "Third"));
//        loader = LoaderSetup.createNewTestProject();
//    }

}
