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

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

/**
 * GUI for starting classifai
 *
 * @author Chiawei Lim
 */
@Slf4j
public class WelcomeConsole
{
    private static JFrame frame;
    private static String browserURL;
    private static OSManager osManager;

    final static String BUTTON_PATH;

    private static int PANE_WIDTH = 640;
    private static int PANE_HEIGHT = 480;

    final static int BTN_X_COORD = 217;
    final static int BTN_Y_COORD = 342;

    final static int BTN_WIDTH = 55;
    final static int BTN_HEIGHT = 55;

    final static int X_GAP = 88;

    static
    {
        BUTTON_PATH = "/console/";
        browserURL = "http://localhost:" + ParamConfig.getHostingPort();
        osManager = new OSManager();
    }

    public static void start()
    {
        frame = new JFrame("Welcome to Classifai");

        JPanel panel = new JPanel(new BorderLayout());
        panel.setSize(PANE_WIDTH, PANE_HEIGHT);

        JButton openButton = getButton("Open_Button.png", "Open");
        openButton.setBounds(BTN_X_COORD + X_GAP * 0, BTN_Y_COORD, BTN_WIDTH, BTN_HEIGHT);

        openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                BrowserHandler.openOnBrowser(browserURL, osManager);
            }
        });

        JButton closeButton = getButton("Close_Button.png", "Close");
        closeButton.setBounds(BTN_X_COORD + X_GAP * 1, BTN_Y_COORD, BTN_WIDTH, BTN_HEIGHT);

        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        JButton acknowledgementButton = getButton("Acknowledge_Button.png", "License");
        acknowledgementButton.setBounds(BTN_X_COORD + (X_GAP * 2) - 2, BTN_Y_COORD, BTN_WIDTH, BTN_HEIGHT);

        panel.add(openButton);
        panel.add(closeButton);
        panel.add(acknowledgementButton);

        JLabel backgroundLabel = getBackground("Classifai_WelcomeHandler_big.jpg");
        if(backgroundLabel != null) panel.add(backgroundLabel); // NEED TO BE LAST

        frame.add(panel);

        /*frame.getContentPane().setPreferredSize(new Dimension(PANE_WIDTH, PANE_HEIGHT));

        int frameWidth = PANE_WIDTH - (frame.getInsets().left + frame.getInsets().right);
        int frameHeight = PANE_HEIGHT - (frame.getInsets().top + frame.getInsets().bottom);

        frame.setPreferredSize(new Dimension(frameWidth, frameHeight));
         */

        frame.pack();
        frame.setLocationRelativeTo(null);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setResizable(false);

        frame.setVisible(true);
    }

    public static void setToBackground()
    {
        frame.setState(Frame.ICONIFIED);
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
            BufferedImage oriImg = ImageIO.read(WelcomeConsole.class.getResource(BUTTON_PATH + fileName));

            BufferedImage img = resize(oriImg, PANE_WIDTH, PANE_HEIGHT);

            JLabel bgLabel = new JLabel(new ImageIcon(img));
            bgLabel.setLayout(null);
            bgLabel.setBounds(0,0, PANE_WIDTH, PANE_HEIGHT);

            return bgLabel;
        }
        catch(Exception e) {

            e.printStackTrace();
        }

        return null;

    }

    public static BufferedImage resize(BufferedImage img, int newW, int newH) {
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return dimg;
    }
}