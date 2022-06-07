package ai.classifai.frontend.router;

import ai.classifai.core.services.project.ProjectRepository;
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
    private ProjectController projectController;
    private ImageAnnotationController imageAnnotationController;
    private final ProjectRepository projectRepository;

    public RouterService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    private void configureEndpoints() {
        this.projectController = new ProjectController(projectRepository);
        this.imageAnnotationController = new ImageAnnotationController(projectRepository);
    }

    private void addNoCacheHeader(RoutingContext ctx)
    {
        ctx.response().headers().add("Cache-Control", "no-cache");
        ctx.next();
    }

    @Override
    public void start() {
        configureEndpoints();

        Router router = RestRouter.register(vertx, projectController);

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
