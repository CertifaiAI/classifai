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
 * Sample: java -jar classifai-uberjar-dev.jar --port=8888 --docker --projectname=demo --projecttype=segmentation --datapath=/Users/john/Desktop/sample-image
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
 */
@Slf4j
public class CLIArgument
{
    private boolean isDockerEnv = false;

    private String projectName = null;
    private String projectType = null;

    private String dataPath = null;

    public CLIArgument(String[] args)
    {
        for (String arg : args)
        {
            if (arg.contains("--docker"))
            {
                isDockerEnv = true;
                ParamConfig.setIsDockerEnv(true);
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
        }

        if (!isDockerEnv) FlatLightLaf.install();

        checkToInitiateCLIProject();
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

        AnnotationType type = AnnotationHandler.getType(projectType);

        //scenario 2
        if (type == null)
        {
            log.info("--projecttype not valid with argument: " + projectType);
            printMessageForCLIProjectFailed();
            return;
        }

        CLIProjectInitiator initiator;

        boolean isDataPathValid = dataPath != null && !dataPath.equals("") && new File(dataPath).exists();
        boolean isProjectNameValid = (projectName != null) && (!projectName.equals(""));

        if (isDataPathValid)
        {
            log.info("Data path imported through cli: " + dataPath);

            if (isProjectNameValid)
            {
                initiator = new CLIProjectInitiator(type, projectName, new File(dataPath));
            }
            else
            {
                initiator = new CLIProjectInitiator(type, new File(dataPath));
            }

        }
        else
        {
            if (isProjectNameValid)
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
