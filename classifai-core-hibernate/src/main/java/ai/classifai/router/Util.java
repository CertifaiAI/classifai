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
import ai.classifai.util.http.HTTPResponseHandler;
import ai.classifai.util.message.ReplyHandler;
import io.vertx.ext.web.RoutingContext;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility functions for router
 *
 * @author devenyantis
 */
@Slf4j
public class Util {

    public boolean checkIfProjectNull(RoutingContext context, Object project, @NonNull String projectName)
    {
        if(project == null)
        {
            HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Project not found: " + projectName));

            return true;
        }

        return false;
    }

    public void checkIfDockerEnv(RoutingContext context)
    {
        if(ParamConfig.isDockerEnv())
        {
            HTTPResponseHandler.configureOK(context);
        }
    }

//    public String getDbQuery(AnnotationType type)
//    {
//        if(type.equals(AnnotationType.BOUNDINGBOX))
//        {
//            return BoundingBoxDbQuery.getQueue();
//        }
//        else if(type.equals(AnnotationType.SEGMENTATION))
//        {
//            return SegDbQuery.getQueue();
//        }
//
//        log.info("DB Query Queue not found: " + type);
//        return null;
//    }


}
