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

import ai.classifai.util.type.OS;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default text editor in current OS to open log
 *
 * @author codenamewei
 */
@Slf4j
public class LogHandler
{
    private static Map<OS, List<String>> textEditorKey;

    static
    {
        textEditorKey = new HashMap<>();

        List winEditorPath = new ArrayList<String>();

        winEditorPath.add("C:\\Windows\\System32\\notepad.exe");
        winEditorPath.add("C:\\Windows\\notepad.exe");

        List linuxEditorPath = new ArrayList<String>();

        linuxEditorPath.add("default");

        List macEditorPath = new ArrayList<String>();
        macEditorPath.add("default");

        textEditorKey.put(OS.MAC, macEditorPath);
        textEditorKey.put(OS.WINDOWS, winEditorPath);
        textEditorKey.put(OS.LINUX, linuxEditorPath);
    }

    public static List getOSEditor(@NonNull OS os)
    {
        if(!textEditorKey.containsKey(os)) return null;

        return textEditorKey.get(os);
    }
}