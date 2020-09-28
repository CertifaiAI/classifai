/*
 * Copyright (c) 2020 CertifAI
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
package ai.classifai.util.http;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.NonNull;

/**
 * Configure response to send back to client in a more systematic manner
 *
 * @author Chiawei Lim
 */
public class HTTPResponseHandler
{
    public static void configureOK(@NonNull RoutingContext context, JsonObject jsonObject)
    {
        context.response().setStatusCode(HTTPResponseCode.ok());
        context.response().putHeader("Content-Type", "application/json");
        context.response().end(jsonObject.encodePrettily());
    }

}
