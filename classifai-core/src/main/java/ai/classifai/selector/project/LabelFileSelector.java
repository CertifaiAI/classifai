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
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

/**
 * Open browser to choose label file to import
 *
 * @author codenamewei
 */
@Slf4j
public class LabelFileSelector extends SelectionWindow
{
    @Getter @Setter
    private static FileSystemStatus importLabelFileStatus = FileSystemStatus.WINDOW_CLOSE_NO_ACTION;

    @Getter @Setter
    private static File labelFile = null;

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
            EventQueue.invokeLater(() -> {

                if(windowStatus.equals(SelectionWindowStatus.WINDOW_CLOSE))
                {
                    labelFile = null;
                    windowStatus = SelectionWindowStatus.WINDOW_OPEN;
                    setImportLabelFileStatus(FileSystemStatus.WINDOW_OPEN);

                    JFrame frame = initFrame();
                    String title = "Select Label File (*.txt)";
                    JFileChooser chooser = initChooser(JFileChooser.FILES_ONLY, title);
                    chooser.setFileFilter(imgFilter);

                    //Important: prevent Welcome Console from popping out
                    WelcomeLauncher.setToBackground();

                    int res = chooser.showOpenDialog(frame);
                    frame.dispose();

                    if (res == JFileChooser.APPROVE_OPTION)
                    {
                        labelFile = chooser.getSelectedFile();

                        setImportLabelFileStatus(FileSystemStatus.WINDOW_CLOSE_ITEM_SELECTED);
                    }
                    else
                    {
                        setImportLabelFileStatus(FileSystemStatus.WINDOW_CLOSE_NO_ACTION);
                        log.debug("Operation of import project aborted");
                    }

                    windowStatus = SelectionWindowStatus.WINDOW_CLOSE;
                }
                else
                {
                    showAbortImportPopup();
                    setImportLabelFileStatus(FileSystemStatus.WINDOW_CLOSE_NO_ACTION);
                }
            });
        }
        catch (Exception e)
        {
            log.info("LabelFileSelector failed to open", e);
        }
    }
}
