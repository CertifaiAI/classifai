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
package ai.classifai.frontend.response;

import ai.classifai.backend.utility.handler.ReplyHandler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.NonNull;

/**
 * Configure response to send back to client in a more systematic manner
 *
 * @author codenamewei
 */
public class HTTPResponseHandler
{
    public static void configureOK(@NonNull RoutingContext context, JsonObject jsonObject)
    {
        context.response().setStatusCode(HTTPResponseCode.ok());
        context.response().putHeader("Content-Type", "application/json");
        context.response().end(jsonObject.encodePrettily());
    }

    public static void configureOK(@NonNull RoutingContext context)
    {
        configureOK(context, ReplyHandler.getOkReply());
    }

//    public static Future<ActionStatus> nullProjectResponse() {
//        return Future.succeededFuture(ActionStatus.failedWithMessage("Project not exist"));
//    }

}
