package ai.classifai.core;

import ai.classifai.backend.repository.service.ProjectRepoService;
import ai.classifai.core.services.project.ProjectService;
import ai.classifai.core.services.project.ProjectServiceImpl;
import ai.classifai.core.services.project.ProjectRepository;
import ai.classifai.frontend.router.RouterService;
import ai.classifai.backend.repository.JdbcHolder;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MainVerticle extends AbstractVerticle {
    // every injection should done here
    private final ProjectRepository projectRepoService;
    private final ProjectService projectService;
    private final RouterService routerService;
    private final JdbcHolder jdbcHolder;

    public MainVerticle(Vertx vertx) {
        this.jdbcHolder = new JdbcHolder(vertx);
        this.projectRepoService = new ProjectRepoService(jdbcHolder);
        this.projectService = new ProjectServiceImpl(projectRepoService);
        this.routerService = new RouterService(projectRepoService);
    }

    @Override
    public void start(Promise<Void> promise) {
        Promise<String> deploy = Promise.promise();
        vertx.deployVerticle(routerService, deploy);

        deploy.future().onComplete(res -> {
            if (res.succeeded()) {
                log.info("start");
            }

            else if (res.failed()) {
                log.info(res.cause().getMessage());
            }
        });
    }

    @Override
    public void stop() {
        routerService.stop();
        jdbcHolder.stop();

        vertx.close(voidAsyncResult -> {
            log.info("verticle close");
        });
    }
}
