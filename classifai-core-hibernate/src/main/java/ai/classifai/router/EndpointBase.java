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
package ai.classifai.router;

import ai.classifai.selector.status.FileSystemStatus;
import ai.classifai.selector.status.SelectionWindowStatus;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.http.HTTPResponseHandler;
import ai.classifai.util.message.ReplyHandler;
import ai.classifai.util.type.AnnotationHandler;
import ai.classifai.util.type.AnnotationType;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.Setter;

/**
 * Endpoint base functions
 *
 * @author codenamewei
 */
public abstract class EndpointBase
{
    protected ParamHandler paramHandler = new ParamHandler();
//    protected BodyHandler bodyHandler = new BodyHandler();

    @Setter protected Vertx vertx = null;

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

    protected void sendResponseBody(Message<Object> msg, RoutingContext context)
    {
        JsonObject response = (JsonObject) msg.body();

        HTTPResponseHandler.configureOK(context, response);
    }

    protected DeliveryOptions getDeliveryOptions(String action)
    {
        return new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), action);
    }
}
