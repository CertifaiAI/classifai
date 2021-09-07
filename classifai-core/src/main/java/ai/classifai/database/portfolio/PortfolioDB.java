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
package ai.classifai.database.portfolio;

import ai.classifai.action.ActionConfig;
import ai.classifai.database.annotation.AnnotationQuery;
import ai.classifai.util.ParamConfig;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

/**
 * To run database query
 *
 * @author devenyantis
 */
@Slf4j
public class PortfolioDB {
    private final EventBus eventBus;

    public PortfolioDB(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    private Future<JsonObject> runQuery(JsonObject msg, String action, String annotationQueue) {
        DeliveryOptions options = new DeliveryOptions()
                .addHeader(ParamConfig.getActionKeyword(), action);

        final Promise<JsonObject> promise = Promise.promise();
        eventBus.request(annotationQueue, msg, options, fetch -> {
            JsonObject response = (JsonObject) fetch.result().body();
            promise.complete(response);
        });
        return promise.future();
    }

    private Future<JsonObject> runQuery(JsonObject msg, String action) {
        return runQuery(msg, action, PortfolioDbQuery.getQueue());
    }

    public Future<JsonObject> renameProject(String projectId, String newProjectName) {
        JsonObject msg = new JsonObject()
                .put(ParamConfig.getProjectIdParam(), projectId)
                .put(ParamConfig.getNewProjectNameParam(), newProjectName);

        return runQuery(msg, PortfolioDbQuery.getRenameProject());
    }

    public Future<JsonObject> exportProject(String projectId, int annotationType, int exportType) {
        JsonObject msg = new JsonObject()
                .put(ParamConfig.getProjectIdParam(), projectId)
                .put(ParamConfig.getAnnotationTypeParam(), annotationType)
                .put(ActionConfig.getExportTypeParam(), exportType);
        
        return runQuery(msg, PortfolioDbQuery.getExportProject());
    }

    public Future<JsonObject> deleteProjectData(String projectId, String annotationQueue,
                                                JsonArray uuidListArray, JsonArray uuidImgPathList) {
        JsonObject msg = new JsonObject()
                .put(ParamConfig.getProjectIdParam(), projectId)
                .put(ParamConfig.getUuidListParam(), uuidListArray)
                .put(ParamConfig.getImgPathListParam(), uuidImgPathList);
        
        return runQuery(msg, AnnotationQuery.getDeleteProjectData(), annotationQueue);
    }

    public Future<JsonObject> renameData(String projectId, String annotationQueue, String uuid, String newFilename) {
        JsonObject msg = new JsonObject()
                .put(ParamConfig.getProjectIdParam(), projectId)
                .put(ParamConfig.getUuidParam(), uuid)
                .put(ParamConfig.getNewFileNameParam(), newFilename);

        return runQuery(msg, AnnotationQuery.getRenameProjectData(), annotationQueue);
    }

    public Future<JsonObject> startProject(String projectId, Boolean isStarred) {
        JsonObject msg = new JsonObject()
                .put(ParamConfig.getStatusParam(), isStarred)
                .put(ParamConfig.getProjectIdParam(), projectId);
        
        return runQuery(msg, PortfolioDbQuery.getStarProject());
    }

    public Future<JsonObject> reloadProject(String projectId) {
        JsonObject msg = new JsonObject()
                .put(ParamConfig.getProjectIdParam(), projectId);
        
        return runQuery(msg, PortfolioDbQuery.getReloadProject());
    }

    public Future<JsonObject> getProjectMetadata(String projectId) {
        JsonObject msg = new JsonObject()
                .put(ParamConfig.getProjectIdParam(), projectId);
        
        return runQuery(msg, PortfolioDbQuery.getRetrieveProjectMetadata());
    }

    public Future<JsonObject> getAllProjectsMeta(int annotationType) {
        JsonObject msg = new JsonObject()
                .put(ParamConfig.getAnnotationTypeParam(), annotationType);
        
        return runQuery(msg, PortfolioDbQuery.getRetrieveAllProjectsMetadata());
    }

    public Future<JsonObject> loadProject(String projectId, String annotationQueue) {
        JsonObject msg = new JsonObject().put(ParamConfig.getProjectIdParam(), projectId);
        
        return runQuery(msg, AnnotationQuery.getLoadValidProjectUuid(), annotationQueue);
    }

    public Future<JsonObject> getThumbnail(String projectID, String annotationQueue, String uuid) {
        JsonObject msg = new JsonObject()
                .put(ParamConfig.getUuidParam(), uuid)
                .put(ParamConfig.getProjectIdParam(), projectID);
        
        return runQuery(msg, AnnotationQuery.getQueryData(), annotationQueue);
    }

    public Future<JsonObject> getImageSource(String projectID, String annotationQueue, 
                                             String uuid, String projectName) {
        JsonObject msg = new JsonObject()
                .put(ParamConfig.getUuidParam(), uuid)
                .put(ParamConfig.getProjectIdParam(), projectID)
                .put(ParamConfig.getProjectNameParam(), projectName);
        
        return runQuery(msg, AnnotationQuery.getRetrieveDataPath(), annotationQueue);
    }

    public Future<JsonObject> updateData(String projectID, String annotationQueue, JsonObject requestBody) {
        requestBody.put(ParamConfig.getProjectIdParam(), projectID);
        
        return runQuery(requestBody, AnnotationQuery.getUpdateData(), annotationQueue);
    }

    public Future<JsonObject> updateLastModifiedDate(String projectID, String dbFormat) {
        JsonObject msg = new JsonObject()
                .put(ParamConfig.getProjectIdParam(), projectID)
                .put(ParamConfig.getCurrentVersionParam(), dbFormat);
        
        return runQuery(msg, PortfolioDbQuery.getUpdateLastModifiedDate());
    }

    public Future<JsonObject> updateLabels(String projectID, JsonObject requestBody) {
        requestBody.put(ParamConfig.getProjectIdParam(), projectID);
        
        return runQuery(requestBody, PortfolioDbQuery.getUpdateLabelList());
    }

    public Future<JsonObject> deleteProjectFromPortfolioDb(String projectID) {
        JsonObject msg = new JsonObject()
                .put(ParamConfig.getProjectIdParam(), projectID);

        return runQuery(msg, PortfolioDbQuery.getDeleteProject());
    }

    public Future<JsonObject> deleteProjectFromAnnotationDb(String projectId, String annotationQuery) {
        JsonObject msg = new JsonObject()
                .put(ParamConfig.getProjectIdParam(), projectId);

        return runQuery(msg, AnnotationQuery.getDeleteProject(),annotationQuery);
    }
}
