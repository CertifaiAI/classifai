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
package ai.classifai.ui.launcher;

/**
 * Print Logo on command prompt / terminal
 *
 * @author Kenge
 */

import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;

@Slf4j
public class LogoLauncher {

    public static void print()
    {
        System.out.println("\n");
        System.out.println("   *********  ***          *****     *********  *********  *********  *********    *****    *********  ");
        System.out.println("   *********  ***        *********   ***        ***        *********  *********  *********  *********  ");
        System.out.println("   ***        ***        ***    ***  ***        ***           ***     ***        ***   ***     ***     ");
        System.out.println("   ***        ***        ***    ***  *********  *********     ***     *********  ***   ***     ***     ");
        System.out.println("   ***        ***        **********        ***        ***     ***     *********  *********     ***     ");
        System.out.println("   *********  *********  ***    ***        ***        ***  *********  ***        ***   ***  *********  ");
        System.out.println("   *********  *********  ***    ***  *********  *********  *********  ***        ***   ***  *********  ");
        System.out.println("\n");
    }

    public static Image getClassifaiIcon()
    {
        try
        {
            final Image image = ImageIO.read(WelcomeLauncher.class.getResource( "/console/" + "Classifai_Favicon_Light_BG.jpg"));

            return image;
        }
        catch (Exception e)
        {
            log.info("Error when setting icon: " + e);
        }


        return null;
    }
}
