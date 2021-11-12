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
package ai.classifai.frontend.ui.selector.project;

import ai.classifai.frontend.ui.DesktopUI;
import ai.classifai.frontend.ui.component.SelectionWindow;
import ai.classifai.frontend.ui.enums.SelectionWindowStatus;
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
    private static final FileNameExtensionFilter TXT_FILTER = new FileNameExtensionFilter(
            "Text Files", "txt");

    @Setter private File labelFile = null;

    public LabelFileSelector(DesktopUI ui) {
        super(ui);
    }

    public String getLabelFilePath()
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
                    windowStatus = SelectionWindowStatus.WINDOW_OPEN;

                    labelFile = null;

                    JFrame frame = ui.getFrameAtMousePointer();
                    String title = "Select Label File (*.txt)";
                    JFileChooser chooser = initChooser(JFileChooser.FILES_ONLY, title);
                    chooser.setFileFilter(TXT_FILTER);

                    //Important: prevent Welcome Console from popping out
                    ui.ensureWelcomeLauncherStaysInBackground();

                    int res = chooser.showOpenDialog(frame);
                    frame.dispose();

                    if (res == JFileChooser.APPROVE_OPTION)
                    {
                        labelFile = chooser.getSelectedFile();
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
