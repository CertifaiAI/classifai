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
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

@Slf4j
public class TabularFileSelector extends SelectionWindow
{
    private static final FileNameExtensionFilter TABULAR_FILE_FILTER = new FileNameExtensionFilter(
            "Tabular Files", "csv", "xlsx");

    @Setter private File tabularFile = null;

    public TabularFileSelector(DesktopUI ui) {
        super(ui);
    }

    public String getTabularFilePath()
    {
        return (tabularFile != null) ? tabularFile.getAbsolutePath() : "";
    }

    public void run()
    {
        try
        {
            EventQueue.invokeLater(() -> {

                if(windowStatus.equals(SelectionWindowStatus.WINDOW_CLOSE))
                {
                    windowStatus = SelectionWindowStatus.WINDOW_OPEN;

                    tabularFile = null;

                    JFrame frame = ui.getFrameAtMousePointer();
                    String title = "Select Label File (*.csv, *.xlsx)";
                    JFileChooser chooser = initChooser(JFileChooser.FILES_ONLY, title);
                    chooser.setFileFilter(TABULAR_FILE_FILTER);

                    //Important: prevent Welcome Console from popping out
                    ui.ensureWelcomeLauncherStaysInBackground();

                    int res = chooser.showOpenDialog(frame);
                    frame.dispose();

                    if (res == JFileChooser.APPROVE_OPTION)
                    {
                        tabularFile = chooser.getSelectedFile();
                    }
                    else
                    {
                        log.debug("Operation of import tabular file aborted");
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
            log.info("TabularFileSelector failed to open", e);
        }
    }

}

