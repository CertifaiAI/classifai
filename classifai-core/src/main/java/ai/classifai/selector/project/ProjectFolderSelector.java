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
package ai.classifai.selector.project;

import ai.classifai.selector.window.FileSystemStatus;
import ai.classifai.ui.SelectionWindow;
import ai.classifai.ui.launcher.WelcomeLauncher;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;

/**
 * Open browser to select project folder
 *
 * @author codenamewei
 */
@Slf4j
public class ProjectFolderSelector extends SelectionWindow
{
    @Getter private String projectFolderPath = null;
    @Getter private FileSystemStatus fileSystemStatus = FileSystemStatus.DID_NOT_INITIATED;

    public void run()
    {
        if(fileSystemStatus.equals(FileSystemStatus.WINDOW_OPEN))
        {
            log.debug("Project folder selector window is opened. Close that to proceed.");
            return;
        }
        else
        {
            fileSystemStatus = FileSystemStatus.WINDOW_OPEN;
        }

        try
        {
            EventQueue.invokeLater(() -> {

                JFrame frame = initFrame();
                String title = "Select Project Folder";
                JFileChooser chooser = initChooser(JFileChooser.DIRECTORIES_ONLY, title);

                //Important: prevent Welcome Console from popping out
                WelcomeLauncher.setToBackground();

                int res = chooser.showOpenDialog(frame);
                frame.dispose();

                if (res == JFileChooser.APPROVE_OPTION)
                {
                    projectFolderPath = chooser.getSelectedFile().getAbsolutePath();
                    fileSystemStatus = FileSystemStatus.WINDOW_CLOSE_ITEM_SELECTED;
                }
                else
                {
                    fileSystemStatus = FileSystemStatus.WINDOW_CLOSE_NO_ACTION;
                }
            });
        }
        catch (Exception e)
        {
            log.info("ProjectFolderSelector failed to open", e);
        }
    }

}