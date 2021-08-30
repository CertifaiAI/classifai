package ai.classifai.database;

import ai.classifai.util.message.ReplyHandler;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import lombok.NonNull;

import java.util.function.Consumer;

public class DBUtils {

    private DBUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static Handler<AsyncResult<RowSet<Row>>> handleEmptyResponse(@NonNull Message<JsonObject> message) {
        return handleEmptyResponse(message, () -> message.replyAndRequest(ReplyHandler.getOkReply()));
    }

    public static Handler<AsyncResult<RowSet<Row>>> handleEmptyResponse(@NonNull Message<JsonObject> message,
                                                                        @NonNull Runnable successSideEffect) {
        return handleEmptyResponse(successSideEffect, cause ->
                message.replyAndRequest(ReplyHandler.reportDatabaseQueryError(cause)));
    }

    public static Handler<AsyncResult<RowSet<Row>>> handleEmptyResponse(@NonNull Runnable successSideEffect,
                                                                        @NonNull Consumer<Throwable> failureSideEffect)
    {
        return fetch -> {
            if (fetch.succeeded()) {
                successSideEffect.run();
            } else {
                failureSideEffect.accept(fetch.cause());
            }
        };
    }

    public static Handler<AsyncResult<RowSet<Row>>> handleResponse(@NonNull Message<JsonObject> message,
                                                                   @NonNull Consumer<RowSet<Row>> successSideEffect)
    {

        return handleResponse(successSideEffect, cause ->
                message.replyAndRequest(ReplyHandler.reportDatabaseQueryError(cause)));
    }

    public static Handler<AsyncResult<RowSet<Row>>> handleResponse(@NonNull Consumer<RowSet<Row>> successSideEffect,
                                                                   @NonNull Consumer<Throwable> failureSideEffect)
    {
        return fetch -> {
            if (fetch.succeeded()) {
                successSideEffect.accept(fetch.result());
            } else {
                failureSideEffect.accept(fetch.cause());
            }
        };
    }

}
