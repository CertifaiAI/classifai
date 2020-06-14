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

package ai.certifai.selector;

import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.io.File;
import java.util.Arrays;

@Slf4j
public class FolderSelector{
    private static FileNameExtensionFilter imgfilter = new FileNameExtensionFilter("Image Files", "jpg", "png", "gif", "jpeg");

    public void runFolderSelector() {

        try {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    Point pt = MouseInfo.getPointerInfo().getLocation();
                    JFileChooser fc = new JFileChooser() {
                        @Override
                        protected JDialog createDialog(Component parent)
                                throws HeadlessException {
                            JDialog dialog = super.createDialog(parent);
                            dialog.setLocation(pt);
                            dialog.setLocationByPlatform(true);
                            dialog.setAlwaysOnTop(true);
                            return dialog;
                        }
                    };

                    fc.setCurrentDirectory(SelectorHandler.getRootSearchPath());
                    fc.setFileFilter(imgfilter);
                    fc.setDialogTitle("Select Directory");
                    fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);


                    if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
                    {
                        System.out.println(fc.getSelectedFile().getAbsolutePath());
                        File allfiles = fc.getSelectedFile().getAbsoluteFile();
                        SelectorHandler.configureDatabaseUpdate(Arrays.asList(allfiles));
                        SelectorHandler.processSelectorOutput();
                    } else {
                        File nullfiles = null;
                        SelectorHandler.configureDatabaseUpdate(Arrays.asList(nullfiles));
                        SelectorHandler.processSelectorOutput();
                    }
                }
            });
        }
        catch (Exception e)
        {
            SelectorHandler.setWindowState(false);
            log.debug("Select folder failed to open", e);
        }

    }

}


