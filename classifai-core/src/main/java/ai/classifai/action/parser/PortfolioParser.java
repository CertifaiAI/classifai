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
package ai.classifai.action.parser;

import ai.classifai.database.annotation.bndbox.BoundingBoxVerticle;
import ai.classifai.database.annotation.seg.SegVerticle;
import ai.classifai.database.portfolio.PortfolioVerticle;
import ai.classifai.loader.LoaderStatus;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.ProjectHandler;
import ai.classifai.util.data.StringHandler;
import ai.classifai.util.type.AnnotationType;
import ai.classifai.util.versioning.ProjectVersion;
import ai.classifai.util.versioning.VersionCollection;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;


/***
 * Parsing Portfolio Table in and out classifai with configuration file
 *
 * @author codenamewei
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PortfolioParser
{
    public static void parseOut(@NonNull Row inputRow, @NonNull JsonObject jsonObject)
    {
        jsonObject.put(ParamConfig.getProjectIdParam(), inputRow.getString(0));
        jsonObject.put(ParamConfig.getProjectNameParam(), inputRow.getString(1));
        jsonObject.put(ParamConfig.getAnnotationTypeParam(), inputRow.getInteger(2));

        jsonObject.put(ParamConfig.getProjectPathParam(), inputRow.getString(3));
        jsonObject.put(ParamConfig.getIsNewParam(), inputRow.getBoolean(4));
        jsonObject.put(ParamConfig.getIsStarredParam(), inputRow.getBoolean(5));

        jsonObject.put(ParamConfig.getCurrentVersionUuidParam(), inputRow.getString(6));
        jsonObject.put(ParamConfig.getVersionListParam(), StringHandler.cleanUpRegex(inputRow.getString(7), Arrays.asList("\"")));
        jsonObject.put(ParamConfig.getUuidVersionListParam(), StringHandler.cleanUpRegex(inputRow.getString(8), Arrays.asList("\"")));
        jsonObject.put(ParamConfig.getLabelVersionListParam(), StringHandler.cleanUpRegex(inputRow.getString(9), Arrays.asList("\"")));
    }

    public static ProjectLoader parseIn(@NonNull JsonObject jsonObject)
    {
        VersionCollection versionCollector = new VersionCollection(jsonObject.getString(ParamConfig.getVersionListParam()));
        ProjectVersion projVersion = versionCollector.getVersionUuidDict().get(jsonObject.getString(ParamConfig.getCurrentVersionUuidParam()));

        versionCollector.setUuidDict(jsonObject.getString(ParamConfig.getUuidVersionListParam()));
        versionCollector.setLabelDict(jsonObject.getString(ParamConfig.getLabelVersionListParam()));

        return new ProjectLoader.Builder()
                                .projectID(jsonObject.getString(ParamConfig.getProjectIdParam()))
                                .projectName(jsonObject.getString(ParamConfig.getProjectNameParam()))
                                .annotationType(jsonObject.getInteger(ParamConfig.getAnnotationTypeParam()))

                                .projectPath(jsonObject.getString(ParamConfig.getProjectPathParam()))
                                .isProjectNew(jsonObject.getBoolean(ParamConfig.getIsNewParam()))
                                .isProjectStarred(jsonObject.getBoolean(ParamConfig.getIsStarredParam()))

                                .loaderStatus(LoaderStatus.DID_NOT_INITIATED)

                                .currentProjectVersion(projVersion)
                                .versionCollection(versionCollector)

                                .build();


    }
}
