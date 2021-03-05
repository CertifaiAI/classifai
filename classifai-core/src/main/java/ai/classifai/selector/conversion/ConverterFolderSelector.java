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
package ai.classifai.selector.conversion;

import ai.classifai.ui.launcher.conversion.ConversionSelection;
import ai.classifai.ui.launcher.conversion.ConverterLauncher;
import ai.classifai.util.ParamConfig;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
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
                    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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

                    //prevent Welcome Console from popping out
                    //WelcomeLauncher.setToBackground();

                    if (res == JFileChooser.APPROVE_OPTION)
                    {
                        File rootFolder =  chooser.getSelectedFile().getAbsoluteFile();

                        if((rootFolder != null) && (rootFolder.exists()))
                        {
                            if(selection.equals(ConversionSelection.INPUT))
                            {
                                ConverterLauncher.setInputFolderPath(rootFolder.getAbsolutePath());
                            }
                            else if(selection.equals(ConversionSelection.OUTPUT))
                            {
                                ConverterLauncher.setOutputFolderPath(rootFolder.getAbsolutePath());
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


