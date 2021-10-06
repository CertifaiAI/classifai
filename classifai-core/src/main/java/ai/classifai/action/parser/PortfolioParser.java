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
import ai.classifai.dto.data.ProjectConfigProperties;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.loader.ProjectLoaderStatus;
import ai.classifai.util.project.ProjectInfra;
import ai.classifai.util.project.ProjectInfraHandler;
import ai.classifai.util.type.AnnotationHandler;
import io.vertx.sqlclient.Row;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;


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
     */
    public static ProjectConfigProperties parseOut(@NonNull Row row)
    {
        String annotationName = AnnotationHandler.getType(row.getInteger(2)).name();

        return ProjectConfigProperties.builder()
                .projectID(row.getString(0))
                .projectName(row.getString(1))
                .annotationType(annotationName.toLowerCase(Locale.ROOT))
                .projectPath(row.getString(3))
                .isNew(row.getBoolean(4))
                .isStarred(row.getBoolean(5))
                .projectInfra(row.getString(6).toLowerCase())
                .currentVersion(row.getString(7))
                .projectVersion(row.getString(8))
                .uuidVersionList(row.getString(9))
                .labelVersionList(row.getString(10))
                .build();

    }

    public static ProjectLoader parseIn(@NonNull ProjectConfigProperties projectConfig)
    {
        //annotation_type (string -> int)
        String annotation = projectConfig.getAnnotationType();
        int annotationInt = Objects.requireNonNull(AnnotationHandler.getType(annotation)).ordinal();

        ProjectVersion project = loadProjectVersion(projectConfig.getProjectVersion());

        Version currentVersion = new Version(projectConfig.getCurrentVersion());
        project.setCurrentVersion(currentVersion.getVersionUuid());

        Map<String, List<String>> uuidDict = ActionOps.getKeyWithArray(projectConfig.getUuidVersionList());
        project.setUuidListDict(uuidDict);                                                                          //uuid_version_list

        Map<String, List<String>> labelDict = ActionOps.getKeyWithArray(projectConfig.getLabelVersionList());
        project.setLabelListDict(labelDict);                                                                        //label_version_list


        ProjectInfra projectInfra = ProjectInfraHandler.getInfra(projectConfig.getProjectInfra());
        return ProjectLoader.builder()
                                .projectId(projectConfig.getProjectID())               //project_id
                                .projectName(projectConfig.getProjectName())           //project_name
                                .annotationType(annotationInt)                                                  //annotation_type
                                .projectPath(new File(ActionConfig.getJsonFilePath()))                          //project_path
                                .isProjectStarred(projectConfig.getIsStarred())       //is_starred

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
