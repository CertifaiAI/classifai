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
import sun.jvm.hotspot.debugger.posix.elf.ELFSectionHeader;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javax.swing.JOptionPane.showMessageDialog;

/***
 * Handler to open classifai in browser.
 * Options goes to
 * (1) chromium comes with package
 * (2) chrome in local system
 * Else, user have to open the url in selected browser
 *
 * @author Chiawei Lim
 */
@Slf4j
public class BrowserHandler {

    private static Map<String, List> browserKey;

    private static ImageIcon browserNotFoundIcon;

    static
    {
        browserKey = new HashMap<>();

        List macBrowserKey = new ArrayList<String>();
        macBrowserKey.add("/Applications/classifai.app/Contents/app/chrome-mac/Chromium.app");
        macBrowserKey.add("/Applications/Google Chrome.app");

        List winBrowserKey = new ArrayList<String>();

        String chromeNativePath = System.getProperty("user.home") + "\\AppData\\Local\\Google\\Chrome\\Application\\chrome.exe";

        winBrowserKey.add("app\\chrome-win\\chrome.exe");
        winBrowserKey.add(chromeNativePath);
        winBrowserKey.add("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe");

        browserKey.put(OS.MAC.name(), macBrowserKey);
        browserKey.put(OS.WINDOWS.name(), winBrowserKey);

        try {

            Image iconImage = ImageIO.read(BrowserHandler.class.getResource( "/icon/Classifai_Favicon_Dark_32px.png"));

            browserNotFoundIcon = new ImageIcon(iconImage);
        }
        catch (Exception e)
        {
            log.info("Classifai icon for Browser Not Found is missing", e);
        }
    }

    public static void openOnBrowser(String url, OSManager osManager)
    {
        boolean browserNotFound = true;//default as true
        String browserNotFoundMessage = "Browser not found.\nProceed to open " + url + " in other browser";
        String osNotSupportedMessage = "OS not supported.\nProceed to open " + url + " in other browser";

        OS currentOS = osManager.getCurrentOS();

        if(browserKey.containsKey(currentOS.name()))
        {
            List<String> browserList = browserKey.get(currentOS.name());

            if((browserList == null) || (browserList.isEmpty()))
            {
                log.info("Browser for " + currentOS.name() + " cannot be found");
                failToOpenBrowserMessage(browserNotFoundMessage);
                return;
            }

            for(String browser : browserList)
            {
                if((browserNotFound == true) && (isBrowserFileExist(browser)))
                {
                    if(tryCurrentBrowserPath(currentOS, browser, url))
                    {
                        browserNotFound = false;
                        break;
                    }
                }
            }

            if(browserNotFound)
            {
                log.info("Could not find a browser for current OS: " + currentOS.name());
                failToOpenBrowserMessage(browserNotFoundMessage);
                return;
            }

        }
        else
        {
            log.info("Current selected OS: " + currentOS.name() + " is not supported yet");
            failToOpenBrowserMessage(osNotSupportedMessage);
            return;
        }

    }

    public static boolean tryCurrentBrowserPath(OS os, String browserPath, String url)
    {
        String[] commandPath = null;

        if(os.equals(OS.MAC))
        {
            commandPath = new String[]{"/usr/bin/open", "-a", browserPath, url};
        }
        else if(os.equals(OS.WINDOWS))
        {
            commandPath = new String[]{"cmd", "/c", "start " + browserPath + " " + url};
        }

        try
        {
            Runtime.getRuntime().exec(commandPath);
            return true;
        }
        catch(Exception e)
        {
            log.debug("Failed to open classifai in " + os.name(), e);
        }
        return false;
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
