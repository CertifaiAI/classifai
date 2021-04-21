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

import ai.classifai.database.versioning.ProjectVersion;
import ai.classifai.loader.LoaderStatus;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.selector.filesystem.FileSystemStatus;
import ai.classifai.ui.SelectionWindow;
import ai.classifai.ui.launcher.WelcomeLauncher;
import ai.classifai.util.collection.UuidGenerator;
import ai.classifai.util.data.ImageHandler;
import ai.classifai.util.project.ProjectHandler;
import ai.classifai.util.project.ProjectInfra;
import ai.classifai.util.type.AnnotationHandler;
import ai.classifai.util.type.AnnotationType;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Objects;

/**
 * Open browser to select folder with importing list of data points in the folder
 *
 * @author codenamewei
 */
@Slf4j
public class ProjectFolderSelector extends SelectionWindow {

    public void run(@NonNull String projectName, @NonNull AnnotationType annotationType)
    {
        try
        {
            EventQueue.invokeLater(new Runnable() {
                @SneakyThrows
                @Override
                public void run()
                {
                    ProjectLoader loader = Objects.requireNonNull(
                            configureLoader(projectName, annotationType.ordinal(), new File("")));

                    loader.setFileSystemStatus(FileSystemStatus.WINDOW_OPEN);

                    JFrame frame = initFrame();
                    String title = "Select Folder";
                    JFileChooser chooser = initChooser(JFileChooser.DIRECTORIES_ONLY, title);

                    //Important: prevent Welcome Console from popping out
                    WelcomeLauncher.setToBackground();

                    int res = chooser.showOpenDialog(frame);
                    frame.dispose();

                    if (res == JFileChooser.APPROVE_OPTION)
                    {
                        File projectPath =  chooser.getSelectedFile().getAbsoluteFile();

                        log.debug("Proceed with creating project");
                        loader.setProjectPath(projectPath.toString());
                        initFolderIteration(loader);

                        loader.setFileSystemStatus(FileSystemStatus.WINDOW_CLOSE_DATABASE_UPDATED);
                    }
                    else
                    {
                        // Abort creation if user did not choose any
                        showAbortProjectPopup();
                        log.info("Creation of " + projectName + " with " + annotationType.name() + " aborted");

                        loader.setFileSystemStatus(FileSystemStatus.WINDOW_CLOSE_DATABASE_NOT_UPDATED);
                    }
                }
            });
        }
        catch (Exception e)
        {
            log.info("ProjectFolderSelector failed to open", e);
        }
    }

    private void showAbortProjectPopup()
    {
        String popupTitle = "Project Not Created";
        String message = "No folder chosen. Abort project creation.";
        SelectionWindow.showPopupAndLog(popupTitle, message, JOptionPane.INFORMATION_MESSAGE);
    }

    private ProjectLoader configureLoader(@NonNull String projectName, @NonNull Integer annotationInt, @NonNull File rootPath)
    {
        if (ProjectHandler.isProjectNameUnique(projectName, annotationInt))
        {
            String annotationName = AnnotationHandler.getType(annotationInt).name();

            log.debug("Creating " + annotationName.toLowerCase(Locale.ROOT) + " project with name: " + projectName);

            String projectID = UuidGenerator.generateUuid();

            String rootProjectPath = rootPath.getAbsolutePath();

            ProjectLoader loader = ProjectLoader.builder()
                    .projectId(projectID)
                    .projectName(projectName)
                    .annotationType(annotationInt)
                    .projectPath(rootProjectPath)
                    .loaderStatus(LoaderStatus.LOADED)
                    .isProjectStarred(Boolean.FALSE)
                    .isProjectNew(Boolean.TRUE)
                    .projectVersion(new ProjectVersion())
                    .projectInfra(ProjectInfra.ON_PREMISE)
                    .build();

            ProjectHandler.loadProjectLoader(loader);

            return loader;
        }

        return null;
    }

    private void initFolderIteration(@NonNull ProjectLoader loader) throws IOException {
        loader.setFileSystemStatus(FileSystemStatus.WINDOW_CLOSE_LOADING_FILES);

        String projectPath = loader.getProjectPath();
        if(!ImageHandler.iterateFolder(loader.getProjectId(), new File(projectPath)))
        {
            // Get example image from metadata
            File srcImgFile = Paths.get(".", "metadata", "classifai_overview.png").toFile();
            File destImageFile = Paths.get(projectPath, "example_img.png").toFile();
            FileUtils.copyFile(srcImgFile, destImageFile);
            log.info("Empty folder. Example image added.");

            // Run initiate image again
            ImageHandler.iterateFolder(loader.getProjectId(), new File(projectPath));
        }
    }

}