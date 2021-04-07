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
import ai.classifai.ui.SelectionWindow;
import ai.classifai.ui.launcher.WelcomeLauncher;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Paths;

import static javax.swing.JOptionPane.showMessageDialog;

/**
 * Open browser to choose for configuration file to import
 *
 * @author codenamewei
 */
@Slf4j
public class ProjectImportSelector extends SelectionWindow {
    public void run()
    {
        try
        {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {

                    if(windowStatus.equals(ImportSelectionWindowStatus.WINDOW_CLOSE))
                    {
                        windowStatus = ImportSelectionWindowStatus.WINDOW_OPEN;

                        JFrame frame = initFrame();
                        JFileChooser chooser = initChooser(JFileChooser.FILES_ONLY);

                        //Important: prevent Welcome Console from popping out
                        WelcomeLauncher.setToBackground();

                        int res = chooser.showOpenDialog(frame);
                        frame.dispose();

                        if (res == JFileChooser.APPROVE_OPTION)
                        {
                            File jsonFile =  chooser.getSelectedFile().getAbsoluteFile();
                            ActionConfig.setJsonFilePath(Paths.get(FilenameUtils.getFullPath(jsonFile.toString())).toString());

                            if (jsonFile.exists())
                            {
                                log.info("Proceed with importing project with " + jsonFile.getName());

                                ProjectImport.importProjectFile(jsonFile);
                            }
                            else
                            {
                                log.debug("Import project failed");
                            }
                        }
                        else
                        {
                            log.debug("Operation of import project aborted");
                        }

                        windowStatus = ImportSelectionWindowStatus.WINDOW_CLOSE;
                    }
                    else
                    {
                        showAbortImportPopup();
                    }
                }
            });
        }
        catch (Exception e)
        {
            log.info("ProjectHandler for File type failed to open", e);
        }
    }

    private void showAbortImportPopup()
    {
        String message = "Another selection window is currently open. Please close to proceed.";
        log.info(message);
        showMessageDialog(null,
                message,
                "Error Opening Window", JOptionPane.ERROR_MESSAGE);
    }

}
