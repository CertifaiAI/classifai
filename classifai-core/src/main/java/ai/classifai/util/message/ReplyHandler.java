/*
 * Copyright (c) 2020-2021 CertifAI Sdn. Bhd.
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
package ai.classifai.util.message;

import ai.classifai.util.http.HTTPResponseHandler;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

/**
 * Default reply handler for response
 *
 * @author codenamewei
 */
@Slf4j
public class ReplyHandler {

    private static final String ERROR_CODE = "error_code";
    private static final String ERROR_MESSAGE = "error_message";

    private static final String MESSAGE_KEY = "message";
    private static final Integer SUCCESSFUL = 1;
    private static final Integer FAILED = 0;

    public static String getErrorCodeKey() {return ERROR_CODE; }

    public static String getMessageKey()
    {
        return MESSAGE_KEY;
    }

    public static String getErrorMesageKey()
    {
        return ERROR_MESSAGE;
    }

    public static JsonObject reportDatabaseQueryError(Throwable cause)
    {
        log.error("Database query error", cause);

        //message.fail(ErrorCodes.DB_ERROR.ordinal(), cause.getMessage());
        return new JsonObject().put(MESSAGE_KEY, FAILED)
                               .put(ERROR_CODE, ErrorCodes.DB_ERROR.ordinal())
                               .put(ERROR_MESSAGE, cause.getMessage());
    }
    public static JsonObject getOkReply()
    {
        return new JsonObject().put(MESSAGE_KEY, SUCCESSFUL);
    }

    public static JsonObject getFailedReply()
    {
        return new JsonObject().put(MESSAGE_KEY, FAILED);
    }

    public static void sendEmptyResult(RoutingContext context, Future<Void> future, String errorMessage) {
        future.onComplete(result -> {
            if(result.succeeded()) {
                HTTPResponseHandler.configureOK(context);
            } else {
                HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError(errorMessage));
            }
        });
    }

    public static void sendEmptyResult(RoutingContext context, Future<?> future, Runnable successSideEffect,
                                  String errorMessage) {
        future.onComplete(result -> {
            if(result.succeeded()) {
                successSideEffect.run();
                HTTPResponseHandler.configureOK(context);
            } else {
                HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError(errorMessage));
            }
        });
    }

    public static void sendResult(RoutingContext context, Future<JsonObject> future, String errorMessage) {
        future.onComplete(result -> {
            if(result.succeeded()) {
                HTTPResponseHandler.configureOK(context, result.result());
            } else {
                HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError(errorMessage));
            }
        });
    }

    public static void sendResultRunSuccessSideEffect(RoutingContext context, Future<JsonObject> future,
                                                      Runnable successSideEffect, String errorMessage) {
        future.onComplete(result -> {
            if(result.succeeded()) {
                successSideEffect.run();
                HTTPResponseHandler.configureOK(context, result.result());
            } else {
                HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError(errorMessage));
            }
        });
    }

    public static void sendResultRunFailSideEffect(RoutingContext context, Future<?> future,
                                                   Runnable failSideEffect, String errorMessage) {
        future.onComplete(result -> {
            if(result.succeeded()) {
                HTTPResponseHandler.configureOK(context, ReplyHandler.getOkReply());
            } else {
                failSideEffect.run();
                HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError(errorMessage));
            }
        });
    }

    public static JsonObject reportUserDefinedError(String userDefinedMessage)
    {
        log.info(userDefinedMessage);

        return new JsonObject().put(MESSAGE_KEY, FAILED)
                .put(ERROR_CODE, ErrorCodes.USER_DEFINED_ERROR.ordinal())
                .put(ERROR_MESSAGE, userDefinedMessage);
    }

    public static JsonObject reportBadParamError(String userDefinedMessage)
    {
        log.info(userDefinedMessage);

        return new JsonObject().put(MESSAGE_KEY, FAILED)
                .put(ERROR_CODE, ErrorCodes.BAD_PARAM.ordinal())
                .put(ERROR_MESSAGE, userDefinedMessage);
    }

    public static JsonObject reportProjectNameError(String info)
    {
        log.info(info);

        return new JsonObject().put(MESSAGE_KEY, FAILED)
                .put(ERROR_CODE, ErrorCodes.BAD_PARAM.ordinal())
                .put(ERROR_MESSAGE, info);
    }


    public static boolean isReplyOk(JsonObject jsonObject)
    {
       return jsonObject.getInteger(MESSAGE_KEY) == SUCCESSFUL;
    }

}
