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

/**
 * Abstract class for controllers that handles APIs
 *
 * @author YinChuangSum
 */
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
