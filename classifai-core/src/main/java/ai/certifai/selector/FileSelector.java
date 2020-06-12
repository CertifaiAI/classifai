/*
 * Copyright (c) 2020 CertifAI
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

package ai.certifai.selector;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.List;

@Slf4j
@NoArgsConstructor
public class FileSelector extends Application {

    //private static String windowTitle = "Choose a file / multiple files";
    private static FileNameExtensionFilter imgfilter = new FileNameExtensionFilter("Image Files", "jpg", "png", "gif", "jpeg");
//    private static FileChooser.ExtensionFilter fileExtensions;
//
//    static {
//        fileExtensions = new FileChooser.ExtensionFilter("image", Arrays.asList("*.jpg", "*.jpeg", "*.png"));
//    }
    public void runMain()
    {
        try
        {
            launch();
        }
        catch(IllegalStateException e)
        {
            SelectorHandler.setWindowState(false);

            log.debug("Select files failed to open", e);
        }

    }

    public void start(Stage stage) throws Exception
    {
        runFileSelector();
    }

    public void runFileSelector() {

        Platform.runLater(() -> {

//            Stage stage = new Stage();
//
//            FileChooser fileChooser = new FileChooser();
//            fileChooser.setTitle(windowTitle);
//            fileChooser.setInitialDirectory(SelectorHandler.getRootSearchPath());
//            fileChooser.getExtensionFilters().addAll(fileExtensions);
//
//            //Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
//            stage.initStyle(StageStyle.UTILITY);
//            stage.setMaxWidth(0);
//            stage.setMaxHeight(0);
//            stage.setX(Double.MAX_VALUE);
//            //stage.setX((primScreenBounds.getWidth() - stage.getWidth()) / 2);
//            //stage.setY((primScreenBounds.getHeight() - stage.getHeight()) / 2);
//
//            stage.setAlwaysOnTop(true);
//            stage.show();
//
//            List<File> chosenFiles = fileChooser.showOpenMultipleDialog(stage);
//
//            SelectorHandler.configureDatabaseUpdate(chosenFiles);
//
//            SelectorHandler.processSelectorOutput();

            JFileChooser fc = new JFileChooser(){
                @Override
                protected JDialog createDialog(Component parent)
                        throws HeadlessException {
                    JDialog dialog = super.createDialog(parent);
                    // config here as needed - just to see a difference
                    dialog.setLocationByPlatform(true);
                    // might help - can't know because I can't reproduce the problem
                    dialog.setAlwaysOnTop(true);
                    return dialog;
                }
            };
            //fc.setCurrentDirectory(new java.io.File("."));
            fc.setFileFilter(imgfilter);
            fc.setDialogTitle("Select Files");
            fc.setMultiSelectionEnabled(true);
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);

            if(fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
                File[] allfiles = fc.getSelectedFiles();
                SelectorHandler.configureDatabaseUpdate(Arrays.asList(allfiles));
                SelectorHandler.processSelectorOutput();
            }
            else{
                File nullfiles = null;
                SelectorHandler.configureDatabaseUpdate(Arrays.asList(nullfiles));
                SelectorHandler.processSelectorOutput();
            }

            Platform.setImplicitExit(false);

        });
    }


}


