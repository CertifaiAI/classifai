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

/***
 * Handler to open classifai in browser
 *
 * @author Chiawei Lim
 */
@Slf4j
public class BrowserHandler {

    public static void openOnBrowser(String url)
    {
        String OS = System.getProperty("os.name").toLowerCase();

        if(isWindows(OS)) //windows
        {
            try
            {
                Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start chrome " + url});
            }
            catch(Exception e)
            {
                log.debug("Windows: Failed to open in browser");
            }

        }
        else if(isMac(OS))//mac
        {
            try
            {
                Runtime.getRuntime().exec(new String[]{"/usr/bin/open", "-a", "/Users/wei/Downloads/chrome-mac/Chromium.app", url});

            }
            catch(Exception e)
            {
                log.debug("Failed to open in browser");
            }
        }
        else
        {
            log.debug("Browser not supported");
        }
    }

    private static boolean isWindows(String OS) {

        return (OS.indexOf("win") >= 0);

    }

    private static boolean isMac(String OS) {

        return (OS.indexOf("mac") >= 0);

    }

    private static boolean isUnix(String OS) {

        return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 );

    }

    private static boolean isSolaris(String OS) {

        return (OS.indexOf("sunos") >= 0);

    }
}
