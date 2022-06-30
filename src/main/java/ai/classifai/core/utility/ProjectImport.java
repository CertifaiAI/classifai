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
package ai.classifai.core.utility;

import ai.classifai.core.loader.ProjectHandler;
import ai.classifai.core.loader.ProjectLoader;
import ai.classifai.core.utility.parser.PortfolioParser;
import ai.classifai.core.properties.image.ImageDataProperties;
import ai.classifai.core.properties.ProjectConfigProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileReader;
import java.util.Map;

/**
 * Import of project from configuration file
 *
 * @author codenamewei
 */
@Slf4j
public class ProjectImport
{
    @Setter private ProjectHandler projectHandler;

    public ProjectImport(ProjectHandler projectHandler){
        this.projectHandler = projectHandler;
    }

    public String importProjectFile(@NonNull File jsonFile)
    {
        try {
            String jsonStr = IOUtils.toString(new FileReader(jsonFile));
            ProjectConfigProperties importConfig = jsonStrToConfig(jsonStr);

            if(importConfig == null) {
                return null;
            }

            if(!checkToolVersion(importConfig)) {
                return null;
            }

            checkProjectPath(importConfig);

            return loadProjectFromImportingConfigFile(importConfig);

        } catch(Exception e) {
            log.info("Error in importing project. ", e);
            return null;
        }
    }

    public ProjectConfigProperties jsonStrToConfig(String jsonString)
    {
        ObjectMapper mp = new ObjectMapper();
        try {
            return mp.readValue(jsonString, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            log.info("Fail to convert import config", e);
            return null;
        }
    }

    public boolean checkToolVersion(ProjectConfigProperties importConfig)
    {
        String toolNameFromJson = importConfig.getToolName();
        String toolVersionFromJson = importConfig.getToolVersion();
        String updatedDateFromJson = importConfig.getUpdateDate();

        if(toolNameFromJson == null || toolVersionFromJson == null || updatedDateFromJson == null || !toolNameFromJson.equals(ActionConfig.getToolName()))
        {
            String message = "The configuration file imported is not valid.";
            log.info(message);

            return false;
        }

        // Does not abort import process. Just print warning
        if(!toolVersionFromJson.equals(ActionConfig.getToolVersion()))
        {
            String message = "Different tool version detected. Import may not work." +
                    "\nInstalled Version: " + ActionConfig.getToolVersion() +
                    "\nJSON Version: " + toolVersionFromJson;
            log.warn(message);
        }
        return true;
    }

    public void checkProjectPath(ProjectConfigProperties importConfig)
    {
        String initialProjectPath = importConfig.getProjectPath();

        if(!initialProjectPath.equals(ActionConfig.getJsonFilePath()))
        {
            String message = "Project path updated \nFrom: " + initialProjectPath + "\nTo: " + ActionConfig.getJsonFilePath();
            log.info(message);
        }
    }

    public String loadProjectFromImportingConfigFile(@NonNull ProjectConfigProperties importConfig)
    {
        ProjectLoader loader = PortfolioParser.parseIn(importConfig);

        String newProjName = "";
        while (!projectHandler.isProjectNameUnique(loader.getProjectName(), loader.getAnnotationType()))
        {
            newProjName = new NameGenerator().getNewProjectName();
            loader.setProjectName(newProjName);
            loader.setProjectId(UuidGenerator.generateUuid());
        }

        // Only show popup if there is duplicate project name
        if(!newProjName.equals(""))
        {
            String message = "Name Overlapped. Rename as " + newProjName + ".";
            log.info(message);
        }

        projectHandler.loadProjectLoader(loader);

        //load project table first
        Map<String, ImageDataProperties> content = importConfig.getContent();
//        ProjectParser.parseIn(annotationDB, loader, content);
//
//        portfolioDB.loadProject(loader);

        return loader.getProjectName();
    }

}