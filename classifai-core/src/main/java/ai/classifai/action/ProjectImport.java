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
import ai.classifai.selector.project.ProjectImportSelector;
import ai.classifai.ui.SelectionWindow;
import ai.classifai.util.ParamConfig;
import io.vertx.core.json.JsonObject;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import javax.swing.*;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;

/**
 * Import of project from configuration file
 *
 * @author codenamewei
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProjectImport
{
    public static boolean importProjectFile(@NonNull File jsonFile)
    {
        try
        {
            String jsonStr = IOUtils.toString(new FileReader(jsonFile));

            JsonObject inputJsonObject = new JsonObject(jsonStr);

            checkProjectPath(inputJsonObject);

            if(!checkToolVersion(inputJsonObject) || !checkJsonKeys(inputJsonObject))
            {
                return false;
            }

            PortfolioVerticle.loadProjectFromImportingConfigFile(inputJsonObject);

        }
        catch(Exception e)
        {
            log.info("Error in importing project. ", e);
            return false;
        }

        return true;
    }

    public static boolean checkJsonKeys(JsonObject inputJsonObject)
    {
        // Templates for JSON config files in order
        List<String> jsonExportFileTemplates = Arrays.asList(
                ActionConfig.getToolParam(),
                ActionConfig.getToolVersionParam(),
                ActionConfig.getUpdatedDateParam(),
                ParamConfig.getProjectIdParam(),
                ParamConfig.getProjectNameParam(),
                ParamConfig.getAnnotationTypeParam(),
                ParamConfig.getIsNewParam(),
                ParamConfig.getIsStarredParam(),
                ParamConfig.getProjectInfraParam(),
                ParamConfig.getCurrentVersionParam(),
                ParamConfig.getProjectVersionParam(),
                ParamConfig.getUuidVersionListParam(),
                ParamConfig.getLabelVersionListParam(),
                ParamConfig.getProjectContentParam()
        );

        for(String key: jsonExportFileTemplates)
        {
            if(!inputJsonObject.containsKey(key))
            {
                String popupTitle = "Import Error";
                String message = "Missing Key in JSON file: " + key;
//                SelectionWindow.showPopupAndLog(popupTitle, message, JOptionPane.ERROR_MESSAGE);
                ProjectImportSelector.formatImportErrorMessage(message);

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
            String popupTitle = "Invalid import file";
            String message = "The configuration file imported is not valid.";
//            SelectionWindow.showPopupAndLog(popupTitle, message, JOptionPane.ERROR_MESSAGE);
            ProjectImportSelector.formatImportErrorMessage(message);

            return false;
        }

        // Does not abort import process. Just print warning
        if(!toolVersionFromJson.equals(ActionConfig.getToolVersion()))
        {
            String message = "Different tool version detected. Import may not work." +
                    "\n\nInstalled Version: " + ActionConfig.getToolVersion() +
                    "\nJSON Version: " + toolVersionFromJson;
            log.warn(message);
        }
        return true;
    }

    public static void checkProjectPath(JsonObject inputJsonObject)
    {
        String initialProjectPath = inputJsonObject.getString(ParamConfig.getProjectPathParam());

        if(!initialProjectPath.equals(ActionConfig.getJsonFilePath()))
        {
            String popupTitle = "Project Path Update";
            String message = "Project path updated \n\nFrom: " + initialProjectPath + "\nTo: " + ActionConfig.getJsonFilePath();
//            SelectionWindow.showPopupAndLog(popupTitle, message, JOptionPane.INFORMATION_MESSAGE);
            ProjectImportSelector.formatImportErrorMessage(message);
        }
    }

}