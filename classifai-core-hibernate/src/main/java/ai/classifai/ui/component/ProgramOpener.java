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
package ai.classifai.ui.component;

import ai.classifai.util.collection.ConversionHandler;
import ai.classifai.util.type.OS;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;

/**
 * Browser Opening for Classifai
 *
 * @author codenamewei
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProgramOpener
{
    private static ImageIcon browserNotFoundIcon;

    static
    {
        try
        {
            Image iconImage = ImageIO.read(ProgramOpener.class.getResource( "/icon/Classifai_Favicon_Dark_32px.png"));

            browserNotFoundIcon = new ImageIcon(iconImage);
        }
        catch (Exception e)
        {
            log.info("Classifai icon for Program Path Not Found", e);
        }
    }

    public static boolean runProgramPath(OS os, String[] commandPath)
    {
        boolean isProgramAbleToRun = false;

        try
        {
            if (os.equals(OS.LINUX) || os.equals(OS.MAC))
            {
                isProgramAbleToRun = isRunning(Runtime.getRuntime().exec(commandPath));
            }
            else
            {
                isProgramAbleToRun = isRunning(Runtime.getRuntime().exec(ConversionHandler.arrayString2String(commandPath)));
            }

        }
        catch (Exception e)
        {
            log.info("Failed to run " + ConversionHandler.arrayString2String(commandPath) +  ": ", e);
        }

        return isProgramAbleToRun;

    }

    public static boolean isRunning(Process process)
    {
        try
        {
            process.exitValue();
            return false;
        }
        catch (Exception e)
        {
            return true;
        }
    }
}