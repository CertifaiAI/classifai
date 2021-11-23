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

import ai.classifai.loader.CLIProjectImporter;
import ai.classifai.loader.CLIProjectInitiator;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.type.AnnotationType;
import com.formdev.flatlaf.FlatLightLaf;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

/**
 * Command Line Argument Configuration
 *
 * @author codenamewei
 * Build porject using CLI
 * Sample: 1) java -jar classifai-uberjar-dev.jar --port=8888 --docker --projectname=demo --projecttype=segmentation --datapath=/Users/john/Desktop/sample-image
*          2) java -jar classifai-uberjar-dev.jar --port=8888 --docker --projectname=demo --projecttype=segmentation --datapath=/Users/john/Desktop/sample-image --labelpath=/Users/john/Desktop/sample-image/sample-label.txt  (Use if label list text file is available)
 *
 * Import project using CLI
 * Sample : java -jar classifai-uberjar-dev.jar --port=9999 --docker --datapath=/Users/chia wei/Desktop/sample-image --configpath=/Users/chia wei/Desktop/sample-image/sample-config.json
 *
 * Argument:
 * 1. --port=1234
 *    Port for Classifai
 *
 * 2. --docker
 *    Docker build. WelcomeLauncher, FolderSelector & FileSelector will not initiate.
 *
 * 3. --projectname=<projectname>
 *    Create new project from cli / Use existing project if exist
 *    if not defined, default project name will be randomly generated
 *
 *    --projectname=helloworld
 *
 * 4. --projecttype=boundingbox/segmentation
 *    Project Type
 *
 *    --projecttype=boundingbox
 *
 * 5. --datapath=<datapath>
 *
 *    Source of data file
 *
 *    --datapath=/image-folder
 *    --datapath="/Users/john/Desktop/sample-image"
 *
 *  6. --labelpath=<labelpath>
 *
 *     Source of label file
 *
 *     --labelpath=/Users/john/Desktop/sample-image/sample-label.txt
 *
 *  7. --configpath=<configpath>
 *
 *     Source of project configuration file
 *
 *     --configpath=/Users/john/Desktop/sample-image/sample-config.json
 *
 */
@Slf4j
public class CLIArgument
{
    private boolean isDockerEnv = false;

    private String projectName = null;
    private String projectType = null;

    private String dataPath = null;
    private String configPath = null;
    private String labelPath = null;

    @Getter
    private CLIProjectInitiator initiator;

    @Getter
    private CLIProjectImporter importer;


    public CLIArgument(String[] args)
    {
        for (String arg : args)
        {
            if (arg.contains("--docker"))
            {
                isDockerEnv = true;
                ParamConfig.setDockerEnv(true);
            }
            else if (arg.contains("--port="))
            {
                String port = getArg(arg);

                if (port != null)
                {
                    PortSelector.configurePort(port);
                }
                else
                {
                    log.info("Port failed to configure through cli");
                }
            }
            else if (arg.contains("--projectname="))
            {
                projectName = getArg(arg);
            }
            else if (arg.contains("--projecttype="))
            {
                projectType  = getArg(arg);
            }
            else if (arg.contains("--datapath="))
            {
                dataPath = getArg(arg);
            }
            else if (arg.contains("--configpath="))
            {
                configPath = getArg(arg);
            }
            else if (arg.contains("--labelpath="))
            {
                labelPath = getArg(arg);
            }
        }

        if (!isDockerEnv) FlatLightLaf.install();

        checkToInitiateCLIProject();
        checkToImportProject();

    }

    private String getArg(String arg)
    {
        String[] buffer = arg.split("=");
        if (buffer.length == 2)
        {
            return buffer[1];
        }

        return null;
    }

    private void checkToInitiateCLIProject()
    {

        if ((projectType == null) && (dataPath == null)) return;

        /*
         * failed scenario:
         *
         *   1. projecttype not set, but projectname and/or datapath set
         *
         *   2. projecttype set - undefined project type (not boundingbox / segmentation) (type == null)
         *
         */
        //Scenario 1
        if (projectType == null)
        {
            log.info("--projecttype not defined while project intend to initiated through cli");

            printMessageForCLIProjectFailed();
            return;
        }

        AnnotationType type = AnnotationType.get(projectType);

        //scenario 2
        if (type == null)
        {
            log.info("--projecttype not valid with argument: " + projectType);
            printMessageForCLIProjectFailed();
            return;
        }

        boolean isDataPathValid = dataPath != null && !dataPath.equals("") && new File(dataPath).exists();
        boolean isProjectNameValid = (projectName != null) && (!projectName.equals(""));
        boolean isLabelPathValid = labelPath != null && !labelPath.equals("") && new File(labelPath).exists();

        if (isDataPathValid)
        {
            log.info("Data path imported through cli: " + dataPath);

            if (isProjectNameValid && isLabelPathValid)
            {
                log.info("Label file path imported through cli: " + labelPath);

                initiator = CLIProjectInitiator.builder()
                        .projectName(projectName)
                        .rootDataPath(new File(dataPath))
                        .labelFilePath(new File(labelPath))
                        .projectType(type)
                        .build();
            }
            else if (isProjectNameValid)
            {
                initiator = CLIProjectInitiator.builder()
                        .projectName(projectName)
                        .rootDataPath(new File(dataPath))
                        .projectType(type)
                        .build();
            }
            else
            {
                initiator = CLIProjectInitiator.builder()
                        .rootDataPath(new File(dataPath))
                        .projectType(type)
                        .build();
            }

        }
    }

    private void checkToImportProject(){

        boolean isConfigPathValid = (configPath != null) && (!configPath.equals("")) && (new File(configPath).exists());

        if (isConfigPathValid)
        {
            log.info("Project configuration file path imported through cli: " + configPath);
            importer = CLIProjectImporter.builder()
                    .configFilePath(new File(configPath))
                    .build();
        }
    }

    private void printMessageForCLIProjectFailed()
    {
        if (configPath != null)
        {
            log.info("Project configuration file path detected");
        }
        log.info("\n" +
                "Usage:  java -jar classifai-uberjar-dev.jar [OPTIONS]\n" +
                "\n" +
                "Options\n" +
                "      --unlockdb              Unlock database to start if lck file exist\n" +
                "      --port=integer          Run in a designated port. Example: --port=1234\n\n" +
                "      --docker                Run in docker mode. Not showing Welcome Launcher and File/Folder Selector.\n" +
                "      --projectname=string    Assign a project name when starting classifai. Project created when not exist. Example: --projectname=demo\n" +
                "      --projecttype=string    Assign the type of project. Only accepts [boundingbox/segmentation] argument. Example: --projecttype=segmentation\n" +
                "      --datapath=string       Folder path to import data points to a project. Example: --datapath=/image-folder\n" +
                "      --labelpath=string        Folder path to import label text file to a project. Example: --labelpath=/image-folder/sample-label.txt\n" +
                "      --configpath=string       Folder path to import configuration file to a project. Example: --configpath=/image-folder/sample-config.json\n");
    }
}
