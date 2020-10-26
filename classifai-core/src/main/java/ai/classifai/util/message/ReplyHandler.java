/*
 * Copyright (c) 2020 CertifAI Sdn. Bhd.
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

import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

/**
 * Default reply handler for response
 *
 * @author codenamewei
 */
@Slf4j
public class ReplyHandler {

    private static final String ERROR_CODE = "errorcode";
    private static final String ERROR_MESSAGE = "errormessage";

    private static final String MESSAGE_KEY = "message";
    private static final Integer SUCCESSFUL = 1;
    private static final Integer FAILED = 0;

    public static String getMessageKey()
    {
        return MESSAGE_KEY;
    }

    public static String getErrorMesageKey()
    {
        return ERROR_MESSAGE;
    }

    public static Integer getSuccessfulSignal()
    {
        return SUCCESSFUL;
    }

    public static Integer getFailedSignal()
    {
        return FAILED;
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

    public static JsonObject reportUserDefinedError(String userDefinedMessage)
    {
        log.info(userDefinedMessage);

        return new JsonObject().put(MESSAGE_KEY, FAILED)
                .put(ERROR_CODE, ErrorCodes.OTHER_ERROR.ordinal())
                .put(ERROR_MESSAGE, userDefinedMessage);
    }

    public static JsonObject reportBadParamError(String userDefinedMessage)
    {
        log.info(userDefinedMessage);

        return new JsonObject().put(MESSAGE_KEY, FAILED)
                .put(ERROR_CODE, ErrorCodes.BAD_QUERY_PARAM.ordinal())
                .put(ERROR_MESSAGE, userDefinedMessage);
    }

    public static JsonObject reportProjectNameError(String info)
    {
        log.info(info);

        return new JsonObject().put(MESSAGE_KEY, FAILED)
                .put(ERROR_CODE, ErrorCodes.BAD_QUERY_PARAM.ordinal())
                .put(ERROR_MESSAGE, info);
    }


    public static boolean isReplyOk(JsonObject jsonObject)
    {
       return jsonObject.getInteger(MESSAGE_KEY) == SUCCESSFUL;
    }

    public static boolean isReplyFailed(JsonObject jsonObject)
    {
        return jsonObject.getInteger(MESSAGE_KEY) == FAILED;
    }

}
