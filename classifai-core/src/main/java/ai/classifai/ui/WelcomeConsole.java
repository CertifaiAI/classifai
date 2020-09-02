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

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URL;


/**
 * GUI for starting classifai
 *
 * @author Chiawei Lim
 */
@Slf4j
public class WelcomeConsole
{
    private static String browserURL;
    private static OSManager osManager;

    final static String BUTTON_PATH;

    final static int FRAME_WIDTH = 1024;
    final static int FRAME_HEIGHT = 768;

    final static int BTN_X_COORD = 180;
    final static int BTN_Y_COORD = 550;

    final static int BTN_WIDTH = 100;
    final static int BTN_HEIGHT = 100;

    final static int X_GAP = 260;

    static
    {
        browserURL = "http://localhost:" + ParamConfig.getHostingPort();
        osManager = new OSManager();
        BUTTON_PATH = "/console/";
    }

    public static void start()
    {
        JFrame frame = new JFrame("Classifai");

        JButton openButton = getButton("openButton.png", "Open");
        openButton.setBounds(BTN_X_COORD + X_GAP * 0, BTN_Y_COORD, BTN_WIDTH, BTN_HEIGHT);

        openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                BrowserHandler.openOnBrowser(browserURL, osManager);
            }
        });

        JButton closeButton = getButton("closeButton.png", "Close");
        closeButton.setBounds(BTN_X_COORD + X_GAP * 1, BTN_Y_COORD, BTN_WIDTH, BTN_HEIGHT);

        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        JButton acknowledgementButton = getButton("licenseButton.png", "License");
        acknowledgementButton.setBounds(BTN_X_COORD + X_GAP * 2, BTN_Y_COORD, BTN_WIDTH, BTN_HEIGHT);

        frame.add(openButton);
        frame.add(closeButton);
        frame.add(acknowledgementButton);

        JLabel backgroundLabel = getBackground("welcomeConsole.png");
        if(backgroundLabel != null) frame.add(backgroundLabel); // NEED TO BE LAST

        frame.setSize(FRAME_WIDTH, FRAME_HEIGHT + 20);
        frame.setLocationRelativeTo(null);
        frame.setLayout(null);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setResizable(false);
    }

    private static JButton getButton(String fileName, String altText)
    {
        JButton button = new JButton();

        try {

            Image img = ImageIO.read(WelcomeConsole.class.getResource(BUTTON_PATH + fileName));

            Image scaledImg = img.getScaledInstance(BTN_WIDTH, BTN_HEIGHT, Image.SCALE_SMOOTH);

            button.setIcon(new ImageIcon(scaledImg));

        }
        catch (Exception e)
        {
            button = new JButton(altText);//altText will be used if icon not found
            log.error("Image for button failed to configured. ", e);
        }

        return button;
    }


    private static JLabel getBackground(String fileName)
    {
        try
        {
            Image img = ImageIO.read(WelcomeConsole.class.getResource(BUTTON_PATH + fileName));

            Image scaledImg = img.getScaledInstance(FRAME_WIDTH, FRAME_HEIGHT, Image.SCALE_SMOOTH);

            JLabel bgLabel = new JLabel(new ImageIcon(scaledImg));
            bgLabel.setLayout(null);
            bgLabel.setBounds(0,0, FRAME_WIDTH, FRAME_HEIGHT);

            return bgLabel;
        }
        catch(Exception e) {

            e.printStackTrace();
        }

        return null;

    }

}