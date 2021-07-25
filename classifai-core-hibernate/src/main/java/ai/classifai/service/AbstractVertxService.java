package ai.classifai.service;

import io.vertx.core.Vertx;

public class AbstractVertxService
{
    protected Vertx vertx;

    public AbstractVertxService(Vertx vertx)
    {
        this.vertx = vertx;
    }
}
