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
package ai.classifai.ui.component;

import ai.classifai.ui.launcher.LogoLauncher;
import ai.classifai.util.ParamConfig;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;

/**
 * Selection window setting and process
 *
 * @author devenyantis
 */
public class SelectionWindow {

    public enum WindowStatus
    {
        WINDOW_OPEN,
        WINDOW_CLOSE
    }
    @Getter @Setter
    private static WindowStatus windowStatus = WindowStatus.WINDOW_CLOSE;

    private static final FileNameExtensionFilter imgFilter = new FileNameExtensionFilter(
            "Json Files", "json");

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

    public static JFileChooser initChooser()
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

        chooser.setCurrentDirectory(ParamConfig.getRootSearchPath());
        chooser.setFileFilter(imgFilter);
        chooser.setDialogTitle("Select Files");
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);

        return chooser;
    }

}
