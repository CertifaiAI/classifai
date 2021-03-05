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
import ai.classifai.util.ProjectHandler;
import ai.classifai.util.datetime.DateTime;
import io.vertx.core.json.JsonObject;
import lombok.Builder;
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
public class ProjectExport
{
    public static JsonObject getDefaultJsonObject()
    {
        JsonObject jsonObject = new JsonObject()
                .put(ActionConfig.getToolParam(), "classifai")
                .put(ActionConfig.getToolVersionParam(), "2.0.0-alpha") //FIXME: dont hardcode
                .put(ActionConfig.getUpdatedDateParam(), new DateTime().toString());


        return jsonObject;
    }

    public static boolean exportToFile(@NonNull File jsonPath, @NonNull JsonObject jsonObject)
    {
        try {
            FileWriter file = new FileWriter(jsonPath);

            file.write(jsonObject.encodePrettily());

            file.close();

        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }

        log.info("Project configuration file saved at: " + jsonPath);

        return true;
    }

    public static String getProjectExportPath(@NonNull String projectId)
    {
        ProjectLoader loader = (ProjectLoader) ProjectHandler.getProjectLoader(projectId);

        return loader.getProjectPath() + File.separator + loader.getProjectName() + ".json";
    }
}
