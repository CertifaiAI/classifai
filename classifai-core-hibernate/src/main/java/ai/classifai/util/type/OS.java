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
package ai.classifai.util.type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * OS Types
 *
 * @author codenamewei
 */
public enum OS
{
    WINDOWS(Arrays.asList("C:\\Program Files\\classifai\\app\\chrome-win\\chrome.exe",
                    System.getProperty("user.home") + "\\AppData\\Local\\Google\\Chrome\\Application\\chrome.exe",
                    "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe",
                    "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe"),
            Arrays.asList("C:\\Windows\\System32\\notepad.exe",
                    "C:\\Windows\\notepad.exe")),
    MAC(Arrays.asList("/Applications/classifai.app/Contents/app/chrome-mac/Chromium.app",
            "/Applications/Google Chrome.app"),
            Collections.singletonList("default")),
    UBUNTU(new ArrayList<>(), new ArrayList<>()),
    LINUX(Collections.singletonList("default"),
            Collections.singletonList("default")),
    UNIX(new ArrayList<>(), new ArrayList<>()),
    SOLARIS(new ArrayList<>(), new ArrayList<>()),
    NULL(new ArrayList<>(), new ArrayList<>());

    private final List<String> browserPathList;
    private final List<String> textEditorPathList;

    OS(List<String> browserPathList, List<String> textEditorPath)
    {
        this.browserPathList = browserPathList;
        this.textEditorPathList = textEditorPath;
    }

    public List<String> getBrowserPathList()
    {
        return browserPathList;
    }

    public List<String> getTextEditorPathList()
    {
        return textEditorPathList;
    }
}