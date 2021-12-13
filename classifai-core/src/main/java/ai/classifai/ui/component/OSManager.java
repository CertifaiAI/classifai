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

import ai.classifai.ui.component.os.*;
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
    private final static OS os = getOS();

    public static boolean openLogInEditor(){
        return os.openLogInEditor();
    }

    public static boolean openBrowser(){
        return os.openBrowser();
    }

    private static OS getOS()
    {
        String osPropertyName = System.getProperty("os.name").toLowerCase();
        if (osPropertyName.contains("mac"))
        {
            return new Mac();
        }
        else if (osPropertyName.contains("win"))
        {
            return new Windows();
        }
        else if (osPropertyName.contains("linux")) //centos, ubuntu
        {
            return new Linux();
        }
        else
        {
            return new UnsupportedOS();
        }
    }

}