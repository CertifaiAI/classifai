package ai.classifai.frontend.router;

import ai.classifai.core.dto.BoundingBoxDTO;
import ai.classifai.core.dto.SegmentationDTO;
import ai.classifai.core.dto.properties.BoundingBoxProperties;
import ai.classifai.core.dto.properties.ImageProperties;
import ai.classifai.core.dto.properties.SegmentationProperties;
import ai.classifai.core.service.annotation.AnnotationService;
import ai.classifai.core.service.project.ProjectService;
import ai.classifai.frontend.api.ImageAnnotationController;
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
    private ProjectController projectController;
    private ImageAnnotationController imageAnnotationController;

    public RouterService(
            ProjectService projectService,
            AnnotationService<BoundingBoxDTO, ImageProperties> imageBoundingBoxService,
            AnnotationService<SegmentationDTO, ImageProperties> imageSegmentationService
    ) {
        this.projectService = projectService;
        this.imageBoundingBoxService = imageBoundingBoxService;
        this.imageSegmentationService = imageSegmentationService;
    }

    private void configureEndpoints() {
        this.projectController = new ProjectController(projectService, imageBoundingBoxService, imageSegmentationService);
        this.imageAnnotationController = new ImageAnnotationController(imageBoundingBoxService, imageSegmentationService);
    }

    private void addNoCacheHeader(RoutingContext ctx)
    {
        ctx.response().headers().add("Cache-Control", "no-cache");
        ctx.next();
    }

    @Override
    public void start() {
        configureEndpoints();

        Router router = RestRouter.register(vertx, projectController, imageAnnotationController);

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
