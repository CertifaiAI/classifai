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

import ai.classifai.util.type.OS;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Handler to manage different os
 *
 * @author codenamewei
 */
@Slf4j
@Getter
public class OSManager
{
    private final OS currentOS;

    public OSManager()
    {
        currentOS = getOS(System.getProperty("os.name").toLowerCase());
    }

    private OS getOS(String osPropertyName)
    {
        if (osPropertyName.contains("mac"))
        {
            return OS.MAC;
        }
        else if (osPropertyName.contains("win"))
        {
            return OS.WINDOWS;
        }
        else if (osPropertyName.contains("linux")) //centos, ubuntu
        {
            return OS.LINUX;
        }
        else if (osPropertyName.contains("nix") || osPropertyName.contains("nux") || osPropertyName.contains("aix"))
        {
            return OS.UNIX;
        }
        else if (osPropertyName.contains("sunos"))
        {
            return OS.SOLARIS;
        }
        else
        {
            return OS.NULL;
        }
    }
}