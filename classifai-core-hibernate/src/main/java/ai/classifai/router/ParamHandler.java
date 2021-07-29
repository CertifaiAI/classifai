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

import ai.classifai.util.ParamConfig;
import ai.classifai.util.type.AnnotationHandler;
import ai.classifai.util.type.AnnotationType;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.UUID;

/**
 * Class to store boiler plate code for getting specific param from routing context
 *
 * @author YinChuangSum
 */
public class ParamHandler {
    public ParamHandler() {}

    public String getNewProjectName(RoutingContext context)
    {
        return context.request().getParam(ParamConfig.getNewProjectNameParam());
    }

    public UUID getDataId(RoutingContext context)
    {
        return UUID.fromString(context.request().getParam(ParamConfig.getUuidParam()));
    }

    public AnnotationType getAnnotationType(RoutingContext context)
    {
        return AnnotationHandler.getTypeFromEndpoint(context.request()
                .getParam(ParamConfig.getAnnotationTypeParam()));
    }

    public String getProjectName(RoutingContext context)
    {
        return context.request().getParam(ParamConfig.getProjectNameParam());
    }
}
