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
package ai.classifai.ui.selector.project;

import ai.classifai.ui.DesktopUI;
import ai.classifai.ui.component.SelectionWindow;
import ai.classifai.ui.enums.SelectionWindowStatus;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Open browser to select project folder
 *
 * @author codenamewei
 */
@Slf4j
public class ProjectFolderSelector extends SelectionWindow
{
    private File projectFolderPath = null;

    public ProjectFolderSelector(DesktopUI ui) {
        super(ui);
    }

    public String getProjectFolderPath()
    {
        return (projectFolderPath != null) ? projectFolderPath.getAbsolutePath() : "";
    }

    public void run()
    {
        try
        {
            EventQueue.invokeLater(() -> {

                if(windowStatus.equals(SelectionWindowStatus.WINDOW_CLOSE))
                {
                    windowStatus = SelectionWindowStatus.WINDOW_OPEN;

                    projectFolderPath = null;

                    JFrame frame = ui.getFrameAtMousePointer();
                    String title = "Select Project Folder";
                    JFileChooser chooser = initChooser(JFileChooser.DIRECTORIES_ONLY, title);

                    //Important: prevent Welcome Console from popping out
                    ui.ensureWelcomeLauncherStaysInBackground();

                    int res = chooser.showOpenDialog(frame);
                    frame.dispose();

                    if (res == JFileChooser.APPROVE_OPTION)
                    {
                        projectFolderPath = chooser.getSelectedFile();
                    }
                    else
                    {
                        log.debug("Operation of import project aborted");
                    }

                    windowStatus = SelectionWindowStatus.WINDOW_CLOSE;
                }
                else
                {
                    showAbortImportPopup();
                }
            });
        }
        catch (Exception e)
        {
            log.info("LabelFileSelector failed to open", e);
        }
    }
}


