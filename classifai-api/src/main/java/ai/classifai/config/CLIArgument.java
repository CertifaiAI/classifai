/*
 * Copyright (c) 2020-2021 CertifAI Sdn. Bhd.
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
package ai.classifai.config;

import ai.classifai.loader.CLIProjectInitiator;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.ProjectHandler;
import ai.classifai.util.type.AnnotationHandler;
import ai.classifai.util.type.AnnotationType;
import com.formdev.flatlaf.FlatLightLaf;
import lombok.extern.slf4j.Slf4j;

import java.io.File;


/**
 * Command Line Argument Configuration
 *
 * @author codenamewei
 *
 * Sample: java -jar classifai-uberjar-dev.jar --unlockdb --docker --docker --projectname=demo --projecttype=segmentation --datapath=/Users/john/Desktop/sample-image
 *
 * Argument:
 * 1. --port=1234
 *    Port for Classifai
 *
 * 2. --unlockdb
 *    To use database even the lck file exist
 *
 * 3. --docker
 *    Docker build. WelcomeLauncher, FolderSelector & FileSelector will not initiate.
 *
 * 4. --projectname=<projectname>
 *    Create new project from cli / Use existing project if exist
 *    if not defined, default project name will be randomly generated
 *
 *    --projectname=helloworld
 *
 * 5. --projecttype=boundingbox/segmentation
 *    Project Type
 *
 *    --projecttype=boundingbox
 *
 * 6. --datapath=<datapath>
 *
 *    Source of data file
 *
 *    --datapath=/image-folder
 *    --datapath="/Users/john/Desktop/sample-image"
 */
@Slf4j
public class CLIArgument
{
    private boolean removeDbLock = false;
    private boolean isDockerEnv = false;

    private String projectName = null;
    private String projectType = null;

    private String dataPath = null;

    public boolean isDbSetup()
    {
        return DbConfig.isDatabaseSetup(removeDbLock);
    }

    public CLIArgument(String[] args)
    {
        for (String arg : args)
        {
            if (arg.contains("--unlockdb"))
            {
                removeDbLock = true;
            }
            else if (arg.contains("--docker"))
            {
                isDockerEnv = true;
                ParamConfig.setIsDockerEnv(true);
            }
            else if (arg.contains("--port="))
            {
                String[] buffer = arg.split("=");
                PortSelector.configurePort(buffer[1]);
            }
            else if (arg.contains("--projectname="))
            {
                String[] buffer = arg.split("=");
                projectName = buffer[1];
            }
            else if (arg.contains("--projecttype="))
            {
                String[] buffer = arg.split("=");
                projectType = buffer[1];
            }
            else if (arg.contains("--datapath="))
            {
                String[] buffer = arg.split("=");
                dataPath = buffer[1];
            }
        }

        if(!isDockerEnv) FlatLightLaf.install();

        checkToInitiateCLIProject();

    }

    private void checkToInitiateCLIProject()
    {

        if((projectType == null) && (projectName == null) && (dataPath == null)) return;

        /*
         * failed scenario:
         *
         *   1. projecttype not set, but projectname and/or datapath set
         *
         *   2. projecttype set - undefined project type (not boundingbox / segmentation) (type == null)
         *
         */
        //Scenario 1
        if(projectType == null && (dataPath != null|| (projectName != null)))
        {
            log.info("--projecttype not defined while project intend to initiated through cli");

            if(dataPath != null) log.info("--datapath=" + dataPath);

            if(projectName != null) log.info("--projectname=" + projectName);

            printMessageForCLIProjectFailed();
            return;
        }

        AnnotationType type = AnnotationHandler.getType(projectType);

        //scenario 2
        if(type == null)
        {
            log.info("--projecttype not valid with argument: " + projectType);
            printMessageForCLIProjectFailed();
            return;
        }

        CLIProjectInitiator initiator;

        boolean isDataPathValid = dataPath != null && !dataPath.equals("") && new File(dataPath).exists();
        boolean isProjectNameValid = (projectName != null) && (!projectName.equals(""));

        if(isDataPathValid && isProjectNameValid)
        {
            log.info("Data path imported through cli: " + dataPath);
            initiator = new CLIProjectInitiator(type, projectName, dataPath);
        }
        else
        {
            if(isProjectNameValid)
            {
                initiator = new CLIProjectInitiator(type, projectName);
            }
            else
            {
                initiator = new CLIProjectInitiator(type);
            }

        }

        ProjectHandler.setCliProjectInitiator(initiator);
    }

    private void printMessageForCLIProjectFailed()
    {
        log.info("\n" +
                "Usage:  java -jar classifai-uberjar-dev.jar [OPTIONS]\n" +
                "\n" +
                "Options\n" +
                "      --unlockdb              Unlock database to start if lck file exist\n" +
                "      --port=integer          Run in a designated port. Example: --port=1234\n\n" +
                "      --docker                Run in docker mode. Not showing Welcome Launcher and File/Folder Selector.\n" +
                "      --projectname=string    Assign a project name when starting classifai. Project created when not exist. Example: --projectname=demo\n" +
                "      --projecttype=string    Assign the type of project. Only accepts [boundingbox/segmentation] argument. Example: --projecttype=segmentation\n" +
                "      --datapath=string       Folder path to import data points to a project. Example: --datapath=/image-folder\n");
    }
}
