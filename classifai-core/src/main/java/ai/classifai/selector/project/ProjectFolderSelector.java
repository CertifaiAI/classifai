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

import ai.classifai.data.type.image.ImageFileType;
import ai.classifai.database.versioning.ProjectVersion;
import ai.classifai.loader.LoaderStatus;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.selector.filesystem.FileSystemStatus;
import ai.classifai.ui.launcher.LogoLauncher;
import ai.classifai.ui.launcher.WelcomeLauncher;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.collection.UuidGenerator;
import ai.classifai.util.data.ImageHandler;
import ai.classifai.util.project.ProjectHandler;
import ai.classifai.util.project.ProjectInfra;
import ai.classifai.util.type.AnnotationHandler;
import ai.classifai.util.type.AnnotationType;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.Locale;

/**
 * Open browser to select folder with importing list of data points in the folder
 *
 * @author codenamewei
 */
@Slf4j
public class ProjectFolderSelector {

    private static FileNameExtensionFilter imgfilter = new FileNameExtensionFilter("Image Files", ImageFileType.getImageFileTypes());

    public void run(@NonNull String projectName, @NonNull AnnotationType annotationType)
    {
        try
        {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {

                    Point pt = MouseInfo.getPointerInfo().getLocation();
                    JFrame frame = new JFrame();

                    frame.setIconImage(LogoLauncher.getClassifaiIcon());
                    frame.setAlwaysOnTop(true);
                    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    frame.setLocation(pt);
                    frame.requestFocus();
                    frame.setVisible(false);

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
                    chooser.setDialogTitle("Select Directory");
                    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    chooser.setAcceptAllFileFilterUsed(false);

                    //Important: prevent Welcome Console from popping out
                    WelcomeLauncher.setToBackground();

                    int res = chooser.showOpenDialog(frame);

                    frame.dispose();

                    if (res == JFileChooser.APPROVE_OPTION)
                    {
                        File projectPath =  chooser.getSelectedFile().getAbsoluteFile();

                        if ((projectPath != null) && (projectPath.exists()))
                        {
                            log.debug("Proceed with creating project");

                            ProjectLoader loader = configureLoader(projectName, annotationType.ordinal(), projectPath);

                            initFolderIteration(loader);
                        }
                    }
                    else
                    {
                        log.info("Creation of " + projectName + "with " + annotationType.name() + " aborted");
                    }
                }
            });
        }
        catch (Exception e)
        {
            log.info("ProjectFolderSelector failed to open", e);
        }

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

    private void initFolderIteration(@NonNull ProjectLoader loader)
    {
        loader.setFileSystemStatus(FileSystemStatus.WINDOW_CLOSE_LOADING_FILES);

        ImageHandler.iterateFolder(loader.getProjectId(), new File(loader.getProjectPath()));
    }

}