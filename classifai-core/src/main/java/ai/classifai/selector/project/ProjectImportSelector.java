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
import ai.classifai.ui.launcher.LogoLauncher;
import ai.classifai.ui.launcher.WelcomeLauncher;
import ai.classifai.util.ParamConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
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
public class ProjectImportSelector
{
    private enum ImportSelectionWindowStatus
    {
        WINDOW_OPEN,
        WINDOW_CLOSE
    }
    private ImportSelectionWindowStatus windowStatus = ImportSelectionWindowStatus.WINDOW_CLOSE;

    private static final FileNameExtensionFilter imgfilter = new FileNameExtensionFilter(
            "Json Files", "json");

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

                        JFrame frame = initiateFrame();
                        JFileChooser chooser = initiateChooser();

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

    private JFrame initiateFrame()
    {
        Point pt = MouseInfo.getPointerInfo().getLocation();
        JFrame frame = new JFrame();
        frame.setIconImage(LogoLauncher.getClassifaiIcon());

        frame.setAlwaysOnTop(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocation(pt);
        frame.requestFocus();
        frame.setVisible(false);

        return frame;
    }

    private JFileChooser initiateChooser()
    {
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
        chooser.setFileFilter(imgfilter);
        chooser.setDialogTitle("Select Files");
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);

        return chooser;
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
