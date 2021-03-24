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

import ai.classifai.database.portfolio.PortfolioVerticle;
import io.vertx.core.json.JsonObject;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import javax.swing.*;
import java.io.File;
import java.io.FileReader;

import static javax.swing.JOptionPane.showMessageDialog;

/**
 * Import of project from configuration file
 *
 * @author codenamewei
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProjectImport
{
    public static void importProjectFile(@NonNull File jsonFile)
    {
        try
        {
            String jsonStr = IOUtils.toString(new FileReader(jsonFile));

            JsonObject inputJsonObject = new JsonObject(jsonStr);

            if(!checkToolVersion(inputJsonObject) || !checkJsonKeys(inputJsonObject))
            {
                return;
            }

            PortfolioVerticle.loadProjectFromImportingConfigFile(inputJsonObject);

        }
        catch(Exception e)
        {
            log.info("Error in importing project. ", e);
        }
    }

    public static boolean checkJsonKeys(JsonObject inputJsonObject)
    {
        for(String key: ActionConfig.getKeys())
        {
            if(!inputJsonObject.containsKey(key)) {
                String message = "Project not imported. Missing Key in JSON file: " + key;
                log.info(message);
                showMessageDialog(null,
                        message,
                        "Import Error", JOptionPane.ERROR_MESSAGE);

                return false;
            }
        }
        return true;
    }

    public static boolean checkToolVersion(JsonObject inputJsonObject)
    {
        String toolNameFromJson = inputJsonObject.getString(ActionConfig.getToolParam());
        String toolVersionFromJson = inputJsonObject.getString(ActionConfig.getToolVersionParam());
        String updatedDateFromJson = inputJsonObject.getString(ActionConfig.getUpdatedDateParam());

        if(toolNameFromJson == null || toolVersionFromJson == null || updatedDateFromJson == null || !toolNameFromJson.equals(ActionConfig.getToolName()))
        {
            String message = "The configuration file imported is not valid.";
            log.info(message);
            showMessageDialog(null,
                    message,
                    "Invalid import file", JOptionPane.ERROR_MESSAGE);

            return false;
        }

        // Does not return false. Only pop up warning. Further checking of JSON file is done after.
        if(!toolVersionFromJson.equals(ActionConfig.getToolVersion()))
        {
            String message = "Different tool version detected. Import may not work." + "\n\nInstalled Version: " + ActionConfig.getToolVersion() +
                    "\nJSON Version: " + toolVersionFromJson;
            log.info(message);
            showMessageDialog(null,
                    message,
                    "Tool Version Warning", JOptionPane.WARNING_MESSAGE);
        }
        return true;
    }

}