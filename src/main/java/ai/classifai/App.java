package ai.classifai;

import ai.classifai.core.MainVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class App
{
    public static void main( String[] args )
    {
        VertxOptions vertxOptions = new VertxOptions();

        vertxOptions.setMaxEventLoopExecuteTimeUnit(TimeUnit.SECONDS);
        vertxOptions.setMaxEventLoopExecuteTime(15);

        DeploymentOptions opt = new DeploymentOptions();
        opt.setWorker(true);

        Vertx vertx = Vertx.vertx(vertxOptions);
        vertx.deployVerticle(new MainVerticle(vertx), opt);
    }
}
