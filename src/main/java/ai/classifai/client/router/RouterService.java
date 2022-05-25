package ai.classifai.client.router;

import ai.classifai.client.api.ProjectController;
import com.zandero.rest.RestRouter;
import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RouterService extends AbstractVerticle {
    ProjectController projectController;

    public RouterService() {
    }

    private void configureEndpoints() {
        log.info("configure");
        this.projectController = new ProjectController();
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
//                    if (r.succeeded())
//                    {
//                        promise.complete();
//                    }
//                    else {
//                        log.debug("Failure in creating HTTPServer in ServerVerticle. " + r.cause().getMessage());
//                        promise.fail(r.cause());
//                    }
                });
    }

    @Override
    public void stop() {
        log.debug("Endpoint Router Verticle stopping...");
    }
}
