package ai.classifai.selector.project;
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

import ai.classifai.action.LabelListImport;
import ai.classifai.selector.filesystem.FileSystemStatus;
import ai.classifai.ui.SelectionWindow;
import ai.classifai.ui.launcher.WelcomeLauncher;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * Open browser to choose label list to import
 *
 * @author codenamewei
 */
@Slf4j
public class LabelListSelector extends SelectionWindow
{
    @Getter @Setter
    private static FileSystemStatus importLabelFileSystemStatus = FileSystemStatus.WINDOW_CLOSE_DATABASE_NOT_UPDATED;

    @Setter
    private static File labelFile = null;

    @Getter @Setter
    private static List<String> labelList = null;

    private static final FileNameExtensionFilter imgFilter = new FileNameExtensionFilter(
            "Text Files", "txt");

    public static String getLabelFilePath()
    {
        return (labelFile != null) ? labelFile.getAbsolutePath() : "";
    }

    public void run()
    {
        try
        {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {

                    if(windowStatus.equals(SelectionWindow.ImportSelectionWindowStatus.WINDOW_CLOSE))
                    {
                        windowStatus = SelectionWindow.ImportSelectionWindowStatus.WINDOW_OPEN;
                        setImportLabelFileSystemStatus(FileSystemStatus.WINDOW_OPEN);

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
                            labelFile = chooser.getSelectedFile().getAbsoluteFile();

                            log.info("Proceed with importing label list file with " + labelFile.getName());

                            setLabelList(new LabelListImport().importLabelListFile(labelFile));

                            if(labelList == null)
                            {
                                log.debug("Import label list failed");
                                setImportLabelFileSystemStatus(FileSystemStatus.WINDOW_CLOSE_DATABASE_NOT_UPDATED);
                            }
                            else
                            {
                                log.debug("Import label list success");

                                setImportLabelFileSystemStatus(FileSystemStatus.WINDOW_CLOSE_DATABASE_UPDATED);
                            }
                        }
                        else
                        {
                            setImportLabelFileSystemStatus(FileSystemStatus.WINDOW_CLOSE_DATABASE_NOT_UPDATED);
                            log.debug("Operation of import project aborted");
                        }

                        windowStatus = SelectionWindow.ImportSelectionWindowStatus.WINDOW_CLOSE;

                    }
                    else
                    {
                        showAbortImportPopup();
                        setImportLabelFileSystemStatus(FileSystemStatus.WINDOW_CLOSE_DATABASE_NOT_UPDATED);
                    }
                }
            });
        }
        catch (Exception e)
        {
            log.info("LabelListSelector failed to open", e);
        }
    }
}
