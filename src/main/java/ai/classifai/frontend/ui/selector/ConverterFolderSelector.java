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
package ai.classifai.frontend.ui.selector;

import ai.classifai.core.utility.ParamConfig;
import ai.classifai.frontend.ui.ConversionSelection;
import ai.classifai.frontend.ui.DesktopUI;
import ai.classifai.frontend.ui.launcher.ConverterLauncher;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Open browser to select folder for conversion
 *
 * @author codenamewei
 */
@Slf4j
public class ConverterFolderSelector {

    private final DesktopUI ui;
    private final ConverterLauncher converterLauncher;

    public ConverterFolderSelector(DesktopUI ui, ConverterLauncher converterLauncher){
        this.ui = ui;
        this.converterLauncher = converterLauncher;
    }

    public void run(ConversionSelection selection)
    {
        try
        {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {

                    Point pt = MouseInfo.getPointerInfo().getLocation();
                    JFrame frame = new JFrame();
                    frame.setAlwaysOnTop(true);
                    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                    frame.setLocation(pt);
                    frame.requestFocus();
                    frame.setVisible(false);

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
                    chooser.setDialogTitle("Select Directory");
                    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    int res = chooser.showOpenDialog(frame);
                    frame.dispose();

                    ui.ensureWelcomeLauncherStaysInBackground();

                    if (res == JFileChooser.APPROVE_OPTION)
                    {
                        File rootFolder =  chooser.getSelectedFile().getAbsoluteFile();

                        if((rootFolder != null) && (rootFolder.exists()))
                        {
                            if(selection.equals(ConversionSelection.INPUT))
                            {
                                converterLauncher.setInputFolderPath(rootFolder.getAbsolutePath());
                            }
                            else if(selection.equals(ConversionSelection.OUTPUT))
                            {
                                converterLauncher.setOutputFolderPath(rootFolder.getAbsolutePath());
                            }
                        }

                    }
                }
            });
        }
        catch (Exception e)
        {
            log.info("Error of FolderSelector in handling conversion: ", e);
        }

    }

}


