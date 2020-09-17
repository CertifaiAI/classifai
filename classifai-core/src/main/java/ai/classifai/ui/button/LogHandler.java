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
package ai.classifai.ui.button;

import ai.classifai.server.ParamConfig;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default text editor in current OS to open log
 */
@Slf4j
public class LogHandler
{
    @Getter private static Map<String, List<String>> textEditorKey;
    @Getter private static String logPath;

    static
    {
        textEditorKey = new HashMap<>();

        List winEditorPath = new ArrayList<String>();

        winEditorPath.add("C:\\Windows\\System32\\notepad.exe");
        winEditorPath.add("C:\\Windows\\notepad.exe");

        textEditorKey.put(OS.MAC.name(), null);
        textEditorKey.put(OS.WINDOWS.name(), winEditorPath);
        textEditorKey.put(OS.LINUX.name(), null);

        logPath = System.getProperty("user.home") + "\\logs\\" +  ParamConfig.LOG_FILE_NAME;
    }
}
