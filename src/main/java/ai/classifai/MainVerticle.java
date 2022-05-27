package ai.classifai;

import ai.classifai.client.router.RouterService;
import ai.classifai.repository.JdbcHolder;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MainVerticle extends AbstractVerticle {
    private final RouterService routerService;

    MainVerticle() {
        this.routerService = new RouterService();
    }

    @Override
    public void start(Promise<Void> promise) {
        Promise<String> deploy = Promise.promise();

        vertx.deployVerticle(routerService, deploy);

        deploy.future().onComplete(res -> {
            JdbcHolder.init(vertx);

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
        JdbcHolder.stop();

        vertx.close(voidAsyncResult -> {
            log.info("verticle close");
        });
    }
}
