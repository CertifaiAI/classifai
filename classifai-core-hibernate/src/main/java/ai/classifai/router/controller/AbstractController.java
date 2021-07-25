package ai.classifai.router.controller;

import ai.classifai.router.ParamHandler;
import ai.classifai.router.Util;
import ai.classifai.selector.status.FileSystemStatus;
import ai.classifai.selector.status.SelectionWindowStatus;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.http.HTTPResponseHandler;
import ai.classifai.util.message.ReplyHandler;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public abstract class AbstractController
{
    protected ParamHandler paramHandler = new ParamHandler();

    protected Util helper = new Util();

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
        return throwable -> HTTPResponseHandler.configureOK(context,
                ReplyHandler.reportUserDefinedError(throwable.getMessage()));
    }

    protected DeliveryOptions getDeliveryOptions(String action)
    {
        return new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), action);
    }
}
