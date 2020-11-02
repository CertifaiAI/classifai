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
package ai.classifai.ui.launcher;

import ai.classifai.MainVerticle;
import ai.classifai.ui.button.BrowserHandler;
import ai.classifai.ui.button.LogHandler;
import ai.classifai.ui.button.OSManager;
import ai.classifai.ui.button.ProgramOpener;
import ai.classifai.ui.launcher.conversion.ConverterLauncher;
import ai.classifai.util.ParamConfig;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

/**
 * GUI for starting classifai
 *
 * @author codenamewei
 */
@Slf4j
public class WelcomeLauncher extends JFrame
{
    private final static OSManager OS_MANAGER;

    private final static String BUTTON_PATH = "/console/";

    private final static int PANE_WIDTH = 640;
    private final static int PANE_HEIGHT = 480;

    private final static int BTN_X_COORD = 217;
    private final static int BTN_Y_COORD = 342;

    private final static int BTN_WIDTH = 55;
    private final static int BTN_HEIGHT = 55;

    private final static int X_GAP = 88;

    private static JFrame mainFrame;

    private static JLabel runningStatusText;
    private static JLabel runningStatusLabel;
    private static JButton openButton;
    private static JButton converterButton;
    private static JButton logButton;
    private static JLabel backgroundLabel;


    static
    {
        OS_MANAGER = new OSManager();

        configure();
    }

    public static void setToBackground()
    {
        mainFrame.setState(Frame.ICONIFIED);
    }

    private static void configure()
    {
        setUpFrame();
        setRunningStatus(RunningStatus.STARTING);
        setUpOpenButton();
        setUpConverterButton();
        setUpLogButton();

        setUpBackground();
    }



    private static void setUpFrame()
    {
        mainFrame = new JFrame("Welcome to Classifai");

        //to have verticles calling stop() before program exit
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e)
            {
                MainVerticle.closeVerticles();

                log.info("Classifai closed successfully...");

                System.exit(0);
            }
        });
    }


    public static void start()
    {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(PANE_WIDTH, PANE_HEIGHT));

        panel.add(openButton);

        panel.add(converterButton);

        panel.add(logButton);

        panel.add(runningStatusLabel);
        panel.add(runningStatusText);

        if(backgroundLabel != null) panel.add(backgroundLabel);

        mainFrame.setIconImage(LogoHandler.getClassifaiIcon());

        mainFrame.add(panel);

        mainFrame.pack();
        mainFrame.setLocationRelativeTo(null);

        mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        mainFrame.setResizable(false);

        mainFrame.setVisible(true);
    }

    private static void setUpOpenButton()
    {
        openButton = getButton("Open_Button.png", "Open");
        openButton.setBounds(BTN_X_COORD + X_GAP * 0, BTN_Y_COORD, BTN_WIDTH, BTN_HEIGHT);

        openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ProgramOpener.launch(OS_MANAGER.getCurrentOS(), BrowserHandler.getBrowserKey(), BrowserHandler.getBrowserURL(), true);
            }
        });
    }

    private static void setUpConverterButton()
    {
        converterButton = getButton("Close_Button.png", "Close");
        converterButton.setBounds(BTN_X_COORD + X_GAP * 1, BTN_Y_COORD, BTN_WIDTH, BTN_HEIGHT);

        converterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                new ConverterLauncher().launch();
            }
        });

    }

    private static void setUpLogButton()
    {
        logButton = getButton("Log_Button.png", "License");
        logButton.setBounds(BTN_X_COORD + (X_GAP * 2) - 2, BTN_Y_COORD, BTN_WIDTH, BTN_HEIGHT);

        logButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                ProgramOpener.launch(OS_MANAGER.getCurrentOS(), LogHandler.getTextEditorKey(), ParamConfig.getLogFilePath(), false);
            }
        });
    }

    private static void setUpBackground()
    {
        try
        {
            BufferedImage oriImg = ImageIO.read(WelcomeLauncher.class.getResource(BUTTON_PATH + "Classifai_WelcomeHandler_big.jpg"));

            BufferedImage img = resize(oriImg, PANE_WIDTH, PANE_HEIGHT);

            backgroundLabel = new JLabel(new ImageIcon(img));
            backgroundLabel.setLayout(null);
            backgroundLabel.setBounds(0,0, PANE_WIDTH, PANE_HEIGHT);
        }
        catch(Exception e) {

            e.printStackTrace();
        }
    }

    private static JButton getButton(String fileName, String altText)
    {
        JButton button = new JButton();

        try {

            Image img = ImageIO.read(WelcomeLauncher.class.getResource(BUTTON_PATH + fileName));

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

    public static void setRunningStatus(RunningStatus status)
    {
        //icon
        setRunningStatusIcon(status);

        //text
        if(runningStatusText == null)
        {
            runningStatusText = new JLabel(status.getText());
            runningStatusText.setFont(new Font("DialogInput", Font.BOLD, 16)); //Serif, SansSerif, Monospaced, Dialog, and DialogInput.
            runningStatusText.setForeground(Color.lightGray);
            runningStatusText.setBounds(30, 200, PANE_WIDTH, PANE_HEIGHT);
        }
        else
        {
            String text = status.getText();
            runningStatusText.setText(text);
        }
    }

    private static void setRunningStatusIcon(RunningStatus status)
    {
        String imageName = null;

        if(status.equals(RunningStatus.STARTING))
        {
            imageName ="red.png";
        }
        else if(status.equals(RunningStatus.RUNNING))
        {
            imageName = "green.png";
        }
        else
        {
            log.info("Running status icon could not be found");
        }

        try
        {
            BufferedImage oriImg = ImageIO.read(WelcomeLauncher.class.getResource(BUTTON_PATH + imageName));

            BufferedImage img = resize(oriImg, 10, 10);

            ImageIcon icon = new ImageIcon(img);

            if(runningStatusLabel == null)
            {
                runningStatusLabel = new JLabel(icon);
                runningStatusLabel.setBounds(0,0,  40, 884 );
            }
            else
            {
                runningStatusLabel.setIcon(icon);
            }


        }
        catch(Exception e) {

            log.info("Could not set up running status icon properly: ", e);
        }
    }

    private static BufferedImage resize(BufferedImage img, int newW, int newH) {
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return dimg;
    }

}