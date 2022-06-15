/*
 * Copyright (c) 2021 CertifAI Sdn. Bhd.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.classifai.backend.repository.database;

import ai.classifai.backend.utility.handler.ReplyHandler;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import lombok.NonNull;

import java.util.function.Consumer;

/**
 * Db response handler
 *
 * @author devenyantis
 */
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

    public static Void toVoid(Object object) {
        return null;
    }
}
