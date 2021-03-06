/*
 * Copyright (c) 2021 CertifAI Sdn. Bhd.
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

import ai.classifai.selector.status.SelectionWindowStatus;
import ai.classifai.ui.launcher.LogoLauncher;
import ai.classifai.ui.launcher.WelcomeLauncher;
import ai.classifai.util.ParamConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;

import static javax.swing.JOptionPane.showMessageDialog;

/**
 * For selection windows initialization and settings
 *
 * @author devenyantis
 */
@Slf4j
public class SelectionWindow {

    // To make sure window open once only
    @Getter @Setter
    protected SelectionWindowStatus windowStatus = SelectionWindowStatus.WINDOW_CLOSE;

    private static JFrame frame = initFrame();

    public boolean isWindowOpen()
    {
        return windowStatus.equals(SelectionWindowStatus.WINDOW_OPEN);
    }

    public static JFrame initFrame()
    {
        Point pt = MouseInfo.getPointerInfo().getLocation();
        JFrame frame = new JFrame();
        frame.setIconImage(LogoLauncher.getClassifaiIcon());

        frame.setAlwaysOnTop(true);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setLocation(pt);
        frame.requestFocus();
        frame.setVisible(false);

        return frame;
    }

    public static JFileChooser initChooser(int mode, String title)
    {
        JFileChooser chooser = new JFileChooser() {
            @Override
            protected JDialog createDialog(Component parent)
                    throws HeadlessException {
                JDialog dialog = super.createDialog(parent);
                dialog.setLocationByPlatform(true);
                dialog.setAlwaysOnTop(true);
                return dialog;
            }
        };

        chooser.setDialogTitle(title);
        chooser.setCurrentDirectory(ParamConfig.getRootSearchPath());
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileSelectionMode(mode);
        chooser.setAcceptAllFileFilterUsed(false);

        return chooser;
    }

    public static void showPopupAndLog(String title, String message, int popupType)
    {
        log.debug(message);
        WelcomeLauncher.setToBackground();
        showMessageDialog(frame, message, title, popupType);
    }

    public void showAbortImportPopup()
    {
        String popupTitle = "Error Opening Window";
        String message = "Another selection window is currently open. Please close to proceed.";
        SelectionWindow.showPopupAndLog(popupTitle, message, JOptionPane.ERROR_MESSAGE);
    }
}
