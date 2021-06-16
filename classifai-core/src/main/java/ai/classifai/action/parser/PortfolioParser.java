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

import ai.classifai.action.ActionConfig;
import ai.classifai.action.ActionOps;
import ai.classifai.database.versioning.ProjectVersion;
import ai.classifai.database.versioning.Version;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.loader.ProjectLoaderStatus;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.project.ProjectInfra;
import ai.classifai.util.project.ProjectInfraHandler;
import ai.classifai.util.type.AnnotationHandler;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Locale;
import java.util.Map;


/***
 * Parsing Portfolio Table in and out classifai with configuration file
 *
 * @author codenamewei
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PortfolioParser
{
    /**
     * Extracting portfolio content and parse to JsonObject, preparing for saving
     * @param row
     * @param jsonObject
     */
    public static void parseOut(@NonNull Row row, @NonNull JsonObject jsonObject)
    {
        String annotationName = AnnotationHandler.getType(row.getInteger(2)).name();

        jsonObject.put(ParamConfig.getProjectIdParam(), row.getString(0));                                  //project_id
        jsonObject.put(ParamConfig.getProjectNameParam(), row.getString(1));                                //project_name
        jsonObject.put(ParamConfig.getAnnotationTypeParam(), annotationName.toLowerCase(Locale.ROOT));           //annotation_type (in string)

        jsonObject.put(ParamConfig.getProjectPathParam(), row.getString(3));                                //project_path
        jsonObject.put(ParamConfig.getIsNewParam(), row.getBoolean(4));                                     //is_new
        jsonObject.put(ParamConfig.getIsStarredParam(), row.getBoolean(5));                                 //is_starred

        jsonObject.put(ParamConfig.getProjectInfraParam(), row.getString(6).toLowerCase());                 //project_infra
        jsonObject.put(ParamConfig.getCurrentVersionParam(), row.getString(7));                             //current version
        jsonObject.put(ParamConfig.getProjectVersionParam(), row.getString(8));                             //project version
        jsonObject.put(ParamConfig.getUuidVersionListParam(), row.getString(9));                            //uuid_version_list
        jsonObject.put(ParamConfig.getLabelVersionListParam(), row.getString(10));                          //label_version_list
    }

    public static ProjectLoader parseIn(@NonNull JsonObject jsonObject)
    {
        //annotation_type (string -> int)
        String annotation = jsonObject.getString(ParamConfig.getAnnotationTypeParam());
        int annotationInt = AnnotationHandler.getType(annotation).ordinal();

        ProjectVersion project = loadProjectVersion(jsonObject.getString(ParamConfig.getProjectVersionParam()));

        Version currentVersion = new Version(jsonObject.getString(ParamConfig.getCurrentVersionParam()));
        project.setCurrentVersion(currentVersion.getVersionUuid());

        Map uuidDict = ActionOps.getKeyWithArray(jsonObject.getString(ParamConfig.getUuidVersionListParam()));
        project.setUuidListDict(uuidDict);                                                                          //uuid_version_list

        Map labelDict = ActionOps.getKeyWithArray(jsonObject.getString(ParamConfig.getLabelVersionListParam()));
        project.setLabelListDict(labelDict);                                                                        //label_version_list


        ProjectInfra projectInfra = ProjectInfraHandler.getInfra(jsonObject.getString(ParamConfig.getProjectInfraParam()));
        return ProjectLoader.builder()
                                .projectId(jsonObject.getString(ParamConfig.getProjectIdParam()))               //project_id
                                .projectName(jsonObject.getString(ParamConfig.getProjectNameParam()))           //project_name
                                .annotationType(annotationInt)                                                  //annotation_type
                                .projectPath(new File(ActionConfig.getJsonFilePath()))                          //project_path
                                .isProjectNew(jsonObject.getBoolean(ParamConfig.getIsNewParam()))               //is_new
                                .isProjectStarred(jsonObject.getBoolean(ParamConfig.getIsStarredParam()))       //is_starred

                                .projectInfra(projectInfra)                                                     //project_infra

                                .projectLoaderStatus(ProjectLoaderStatus.DID_NOT_INITIATED)

                                .projectVersion(project)                                                        //project_version
                                .build();
    }

    public static ProjectVersion loadProjectVersion(@NonNull String input)
    {
        ProjectVersion project = new ProjectVersion(false);

        String[] strVersionArrays = ActionOps.splitStringByJsonSplitter(input);

        for(String strVersion : strVersionArrays)
        {
            Version version = new Version(strVersion);

            project.setVersion(version);
        }

        return project;
    }
}
