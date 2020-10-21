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

import ai.classifai.server.ParamConfig;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***
 * Handler to open classifai in browser.
 * Options goes to
 * (1) chromium comes with package
 * (2) default browser in local system
 * Else, user have to open the url in selected browser
 *
 * @author Chiawei Lim
 */
@Slf4j
public class BrowserHandler {

    @Getter private static Map<String, List<String>> browserKey;
    @Getter private static String browserURL;

    static
    {
        browserURL = "http://localhost:" + ParamConfig.getHostingPort();

        browserKey = new HashMap<>();

        List macBrowserKey = new ArrayList<String>();
        macBrowserKey.add("/Applications/classifai.app/Contents/app/chrome-mac/Chromium.app");
        macBrowserKey.add("/Applications/Google Chrome.app");

        List winBrowserKey = new ArrayList<String>();

        String chromeNativePath = System.getProperty("user.home") + "\\AppData\\Local\\Google\\Chrome\\Application\\chrome.exe";

        winBrowserKey.add("C:\\Program Files\\classifai\\app\\chrome-win\\chrome.exe");
        winBrowserKey.add(chromeNativePath);
        winBrowserKey.add("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe");
        winBrowserKey.add("C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe");


        List linuxBrowserKey = new ArrayList<String>();

        linuxBrowserKey.add("/usr/bin/firefox");

        browserKey.put(OS.MAC.name(), macBrowserKey);
        browserKey.put(OS.WINDOWS.name(), winBrowserKey);
<<<<<<< HEAD
        browserKey.put(OS.LINUX.name(), linuxBrowserKey);

=======
        //browserKey.put(OS.LINUX.name(), linuxBrowserKey);
>>>>>>> b676e49ac9cba714b52dcc7b6ca6f884abf8299e
    }
}