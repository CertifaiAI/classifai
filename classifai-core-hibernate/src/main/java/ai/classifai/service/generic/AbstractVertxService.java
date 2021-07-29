package ai.classifai.service.generic;

import io.vertx.core.Vertx;

/**
 * Abstract class for services
 *
 * @author YinChuangSum
 */
public abstract class AbstractVertxService
{
    protected Vertx vertx;

    public AbstractVertxService(Vertx vertx)
    {
        this.vertx = vertx;
    }
}
