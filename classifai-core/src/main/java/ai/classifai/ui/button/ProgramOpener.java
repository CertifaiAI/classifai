/*
 * Copyright (c) 2020 CertifAI Sdn. Bhd.
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
package ai.classifai.ui.button;

import ai.classifai.util.collection.ConversionHandler;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.Map;

import static javax.swing.JOptionPane.showMessageDialog;

/**
 * Browser Opening for classifai
 *
 * @author codenamewei
 */
@Slf4j
public class ProgramOpener
{
    private static ImageIcon browserNotFoundIcon;

    static
    {
        try {

            Image iconImage = ImageIO.read(BrowserHandler.class.getResource( "/icon/Classifai_Favicon_Dark_32px.png"));

            browserNotFoundIcon = new ImageIcon(iconImage);
        }
        catch (Exception e)
        {
            log.info("Classifai icon for Program Path Not Found is missing", e);
        }
    }

    private ProgramOpener(){}

    public static void launch(OS currentOS, Map<String, List<String>> programKey, String param)
    {
        boolean programNotFound = true;//default as true

        if(programKey.containsKey(currentOS.name()))
        {
            List<String> programList = programKey.get(currentOS.name());

            if((programList == null) || (programList.isEmpty()))
            {
                failToOpenProgramPathMessage("Program for " + currentOS.name() + " cannot be found");
            }

            for(String browser : programList)
            {
                if(programNotFound && (isProgramPathExist(browser)))
                {
                    if(runProgramPath(currentOS, browser, param))
                    {
                        programNotFound = false;
                        break;
                    }
                }
            }

            if(programNotFound)
            {
                failToOpenProgramPathMessage("Initialization of program path failed in current OS: " + currentOS.name());
            }

        }
        else
        {
            String osNotSupportedMessage = "Current selected OS is not supported yet";
            failToOpenProgramPathMessage(osNotSupportedMessage);
        }
    }

    public static boolean runProgramPath(OS os, String programPath, String param)
    {
        boolean isProgramAbleToRun = false;
        String[] commandPath = null;

        if(os.equals(OS.MAC))
        {
            commandPath = new String[]{"/usr/bin/open", "-a", programPath, param};
        }
        else if(os.equals(OS.WINDOWS))
        {
            //commandPath = new String[]{"cmd", "/c", "start \"" + programPath + "\" " + param};
            commandPath = new String[]{programPath + " " + param};

        }else if(os.equals(OS.LINUX))
        {
            commandPath = new String[]{"gio", "open", param};
        }

        try
        {
            if(os.equals(OS.LINUX))
            {
                Runtime.getRuntime().exec(commandPath);
            }
            else
            {
                Runtime.getRuntime().exec(ConversionHandler.arrayString2String(commandPath));
            }

            isProgramAbleToRun =  true;
        }
        catch(Exception e)
        {
            log.debug("Failed to run " + programPath + " " + param + ": ", e);
        }

        return isProgramAbleToRun;
    }

    public static boolean isProgramPathExist(String appPath)
    {
        if(appPath.equals("default"))
        {
            return true;
        }
        if(!new File(appPath).exists())
        {
            log.debug("Program not found - " + appPath);

            return false;
        }

        return true;
    }

    public static void failToOpenProgramPathMessage(String message)
    {
        log.info(message);
        showMessageDialog(null, message,
                "Oops!", JOptionPane.INFORMATION_MESSAGE, browserNotFoundIcon);
    }
}