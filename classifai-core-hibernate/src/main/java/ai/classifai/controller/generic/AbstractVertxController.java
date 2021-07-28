package ai.classifai.controller.generic;

import ai.classifai.router.ParamHandler;
import ai.classifai.router.Util;
import ai.classifai.selector.status.FileSystemStatus;
import ai.classifai.selector.status.SelectionWindowStatus;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.http.HTTPResponseHandler;
import ai.classifai.util.message.ReplyHandler;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
public abstract class AbstractVertxController
{
    protected Vertx vertx;
    protected ParamHandler paramHandler;
    protected Util helper;

    public AbstractVertxController(Vertx vertx)
    {
        this.vertx = vertx;
        this.paramHandler = new ParamHandler();
        helper = new Util();
    }

    public JsonObject compileFileSysStatusResponse(FileSystemStatus status)
    {
        JsonObject response = ReplyHandler.getOkReply();

        response.put(ParamConfig.getFileSysStatusParam(), status.ordinal())
                .put(ParamConfig.getFileSysMessageParam(), status.name());

        return response;
    }

    public JsonObject compileSelectionWindowResponse(SelectionWindowStatus status)
    {
        JsonObject response = ReplyHandler.getOkReply();

        response.put(ParamConfig.getSelectionWindowStatusParam(), status.ordinal())
                .put(ParamConfig.getSelectionWindowMessageParam(), status.name());

        return response;
    }

    protected void sendEmptyResponse(RoutingContext context)
    {
        HTTPResponseHandler.configureOK(context);
    }

    protected void sendResponseBody(JsonObject response, RoutingContext context)
    {
        HTTPResponseHandler.configureOK(context, response);
    }

    protected Handler<Throwable> failedRequestHandler(RoutingContext context)
    {
        return throwable ->
        {
            String stackTrace = Arrays.toString(throwable.getStackTrace());
            log.info(stackTrace);
            HTTPResponseHandler.configureOK(context,
                    ReplyHandler.reportUserDefinedError(Arrays.toString(throwable.getStackTrace())));
        };
    }
}
