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

import com.formdev.flatlaf.FlatDarkLaf;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;

/**
 * LookFeelSetter
 *
 * @author codenamewei
 */
@Slf4j
public class LookFeelSetter
{
    public static void setLightMode()
    {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e)
        {
            log.error("Error in setting UIManager: ", e);
        }
    }

    public static void setDarkMode()
    {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        }
        catch (Exception e)
        {
            log.error("Error in setting ToolFileSelector look: ", e);
        }
    }
}
