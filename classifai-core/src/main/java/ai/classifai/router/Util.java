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
package ai.classifai.router;

import ai.classifai.database.annotation.bndbox.BoundingBoxDbQuery;
import ai.classifai.database.annotation.seg.SegDbQuery;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.http.HTTPResponseHandler;
import ai.classifai.util.message.ReplyHandler;
import ai.classifai.util.type.AnnotationType;
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

    public String getDbQuery(AnnotationType type)
    {
        if(type.equals(AnnotationType.BOUNDINGBOX))
        {
            return BoundingBoxDbQuery.getQueue();
        }
        else if(type.equals(AnnotationType.SEGMENTATION))
        {
            return SegDbQuery.getQueue();
        }

        log.info("DB Query Queue not found: " + type);
        return null;
    }

    /*

    public AnnotationType getAnnotationType(String annotation)
    {
        AnnotationType type = null;

        if(annotation.equals("bndbox"))
        {
            type = AnnotationType.BOUNDINGBOX;
        }
        else if(annotation.equals("seg"))
        {
            type = AnnotationType.SEGMENTATION;
        }
        return type;
    }

    public String getValidProjectUuidQuery(AnnotationType type)
    {
        if(type.equals(AnnotationType.BOUNDINGBOX))
        {
            return BoundingBoxDbQuery.getLoadValidProjectUuid();
        }
        else if(type.equals(AnnotationType.SEGMENTATION))
        {
            return SegDbQuery.getLoadValidProjectUuid();
        }

        log.info("DB Query LoadValidProjectUuid not found: " + type);
        return null;
    }


    public String getQueryData(AnnotationType type)
    {
        if(type.equals(AnnotationType.BOUNDINGBOX))
        {
            return BoundingBoxDbQuery.getQueryData();
        }
        else if(type.equals(AnnotationType.SEGMENTATION))
        {
            return SegDbQuery.getQueryData();
        }

        log.info("DB Query QueryData not found: " + type);
        return null;
    }


    public String getRetrieveDataPathQuery(AnnotationType type)
    {
        if(type.equals(AnnotationType.BOUNDINGBOX))
        {
            return BoundingBoxDbQuery.getRetrieveDataPath();
        }
        else if(type.equals(AnnotationType.SEGMENTATION))
        {
            return SegDbQuery.getRetrieveDataPath();
        }

        log.info("DB Query RetrieveDataPath not found: " + type);
        return null;
    }

    public String getUpdateDataQuery(AnnotationType type)
    {
        if(type.equals(AnnotationType.BOUNDINGBOX))
        {
            return BoundingBoxDbQuery.getUpdateData();
        }
        else if(type.equals(AnnotationType.SEGMENTATION))
        {
            return SegDbQuery.getUpdateData();
        }

        log.info("DB Query UpdateDatadbQuery not found: " + type);
        return null;
    }

    public String getDeleteProjectQuery(AnnotationType type)
    {
        if(type.equals(AnnotationType.BOUNDINGBOX))
        {
            return BoundingBoxDbQuery.getDeleteProject();
        }
        else if(type.equals(AnnotationType.SEGMENTATION))
        {
            return SegDbQuery.getDeleteProject();
        }

        log.info("DB Query DeleteProject not found: " + type);
        return null;
    }

    public String getDeleteSelectionUuidListQuery(AnnotationType type)
    {
        if(type.equals(AnnotationType.BOUNDINGBOX))
        {
            return BoundingBoxDbQuery.getDeleteSelectionUuidList();
        }
        else if(type.equals(AnnotationType.SEGMENTATION))
        {
            return SegDbQuery.getDeleteSelectionUuidList();
        }

        log.info("DB Query DeleteSelectionUuidList not found: " + type);
        return null;
    }
    */


}
