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
package ai.classifai.action;

import ai.classifai.loader.ProjectLoader;
import ai.classifai.util.datetime.DateTime;
import ai.classifai.util.project.ProjectHandler;
import io.vertx.core.json.JsonObject;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Export of project to a configuration file
 *
 * @author codenamewei
 */
@Builder
@Slf4j
@NoArgsConstructor
public class ProjectExport
{
    public JsonObject getConfigSkeletonStructure()
    {
        JsonObject jsonObject = new JsonObject()
                .put(ActionConfig.getToolParam(), ActionConfig.getToolName())
                .put(ActionConfig.getToolVersionParam(), ActionConfig.getToolVersion())
                .put(ActionConfig.getUpdatedDateParam(), new DateTime().toString());

        return jsonObject;
    }

    public String exportToFile(@NonNull String projectId, @NonNull JsonObject jsonObject)
    {
        ProjectLoader loader = ProjectHandler.getProjectLoader(projectId);

        //Configuration file of json format
        String configPath = loader.getProjectPath() + File.separator + loader.getProjectName() + ".json";

        try
        {
            FileWriter file = new FileWriter(configPath);

            file.write(jsonObject.encodePrettily());

            file.close();

            log.info("Project configuration file saved at: " + configPath);
        }
        catch (IOException e)
        {
            return "Path cannot be provided due to failed configuration: " + e;
        }

        return configPath;
    }
}
