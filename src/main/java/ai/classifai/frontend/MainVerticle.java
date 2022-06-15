package ai.classifai.frontend;

import ai.classifai.backend.repository.JdbcHolder;
import ai.classifai.backend.repository.service.*;
import ai.classifai.core.dto.AudioDTO;
import ai.classifai.core.dto.BoundingBoxDTO;
import ai.classifai.core.dto.SegmentationDTO;
import ai.classifai.core.properties.AudioProperties;
import ai.classifai.core.properties.ImageProperties;
import ai.classifai.core.entity.annotation.AudioEntity;
import ai.classifai.core.entity.annotation.ImageBoundingBoxEntity;
import ai.classifai.core.entity.annotation.ImageSegmentationEntity;
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
    private final ProjectRepository projectRepoService;
    private final ProjectService projectService;
    private final AnnotationService<BoundingBoxDTO, ImageProperties> imageBoundingBoxService;
    private final AnnotationRepository<ImageBoundingBoxEntity, BoundingBoxDTO, ImageProperties> imageBoundingBoxRepoService;
    private final AnnotationService<SegmentationDTO, ImageProperties> imageSegmentationService;
    private final AnnotationRepository<ImageSegmentationEntity, SegmentationDTO, ImageProperties> imageSegmentationRepoService;
    private final AnnotationRepository<AudioEntity, AudioDTO, AudioProperties> audioRepoService;
    private final AnnotationService<AudioDTO, AudioProperties> audioService;
    private final RouterService routerService;
    private final JdbcHolder jdbcHolder;

    public MainVerticle(Vertx vertx) {
        this.jdbcHolder = new JdbcHolder(vertx);
        this.projectRepoService = new ProjectRepoService(jdbcHolder);
        this.projectService = new ProjectServiceImpl(projectRepoService);
        this.imageBoundingBoxRepoService = new ImageBoundingBoxRepoService(jdbcHolder);
        this.imageBoundingBoxService = new ImageBoundingBoxService(imageBoundingBoxRepoService, projectService);
        this.imageSegmentationRepoService = new ImageSegmentationRepoService(jdbcHolder);
        this.imageSegmentationService = new ImageSegmentationService(imageSegmentationRepoService, projectService);
        this.audioRepoService = new AudioRepoService(jdbcHolder);
        this.audioService = new AudioService(audioRepoService, projectService);
        this.routerService = new RouterService(projectService, imageBoundingBoxService, imageSegmentationService, audioService);
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
