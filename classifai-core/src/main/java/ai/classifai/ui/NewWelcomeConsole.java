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

import ai.classifai.server.ParamConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.InputStream;

@Slf4j
public class NewWelcomeConsole
{
    private static String browserURL;
    private static OSManager osManager;

    final static int FRAME_WIDTH = 600;
    final static int FRAME_HEIGHT = 350;

    final static int BTN_X_COORD = 330;
    final static int BTN_Y_COORD = 350;

    static
    {
        browserURL = "http://localhost:" + ParamConfig.getHostingPort();
        osManager = new OSManager();
    }

    public static String getItemPath(String fileName)
    {
        InputStream imgPathStream = NewWelcomeConsole.class.getClassLoader().getResourceAsStream("console" + File.separator + fileName);
        File imgFile = new File(System.getProperty("java.io.tmpdir") + File.separator +  fileName);

        if(imgFile.exists() == false)
        {
            System.out.println("Welcome console file should not be empty. ");
            log.error("Welcome console file should not be empty. ", fileName);
            return null;
        }


        try
        {
            FileUtils.copyInputStreamToFile(imgPathStream, imgFile);
            return imgFile.getAbsolutePath();

        }
        catch(Exception e)
        {
            log.error("Error when loading welcome console file", e);
        }

        return null;
    }

    public static void start()
    {
        setUpConsole();
    }

    public static void setUpConsole()
    {
        JFrame frame = new JFrame("Classifai");

        JButton openButton = new JButton(getOpenButton());
        openButton.setBounds(100, 100,100, 40);//x axis, y axis, width, height

        frame.add(openButton);

        setBackground(frame); // NEED TO BE LAST

    }

    public static void setBackground(JFrame frame)
    {
        String bgImagePath = getItemPath("welcomeConsole.png");

        ImageIcon bgOriIcon = new ImageIcon(bgImagePath);

        Image bgOriIconImage = bgOriIcon.getImage();

        Image scaledBgIconImage = bgOriIconImage.getScaledInstance(FRAME_WIDTH, FRAME_HEIGHT, Image.SCALE_SMOOTH);

        JLabel bgLabel = new JLabel(new ImageIcon(scaledBgIconImage));
        bgLabel.setLayout(null);
        bgLabel.setBounds(0,0, FRAME_WIDTH, FRAME_HEIGHT);
        frame.add(bgLabel);

        frame.setSize(FRAME_WIDTH, FRAME_HEIGHT + 20);
        frame.setLayout(null);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setResizable(false);
    }

    public static ImageIcon getOpenButton()
    {
        String openButtonImagePath = getItemPath("openButton.png");

        Image openButtonIcon = new ImageIcon(openButtonImagePath).getImage();

        Image scaledOpenButtonIcon = openButtonIcon.getScaledInstance(100, 100, Image.SCALE_SMOOTH);

        return new ImageIcon(scaledOpenButtonIcon);

    }
}