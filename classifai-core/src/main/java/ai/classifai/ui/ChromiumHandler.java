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

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static javax.swing.JOptionPane.showMessageDialog;

/***
 * Handler to open classifai in browser
 *
 * @author Chiawei Lim
 */
@Slf4j
public class ChromiumHandler {

    private final static String PRIMARY_KEY = "chromium";
    private final static String SECONDARY_KEY = "chrome";

    private static Map<String, String> macBrowserKey;
    private static Map<String, String> winBrowserKey;

    private static ImageIcon browserNotFoundIcon;

    static
    {
        macBrowserKey = new HashMap<>();

        //FIXME: Is there a way to fix this coded path?
        macBrowserKey.put(PRIMARY_KEY, "/Applications/classifai.app/Contents/app/chrome-mac/Chromium.app");
        macBrowserKey.put(SECONDARY_KEY, "/Applications/Google Chrome.app");

        winBrowserKey = new HashMap<>();

        String chromeNativePath = System.getProperty("user.home") + "\\AppData\\Local\\Google\\Chrome\\Application\\chrome.exe";

        winBrowserKey.put(PRIMARY_KEY, "app\\chrome-win\\chrome.exe");
        winBrowserKey.put(SECONDARY_KEY, chromeNativePath);


        try {

            Image iconImage = ImageIO.read(ChromiumHandler.class.getResource( "/icon/Classifai_Favicon_Dark_32px.png"));

            browserNotFoundIcon = new ImageIcon(iconImage);
        }
        catch (Exception e)
        {
            log.info("Classifai icon for Browser Not Found is missing", e);
        }
    }

    public static void openOnBrowser(String url, OSManager osManager)
    {
        String[] commandPath = null;
        String browserNotFoundMessage = "Browser not found.\nProceed to open " + url + " in other browser";
        String osNotSupportedMessage = "OS not supported.\nProceed to open " + url + " in other browser";

        OS currentOS = osManager.getCurrentOS();

        //https://stackoverflow.com/questions/45660482/open-a-url-in-chrome-using-java-in-linux-and-mac/45660804
        //chromium primary, chrome secondary, else failure
        if(currentOS.equals(OS.MAC))
        {
            String param1 = "/usr/bin/open";
            String param2 = "-a";

            if(isBrowserFileExist(macBrowserKey.get(PRIMARY_KEY)))
            {
                commandPath = new String[]{param1, param2, macBrowserKey.get(PRIMARY_KEY), url};
            }
            else if(isBrowserFileExist(macBrowserKey.get(SECONDARY_KEY)))
            {
                commandPath = new String[]{param1, param2, macBrowserKey.get(SECONDARY_KEY), url};
            }
            else
            {
                failToOpenBrowserMessage(browserNotFoundMessage);
            }
        }
        else if(currentOS.equals(OS.WINDOWS))
        {
            String param1 = "cmd";
            String param2 = "/c";

            if(isBrowserFileExist(winBrowserKey.get(PRIMARY_KEY)))
            {
                commandPath = new String[]{param1, param2, "start " + winBrowserKey.get(PRIMARY_KEY) + " " + url};
            }
            else if(isBrowserFileExist(winBrowserKey.get(SECONDARY_KEY)))
            {
                commandPath = new String[]{param1, param2, "start " + winBrowserKey.get(SECONDARY_KEY) + " " + url};
            }
            else
            {
                failToOpenBrowserMessage(browserNotFoundMessage);
            }
        }
        else
        {
            log.debug("Browser in " + currentOS.toString() + " not supported yet");
            failToOpenBrowserMessage(osNotSupportedMessage);
            return;
        }

        if(commandPath != null)
        {
            try
            {
                Runtime.getRuntime().exec(commandPath);
            }
            catch(Exception e)
            {
                log.debug("Failed to open classifai in " + currentOS.toString(), e);
            }
        }

    }

    public static boolean isBrowserFileExist(String appPath)
    {
        if(new File(appPath).exists() == false)
        {
            log.debug("Chromium browser not found - " + appPath);

            return false;
        }

        return true;
    }

    public static void failToOpenBrowserMessage(String message)
    {

        showMessageDialog(null, message,
                "Oops!", JOptionPane.INFORMATION_MESSAGE, browserNotFoundIcon);
    }
}
