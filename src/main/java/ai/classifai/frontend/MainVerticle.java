package ai.classifai.frontend;

import ai.classifai.backend.repository.JdbcHolder;
import ai.classifai.backend.repository.service.ImageBoundingBoxRepoService;
import ai.classifai.backend.repository.service.ImageBoundingBoxService;
import ai.classifai.backend.repository.service.ProjectRepoService;
import ai.classifai.backend.repository.service.ProjectServiceImpl;
import ai.classifai.core.dto.BoundingBoxDTO;
import ai.classifai.core.dto.properties.BoundingBoxProperties;
import ai.classifai.core.dto.properties.ImageProperties;
import ai.classifai.core.entity.annotation.ImageBoundingBoxEntity;
import ai.classifai.core.service.annotation.AnnotationRepository;
import ai.classifai.core.service.annotation.AnnotationService;
import ai.classifai.core.service.project.ProjectRepository;
import ai.classifai.core.service.project.ProjectService;
import ai.classifai.frontend.router.RouterService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MainVerticle extends AbstractVerticle {
    // every injection should done here
    private final ProjectRepository projectRepoService;
    private final ProjectService projectService;
    private final AnnotationService<BoundingBoxDTO, BoundingBoxProperties, ImageProperties> imageBoundingBoxService;
    private final AnnotationRepository<ImageBoundingBoxEntity, BoundingBoxDTO> imageBoundingBoxRepoService;
    private final RouterService routerService;
    private final JdbcHolder jdbcHolder;

    public MainVerticle(Vertx vertx) {
        this.jdbcHolder = new JdbcHolder(vertx);
        this.projectRepoService = new ProjectRepoService(jdbcHolder);
        this.projectService = new ProjectServiceImpl(projectRepoService);
        this.imageBoundingBoxRepoService = new ImageBoundingBoxRepoService(jdbcHolder);
        this.imageBoundingBoxService = new ImageBoundingBoxService(imageBoundingBoxRepoService);
        this.routerService = new RouterService(projectService, imageBoundingBoxService);
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
