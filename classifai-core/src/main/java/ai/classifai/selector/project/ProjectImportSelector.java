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

import ai.classifai.action.ActionConfig;
import ai.classifai.action.ProjectImport;
import ai.classifai.selector.filesystem.FileSystemStatus;
import ai.classifai.ui.SelectionWindow;
import ai.classifai.ui.launcher.WelcomeLauncher;
import lombok.Getter;
import lombok.Setter;
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

    @Getter
    @Setter
    private static FileSystemStatus importFileSystemStatus = FileSystemStatus.DID_NOT_INITIATE;

    @Getter
    @Setter
    private static String importErrorMessage;

    private static final FileNameExtensionFilter imgFilter = new FileNameExtensionFilter(
            "Json Files", "json");

    public void run()
    {
        try
        {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    clearImportErrorMessage();
                    if(windowStatus.equals(ImportSelectionWindowStatus.WINDOW_CLOSE))
                    {
                        windowStatus = ImportSelectionWindowStatus.WINDOW_OPEN;
                        setImportFileSystemStatus(FileSystemStatus.WINDOW_OPEN);

                        JFrame frame = initFrame();
                        String title = "Select File";
                        JFileChooser chooser = initChooser(JFileChooser.FILES_ONLY, title);
                        chooser.setFileFilter(imgFilter);

                        //Important: prevent Welcome Console from popping out
                        WelcomeLauncher.setToBackground();

                        int res = chooser.showOpenDialog(frame);
                        frame.dispose();

                        if (res == JFileChooser.APPROVE_OPTION)
                        {
                            File jsonFile =  chooser.getSelectedFile().getAbsoluteFile();
                            runApproveOption(jsonFile);
                        }
                        else
                        {
                            setImportFileSystemStatus(FileSystemStatus.WINDOW_CLOSE_DATABASE_NOT_UPDATED);
                            log.debug("Operation of import project aborted");
                        }
                    }
                    else
                    {
                        showAbortImportPopup();
                        setImportFileSystemStatus(FileSystemStatus.WINDOW_CLOSE_DATABASE_NOT_UPDATED);
                    }

                    windowStatus = ImportSelectionWindowStatus.WINDOW_CLOSE;

                }
            });
        }
        catch (Exception e)
        {
            log.info("ProjectHandler for File type failed to open", e);
        }
    }

    private void runApproveOption(File jsonFile)
    {
        ActionConfig.setJsonFilePath(Paths.get(FilenameUtils.getFullPath(jsonFile.toString())).toString());

        log.info("Proceed with importing project with " + jsonFile.getName());

        if(!ProjectImport.importProjectFile(jsonFile))
        {
            String mes = "Import project failed";
            log.debug(mes);
            formatImportErrorMessage(mes);
            setImportFileSystemStatus(FileSystemStatus.WINDOW_CLOSE_DATABASE_NOT_UPDATED);
        }
        else
        {
            String mes = "Import project success";
            log.debug(mes);
            formatImportErrorMessage(mes);
            setImportFileSystemStatus(FileSystemStatus.WINDOW_CLOSE_DATABASE_UPDATED);
        }
    }

    private void showAbortImportPopup()
    {
        String popupTitle = "Error Opening Window";
        String message = "Another selection window is currently open. Please close to proceed.";
        SelectionWindow.showPopupAndLog(popupTitle, message, JOptionPane.ERROR_MESSAGE);
    }

    public static void formatImportErrorMessage(String message)
    {
        setImportErrorMessage(getImportErrorMessage() + "\n" + message);
    }

    public static void clearImportErrorMessage()
    {
        setImportErrorMessage("");
    }

}
