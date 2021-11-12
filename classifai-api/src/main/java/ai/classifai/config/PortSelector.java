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
package ai.classifai.config;

import ai.classifai.core.util.ParamConfig;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Port Configuration for hosting classifai
 *
 * @author codenamewei
 */
@Slf4j
public class PortSelector {

    public static void configurePort(@NonNull String inputArg)
    {
        if ((inputArg != null) && (inputArg.length() > 0) && (inputArg.matches("[0-9]+")))
        {
            setHostingPort(Integer.parseInt(inputArg));
        }
    }

    private static void setHostingPort(Integer port)
    {
        ParamConfig.setHostingPort(port);
    }
}
