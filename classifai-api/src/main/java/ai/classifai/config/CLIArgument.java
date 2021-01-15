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
import com.formdev.flatlaf.FlatLightLaf;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.LineIterator;


/**
 * Command Line Argument Configuration
 *
 * @author codenamewei
 *
 * Argument:
 * 1. --port=1234
 *    Port for Classifai
 *
 * 2. --unlockdb
 *    To use database even the lck file exist
 *
 * 3. --cibuild
 *    Configuration for cd build
 *
 * 4. --docker
 *    Docker build. WelcomeLauncher, FolderSelector & FileSelector will not initiate.
 *
 * 5. --projectname=<projectname>
 *    Create new project from cli / Use existing project if exist
 *    if not defined, default project name will be "default"
 *
 *    --projectname=helloworld
 *
 * 6. --projecttype=boundingbox/segmentation
 *    Project Type
 *
 *    --projecttype=boundingbox
 *
 * 7. --datapath=<datapath>
 *
 *    Source of data file
 *
 *    --datapath=/image-folder
 */
@Slf4j
public class CLIArgument
{
    @Getter private boolean isCIBuild = false;

    private boolean removeDbLock = false;

    public boolean isDbSetup()
    {
        return DbConfig.isDatabaseSetup(removeDbLock);
    }

    public CLIArgument(String[] args)
    {
        boolean isDockerEnv = false;
        String projectName = null;
        String projectType = null;
        String dataPath = null;


        for(int i = 0; i < args.length; ++i)
        {
            String arg = args[i];
            if(arg.contains("--port="))
            {
                String[] buffer = args[i].split("=");
                PortSelector.configurePort(buffer[1]);
            }
            else if(arg.contains("--unlockdb"))
            {
                removeDbLock = true;
            }
            else if(arg.contains("--docker"))
            {
                isDockerEnv = true;
                ParamConfig.setIsDockerEnv(true);
            }
            else if(arg.contains("--cibuild"))
            {
                isCIBuild = true;
                isDockerEnv = true;
                ParamConfig.setIsDockerEnv(true);
            }
            else if(arg.contains("--projectname"))
            {
                String[] buffer = args[i].split("=");
                projectName = buffer[1];
            }
            else if(arg.contains("--projecttype"))
            {
                String[] buffer = args[i].split("=");
                projectType = buffer[1];
            }
            else if(arg.contains("--datapath"))
            {
                String[] buffer = args[i].split("=");
                dataPath = buffer[1];
            }
        }

        if(!isDockerEnv) FlatLightLaf.install();

        if((projectType != null) && (dataPath != null))
        {
            CLIProjectInitiator initiator;

            if(projectName != null)
            {
                initiator = new CLIProjectInitiator(projectName, projectType, dataPath);
            }
            else
            {
                initiator = new CLIProjectInitiator(projectType, dataPath);
            }

            ProjectHandler.setCliProjectInitiator(initiator);
        }
    }
}
