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

import ai.classifai.core.entity.model.generic.Label;
import ai.classifai.core.entity.model.generic.Version;
import ai.classifai.database.DbService;
import ai.classifai.service.generic.LabelService;
import ai.classifai.util.type.AnnotationType;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Class to handle Label APIs
 *
 * @author YinChuangSum
 */
public class LabelController extends AbstractVertxController
{
    private LabelService labelService;
    private DbService dbService;

    public LabelController(Vertx vertx, DbService dbService, LabelService labelService)
    {
        super(vertx);
        this.dbService = dbService;
        this.labelService = labelService;
    }

    /***
     *
     * Update labels
     *
     * PUT http://localhost:{port}/:annotation_type/projects/:project_name/newlabels
     *
     */
    // FIXME: temporarily code for current frontend
    public void updateLabels(RoutingContext context)
    {
        String projectName = paramHandler.getProjectName(context);
        AnnotationType annotationType = paramHandler.getAnnotationType(context);

        context.request().bodyHandler(buffer ->
        {
            String newLabelListStr = buffer
                    .toJsonObject()
                    .getString("label_list");

            List<String> newLabelList = labelService.getLabelStringListFromString(newLabelListStr);

            Future<Version> getCurrentVersionFuture = dbService.getProjectByNameAndAnnotation(projectName, annotationType)
                    .compose(project -> Future.succeededFuture(project.getCurrentVersion()));

            Future<List<Label>> getCurrentVersionLabelListFuture = getCurrentVersionFuture
                    .compose(version -> Future.succeededFuture(version.getLabelList()));

            Future<Void> deleteLabelListFuture = getCurrentVersionLabelListFuture
                    .compose(labelList ->
                            labelService.getToDeleteLabelListFuture(labelList, newLabelList))
                    .compose(dbService::deleteLabelList);

            Future<Void> addLabelListFuture = getCurrentVersionLabelListFuture
                    .compose(labelList ->
                            labelService.getToAddLabelDTOListFuture(labelList, newLabelList))
                    .compose(labelDTOList ->
                    {
                        labelDTOList.forEach(labelDTO ->
                                labelDTO.setVersionId(getCurrentVersionFuture.result().getId()));

                        return Future.succeededFuture(labelDTOList);
                    })
                    .compose(dbService::addLabelList);

            addLabelListFuture
                    .compose(unused -> deleteLabelListFuture)
                    .onSuccess(unused -> sendEmptyResponse(context))
                    .onFailure(failedRequestHandler(context));
        });
    }
}
