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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.InputStream;

@Slf4j
public class WelcomeConsole
{
    private static String browserURL;
    private static OSManager osManager;

    final static int FRAME_WIDTH = 600;
    final static int FRAME_HEIGHT = 350;

    final static int BTN_X_COORD = 80;
    final static int BTN_Y_COORD = 220;

    final static int BTN_WIDTH = 100;
    final static int BTN_HEIGHT = 50;

    final static int X_GAP = 180;

    static
    {
        browserURL = "http://localhost:" + ParamConfig.getHostingPort();
        osManager = new OSManager();
    }

    public static String getItemPath(String fileName)
    {
        InputStream imgPathStream = WelcomeConsole.class.getClassLoader().getResourceAsStream("console" + File.separator + fileName);
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

        frame.add(getOpenButton());

        frame.add(getCloseButton());

        frame.add(getAcknowledgementButton());

        setBackground(frame); // NEED TO BE LAST

        frame.setSize(FRAME_WIDTH, FRAME_HEIGHT + 20);
        frame.setLocationRelativeTo(null);
        frame.setLayout(null);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setResizable(false);
    }

    private static void setBackground(JFrame frame)
    {
        String bgImagePath = getItemPath("welcomeConsole.png");

        ImageIcon bgOriIcon = new ImageIcon(bgImagePath);

        Image bgOriIconImage = bgOriIcon.getImage();

        Image scaledBgIconImage = bgOriIconImage.getScaledInstance(FRAME_WIDTH, FRAME_HEIGHT, Image.SCALE_SMOOTH);

        JLabel bgLabel = new JLabel(new ImageIcon(scaledBgIconImage));
        bgLabel.setLayout(null);
        bgLabel.setBounds(0,0, FRAME_WIDTH, FRAME_HEIGHT);
        frame.add(bgLabel);
    }

    private static JButton getButtonFromPath(String imageFileName)
    {
        String imagePath = getItemPath(imageFileName);

        Image buttonIcon = new ImageIcon(imagePath).getImage();

        Image scaledButtonIcon = buttonIcon.getScaledInstance(BTN_WIDTH, BTN_HEIGHT, Image.SCALE_SMOOTH);

        return new JButton(new ImageIcon(scaledButtonIcon));
    }

    private static JButton getOpenButton()
    {
        JButton openButton = getButtonFromPath("openButton.png");

        openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ChromiumHandler.openOnBrowser(browserURL, osManager);
            }
        });

        openButton.setBounds(BTN_X_COORD, BTN_Y_COORD,BTN_WIDTH, BTN_HEIGHT);//x axis, y axis, width, height

        return openButton;
    }

    private static JButton getCloseButton()
    {
        JButton closeButton = getButtonFromPath("openButton.png"); //FIXME

        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        closeButton.setBounds(BTN_X_COORD + X_GAP, BTN_Y_COORD,BTN_WIDTH, BTN_HEIGHT);//x axis, y axis, width, height

        return closeButton;
    }

    private static JButton getAcknowledgementButton()
    {
        JButton closeButton = getButtonFromPath("openButton.png"); //FIXME

        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        closeButton.setBounds(BTN_X_COORD + X_GAP * 2, BTN_Y_COORD,BTN_WIDTH, BTN_HEIGHT);//x axis, y axis, width, height

        return closeButton;
    }


}