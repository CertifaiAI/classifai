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


import ai.classifai.ui.component.LookFeelSetter;
import ai.classifai.ui.component.OSManager;
import ai.classifai.ui.enums.RunningStatus;
import ai.classifai.ui.launcher.conversion.ConverterLauncher;
import ai.classifai.ui.utils.UIResources;
import ai.classifai.util.ParamConfig;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.Objects;

import static javax.swing.JOptionPane.showMessageDialog;

/**
 * GUI for starting classifai
 *
 * @author codenamewei
 */
@Slf4j
public class WelcomeLauncher extends JFrame
{
    private final Runnable shutdownServerCallback;
    private JFrame mainFrame;

    private final int PANE_WIDTH = 640;
    private final int PANE_HEIGHT = 480;

    private final int BTN_X_COORD = 217;
    private final int BTN_Y_COORD = 342;

    private final int BTN_WIDTH = 55;
    private final int BTN_HEIGHT = 55;

    private final int X_GAP = 88;

    private final String BROWSER_FAILED_MESSAGE = "Initialization of url failed.\n" +
        "Open classifai in chrome/firefox with http://localhost:" + ParamConfig.getHostingPort();
    private final String LOG_FAILED_MESSAGE = "Log file failed to open in editor.\n" +
            "Find the log file in " + ParamConfig.getLogFilePath();;

    private JLabel runningStatusText;
    private JLabel runningStatusLabel;
    private JButton openButton;
    private JButton converterButton;
    private JButton logButton;
    private JLabel backgroundLabel;

    private ImageIcon browserNotFoundIcon;

    private final ConverterLauncher converterLauncher;


    public WelcomeLauncher(Runnable shutdownServerCallback, ConverterLauncher converterLauncher){
        this.shutdownServerCallback = shutdownServerCallback;
        this.converterLauncher = converterLauncher;

        configure();
    }

    public void setToBackground()
    {
        mainFrame.setState(Frame.ICONIFIED);
    }

    private void configure()
    {
        setUpFrame();
        setRunningStatus(RunningStatus.STARTING);

        LookFeelSetter.setLightMode(); //hack to prevent rim around the button
        setUpOpenButton();
        setUpConverterButton();
        setUpLogButton();
        LookFeelSetter.setDarkMode(); //hack to prevent rim around the button

        setUpBackground();

        browserNotFoundIcon = new ImageIcon(UIResources.getDarkClassifaiIcon());
    }

    private void setUpFrame()
    {
        mainFrame = new JFrame("Welcome to Classifai");

        //to have verticles calling stop() before program exit
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e)
            {
                shutdownServerCallback.run();

                log.info("Classifai closed successfully...");

                System.exit(0);
            }
        });
    }

    public void start()
    {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(PANE_WIDTH, PANE_HEIGHT));

        panel.add(openButton);
        panel.add(converterButton);
        panel.add(logButton);

        panel.add(runningStatusLabel);
        panel.add(runningStatusText);

        if (backgroundLabel != null) panel.add(backgroundLabel);

        mainFrame.setIconImage(UIResources.getClassifaiIcon());
        mainFrame.add(panel);
        mainFrame.pack();
        mainFrame.setLocationRelativeTo(null);

        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        mainFrame.setResizable(false);

        mainFrame.setVisible(true);
    }


    private void setUpOpenButton()
    {
        openButton = getButton(UIResources.getOpenButton(), "Open");
        openButton.setBounds(BTN_X_COORD + X_GAP * 0, BTN_Y_COORD, BTN_WIDTH, BTN_HEIGHT);

        openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                boolean isOpen = OSManager.openBrowser();

                if (!isOpen)
                {
                    failToOpenProgramPathMessage(BROWSER_FAILED_MESSAGE);
                }
            }
        });
    }

    private void setUpConverterButton()
    {
        converterButton = getButton(UIResources.getConfigButton(), "Conversion");
        converterButton.setBounds(BTN_X_COORD + X_GAP * 1, BTN_Y_COORD, BTN_WIDTH, BTN_HEIGHT);

        converterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!converterLauncher.isOpened())
                {
                    converterLauncher.launch();
                }
            }
        });
    }

    private void setUpLogButton()
    {
        logButton = getButton(UIResources.getLogButton(), "Logs");
        logButton.setBounds(BTN_X_COORD + (X_GAP * 2) - 2, BTN_Y_COORD, BTN_WIDTH, BTN_HEIGHT);

        logButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean isOpen = OSManager.openLogInEditor();

                if (!isOpen)
                {
                    failToOpenProgramPathMessage(LOG_FAILED_MESSAGE);
                }
            }
        });
    }

    private void setUpBackground()
    {
        try
        {
            Image oriImg = UIResources.getBackground();

            BufferedImage img = resize(oriImg, PANE_WIDTH, PANE_HEIGHT);

            backgroundLabel = new JLabel(new ImageIcon(img));
            backgroundLabel.setLayout(null);
            backgroundLabel.setBounds(0,0, PANE_WIDTH, PANE_HEIGHT);
        }
        catch (Exception e)
        {
            log.info("Exception when setting WelcomeLauncher background: ", e);
        }
    }

    public void setRunningStatus(RunningStatus status)
    {
        //icon
        setRunningStatusIcon(status);

        //text
        if (runningStatusText == null)
        {
            runningStatusText = new JLabel(status.getText());
            runningStatusText.setFont(new Font("SansSerif", Font.BOLD, 14)); //Serif, SansSerif, Monospaced, Dialog, and DialogInput.
            runningStatusText.setForeground(Color.lightGray);
            runningStatusText.setBounds(52, 196, PANE_WIDTH, PANE_HEIGHT);
        }
        else
        {
            String text = status.getText();
            runningStatusText.setText(text);
        }
    }

    private JButton getButton(Image buttonImage, String altText)
    {
        JButton button = new JButton();

        try
        {
            Image scaledImg = buttonImage.getScaledInstance(BTN_WIDTH, BTN_HEIGHT, Image.SCALE_SMOOTH);
            button.setIcon(new ImageIcon(scaledImg));

        }
        catch (Exception e)
        {
            button = new JButton(altText);//altText will be used if icon not found
            log.error("Image for button failed to configured. ", e);
        }

        return button;
    }


    private void setRunningStatusIcon(RunningStatus status)
    {
        Image image = null;

        if (status.equals(RunningStatus.STARTING))
        {
            image = UIResources.getRedLight();
        }
        else if (status.equals(RunningStatus.RUNNING))
        {
            image = UIResources.getGreenLight();
        }
        else
        {
            log.info("Running status icon could not be found");
        }

        BufferedImage img = resize(Objects.requireNonNull(image), 10, 10);

        ImageIcon icon = new ImageIcon(img);

        if (runningStatusLabel == null)
        {
            runningStatusLabel = new JLabel(icon);
            runningStatusLabel.setBounds(0,0, 82, 874);
        }
        else
        {
            runningStatusLabel.setIcon(icon);
        }

    }

    private BufferedImage resize(Image img, int newW, int newH)
    {
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return dimg;
    }

    private void failToOpenProgramPathMessage(String message)
    {
        log.info(message);
        showMessageDialog(null, message,
                "Oops!", JOptionPane.INFORMATION_MESSAGE, browserNotFoundIcon);
    }
}