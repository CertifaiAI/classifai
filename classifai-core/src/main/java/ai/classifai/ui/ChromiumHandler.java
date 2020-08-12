/*
 * Copyright (c) 2020 CertifAI
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

package ai.classifai.ui;

import lombok.extern.slf4j.Slf4j;

import java.io.File;

/***
 * Handler to open classifai in browser
 *
 * @author Chiawei Lim
 */
@Slf4j
public class ChromiumHandler {

    private static final String WIN_CHROMIUM_PATH = "app\\chrome-win\\chrome.exe";

    //FIXME: Is there a way to fix this coded path?
    private static final String MAC_CHROMIUM_PATH = "/Applications/classifai.app/Contents/app/chrome-mac/Chromium.app";

    public static void openOnBrowser(String url, OSManager osManager)
    {
        String[] commandPath = null;

        OS currentOS = osManager.getCurrentOS();

        //https://stackoverflow.com/questions/45660482/open-a-url-in-chrome-using-java-in-linux-and-mac/45660804
        if(currentOS.equals(OS.MAC))
        {
            if(isBrowserFileExist(MAC_CHROMIUM_PATH))
            {
                commandPath = new String[]{"/usr/bin/open", "-a", System.getProperty("user.dir") + "/" + MAC_CHROMIUM_PATH, url};
            }
            else
            {
                //open chrome , else failed
            }
        }
        else if(currentOS.equals(OS.WINDOWS))
        {
            if(isBrowserFileExist(WIN_CHROMIUM_PATH))
            {
                commandPath = new String[]{"cmd", "/c", "start " + WIN_CHROMIUM_PATH + " " + url};
            }
            else
            {
                //open chrome , else failed
            }
        }
        else
        {
            log.info("Browser in " + currentOS.toString() + " not supported yet");

            return;
        }

        try
        {
            Runtime.getRuntime().exec(commandPath);

        }
        catch(Exception e)
        {
            log.info(currentOS.toString() + " - Failed to open classifai. ", e);
        }
    }

    public static boolean isBrowserFileExist(String appPath)
    {
        if(new File(appPath).exists() == false)
        {
            log.info("Chromium browser not found.");

            return false;
        }

        return true;
    }
}
