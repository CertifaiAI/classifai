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

import ai.classifai.action.ActionConfig;
import ai.classifai.action.ProjectImport;
import ai.classifai.ui.DesktopUI;
import ai.classifai.ui.component.SelectionWindow;
import ai.classifai.ui.enums.FileSystemStatus;
import ai.classifai.ui.enums.SelectionWindowStatus;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.nio.file.Paths;

/**
 * Open browser to choose for configuration file to import
 *
 * @author codenamewei
 */
@Slf4j
public class ProjectImportSelector extends SelectionWindow {
    private static final FileNameExtensionFilter JSON_FILTER = new FileNameExtensionFilter(
            "Json Files", "json");
    private final ProjectImport projectImport;

    @Getter private FileSystemStatus importFileSystemStatus = FileSystemStatus.DID_NOT_INITIATED;
    @Getter private String projectName = null;

    public ProjectImportSelector(DesktopUI ui, ProjectImport projectImport) {
        super(ui);
        this.projectImport = projectImport;
    }

    public void run()
    {
        try
        {
            EventQueue.invokeLater(() ->
            {
                if(windowStatus.equals(SelectionWindowStatus.WINDOW_CLOSE))
                {
                    windowStatus = SelectionWindowStatus.WINDOW_OPEN;
                    this.importFileSystemStatus = FileSystemStatus.ITERATING_FOLDER;
                    this.projectName = null;

                    JFrame frame = ui.getFrameAtMousePointer();
                    String title = "Select File";
                    JFileChooser chooser = initChooser(JFileChooser.FILES_ONLY, title);
                    chooser.setFileFilter(JSON_FILTER);

                    //Important: prevent Welcome Console from popping out
                    ui.ensureWelcomeLauncherStaysInBackground();

                    int res = chooser.showOpenDialog(frame);
                    frame.dispose();

                    if (res == JFileChooser.APPROVE_OPTION)
                    {
                        windowStatus = SelectionWindowStatus.WINDOW_CLOSE;
                        File jsonFile =  chooser.getSelectedFile().getAbsoluteFile();
                        runApproveOption(jsonFile);
                    }
                    else
                    {
                        windowStatus = SelectionWindowStatus.WINDOW_CLOSE;
                        this.importFileSystemStatus = FileSystemStatus.ABORTED;
                        log.debug("Operation of import project aborted");
                    }
                }
                else
                {
                    showAbortImportPopup();
                }

                windowStatus = SelectionWindowStatus.WINDOW_CLOSE;

            });
        }
        catch (Exception e)
        {
            log.info("ProjectHandler for File type failed to open", e);
        }
    }

    private void runApproveOption(File jsonFile)
    {
        this.importFileSystemStatus = FileSystemStatus.DATABASE_UPDATING;

        ActionConfig.setJsonFilePath(Paths.get(FilenameUtils.getFullPath(jsonFile.toString())).toString());

        log.info("Proceed with importing project with " + jsonFile.getName());

        this.projectName = projectImport.importProjectFile(jsonFile);

        if(this.projectName == null) {
            String mes = "Import project failed.";
            log.debug(mes);
            this.importFileSystemStatus = FileSystemStatus.ABORTED;
        } else {
            String mes = "Import project success.";
            log.debug(mes);
            this.importFileSystemStatus = FileSystemStatus.DATABASE_UPDATED;
        }
    }
}
