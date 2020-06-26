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

import ai.certifai.data.type.image.ImageFileType;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;

/**
 * Open browser to select folder with importing list of data points in the folder
 *
 * @author Chiawei Lim
 */
@Slf4j
public class FolderSelector{

    private static FileNameExtensionFilter imgfilter = new FileNameExtensionFilter("Image Files", ImageFileType.getImageFileTypes());

    public void runFolderSelector() {

        try {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    Point pt = MouseInfo.getPointerInfo().getLocation();
                    JFrame jf = new JFrame();
                    jf.setAlwaysOnTop(true);
                    jf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    jf.setLocation(pt);
                    jf.requestFocus();
                    jf.setVisible(false);

                    JFileChooser fc = new JFileChooser() {
                        @Override
                        protected JDialog createDialog(Component parent)
                                throws HeadlessException {
                            JDialog dialog = super.createDialog(parent);
                            dialog.setLocationByPlatform(true);
                            dialog.setAlwaysOnTop(true);
                            return dialog;
                        }
                    };

                    fc.setCurrentDirectory(SelectorHandler.getRootSearchPath());
                    fc.setFileFilter(imgfilter);
                    fc.setDialogTitle("Select Directory");
                    fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    int res = fc.showOpenDialog(jf);
                    java.util.List<File> files = null;
                    jf.dispose();
                    if (res == JFileChooser.APPROVE_OPTION)
                    {
                        File rootFolder =  fc.getSelectedFile().getAbsoluteFile();

                        files = new ArrayList<>(java.util.Arrays.asList(rootFolder));
                    }

                    SelectorHandler.processSelectorOutput(files);
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


