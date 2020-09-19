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

package ai.classifai.selector;

import ai.classifai.data.type.image.ImageFileType;
import ai.classifai.selector.filesystem.FileSystemStatus;
import ai.classifai.server.ParamConfig;
import ai.classifai.ui.WelcomeLauncher;
import ai.classifai.util.ProjectHandler;
import ai.classifai.util.image.ImageHandler;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;

/**
 * Open browser to select files
 *
 * @author Chiawei Lim
 */
@Slf4j
public class FileSelector{
    private static FileNameExtensionFilter imgfilter = new FileNameExtensionFilter("Image Files", ImageFileType.getImageFileTypes());

    public void runFileSelector(@NonNull Integer projectID)
    {
        ProjectHandler.setIsCurrentFileSystemDBUpdating(true);

        try {
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

                    chooser.setCurrentDirectory(ParamConfig.ROOT_SEARCH_PATH);
                    chooser.setFileFilter(imgfilter);
                    chooser.setDialogTitle("Select Files");
                    chooser.setMultiSelectionEnabled(true);
                    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    int res = chooser.showOpenDialog(frame);
                    frame.dispose();

                    //prevent Welcome Console from popping out
                    WelcomeLauncher.setToBackground();

                    if (res == JFileChooser.APPROVE_OPTION)
                    {
                        java.util.List<File> files = new ArrayList<>(java.util.Arrays.asList(chooser.getSelectedFiles()));

                        if((files != null) && (!files.isEmpty()) && (files.get(0) != null))
                        {

                            ProjectHandler.getProjectLoader(projectID).setFileSystemStatus(FileSystemStatus.WINDOW_CLOSE_LOADING_FILES);

                            ImageHandler.processFile(projectID, files);
                        }
                        else
                        {
                            ProjectHandler.getProjectLoader(projectID).setFileSystemStatus(FileSystemStatus.WINDOW_CLOSE_DATABASE_NOT_UPDATED);
                            ProjectHandler.setIsCurrentFileSystemDBUpdating(false);
                        }
                    }
                    else
                    {
                        ProjectHandler.getProjectLoader(projectID).setFileSystemStatus(FileSystemStatus.WINDOW_CLOSE_DATABASE_NOT_UPDATED);
                        ProjectHandler.setIsCurrentFileSystemDBUpdating(false);
                    }

                }
            });
        }
        catch (Exception e){

            log.info("ProjectHandler for File type failed to open", e);
        }

    }

}


