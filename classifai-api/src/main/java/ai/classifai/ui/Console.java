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

import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Console
{
    final static boolean shouldFill = true;
    final static boolean RIGHT_TO_LEFT = false;

    final static int FRAME_WIDTH = 500;
    final static int FRAME_HEIGHT = 300;

    final static int PADDING_HEIGHT = 40;

    public static void start()
    {
        JFrame frame = new JFrame("Classifai");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);

        addComponentsToPane(frame.getContentPane());

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }


    public static void addComponentsToPane(Container pane)
    {
        if (RIGHT_TO_LEFT) {
            pane.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        }

        pane.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        if (shouldFill) {
            //natural height, maximum width
            c.fill = GridBagConstraints.HORIZONTAL;
        }

        JButton openButton = new JButton("Open in Chromium");
        openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openBrowser();
            }
        });

        c.weightx = 0.5;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.ipady = PADDING_HEIGHT;
        c.gridx = 0;
        c.gridy = 0;
        pane.add(openButton, c);

        JButton closeButton = new JButton("Close");

        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        c.fill = GridBagConstraints.HORIZONTAL;
        c.ipady = PADDING_HEIGHT;      //make this component tall
        c.weightx = 0.5;
        c.gridx = 1;
        c.gridy = 0;
        pane.add(closeButton, c);

    }

    static void openBrowser()
    {
        String url = "http://localhost:9999";// + config().getInteger("http.port"); //FIX THIS

        try
        {
            //delay opening chrome for 3 seconds
            TimeUnit.SECONDS.sleep(2);
        }
        catch(InterruptedException e)
        {
            log.debug("Exception while pause, ", e);
        }

        BrowserHandler.openOnBrowser(url);
    }
}