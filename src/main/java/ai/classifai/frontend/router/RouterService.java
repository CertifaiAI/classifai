package ai.classifai.frontend.router;

import ai.classifai.core.dto.AudioDTO;
import ai.classifai.core.dto.BoundingBoxDTO;
import ai.classifai.core.dto.SegmentationDTO;
import ai.classifai.core.dto.properties.AudioProperties;
import ai.classifai.core.dto.properties.ImageProperties;
import ai.classifai.core.service.annotation.AnnotationService;
import ai.classifai.core.service.project.ProjectService;
import ai.classifai.frontend.api.ImageController;
import ai.classifai.frontend.api.ProjectController;
import com.zandero.rest.RestRouter;
import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RouterService extends AbstractVerticle {
    private final ProjectService projectService;
    private final AnnotationService<BoundingBoxDTO, ImageProperties> imageBoundingBoxService;
    private final AnnotationService<SegmentationDTO, ImageProperties> imageSegmentationService;
    private final AnnotationService<AudioDTO, AudioProperties> audioService;
    private ProjectController projectController;
    private ImageController imageController;

    public RouterService(
            ProjectService projectService,
            AnnotationService<BoundingBoxDTO, ImageProperties> imageBoundingBoxService,
            AnnotationService<SegmentationDTO, ImageProperties> imageSegmentationService,
            AnnotationService<AudioDTO, AudioProperties> audioService
    ) {
        this.projectService = projectService;
        this.imageBoundingBoxService = imageBoundingBoxService;
        this.imageSegmentationService = imageSegmentationService;
        this.audioService = audioService;
    }

    private void configureEndpoints() {
        this.projectController = new ProjectController(
                projectService,
                imageBoundingBoxService,
                imageSegmentationService,
                audioService
        );
        this.imageController = new ImageController(imageBoundingBoxService, imageSegmentationService);
    }

    private void addNoCacheHeader(RoutingContext ctx)
    {
        ctx.response().headers().add("Cache-Control", "no-cache");
        ctx.next();
    }

    @Override
    public void start() {
        configureEndpoints();

        Router router = RestRouter.register(vertx, projectController, imageController);

        router.route().handler(this::addNoCacheHeader);
        router.route().handler(StaticHandler.create());

        vertx.createHttpServer()
                .requestHandler(router)
                .exceptionHandler(Throwable::printStackTrace)
                .listen(8080, r -> {
                    log.info("listening to 8080");
                });
    }

    @Override
    public void stop() {
        log.debug("Endpoint Router Verticle stopping...");
    }
}
