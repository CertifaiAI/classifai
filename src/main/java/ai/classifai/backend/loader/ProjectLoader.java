/*
 * Copyright (c) 2020-2021 CertifAI Sdn. Bhd.
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
package ai.classifai.backend.loader;

import ai.classifai.backend.status.FileSystemStatus;
import ai.classifai.backend.versioning.ProjectVersion;
import ai.classifai.core.enumeration.ProjectInfra;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Class per project for managing the loading of project
 *
 * @author codenamewei
 */
@Slf4j
@Getter
@Setter
@Builder
public class ProjectLoader
{
    private String projectId;
    private String projectName;
    private Integer annotationType;
    private File projectPath;
    private File tabularFilePath;

    private ProjectInfra projectInfra;

//    @NonNull
//    private PortfolioDB portfolioDB;

//    @NonNull
//    private AnnotationDB annotationDB;

    //Load an existing project from database
    //After loaded once, this value will be always LOADED so retrieving of project from memory than db
    private ProjectLoaderStatus projectLoaderStatus;

    @Builder.Default private ProjectVersion projectVersion = new ProjectVersion();

    @Builder.Default private Boolean isProjectNew = Boolean.TRUE;
    @Builder.Default private Boolean isProjectStarred = Boolean.FALSE;

    @Builder.Default private List<String> labelList = new ArrayList<>();

    //a list of unique uuid representing number of data points in one project
    @Builder.Default private List<String> sanityUuidList = new ArrayList<>();
    @Builder.Default private List<String> uuidListFromDb = new ArrayList<>();

    //key: data point uuid
    //value: annotation
//    @Builder.Default private Map<String, Annotation> uuidAnnotationDict = new HashMap<>();

    @Builder.Default private Boolean isLoadedFrontEndToggle = Boolean.FALSE;

    //used when checking for progress in
    //(1) validity of database data point
    //(2) adding new data point through file/folder
    @Builder.Default private Integer currentUuidMarker = 0;
    @Builder.Default private Integer totalUuidMaxLen = 1;

    //Set to push in unique uuid to prevent recurrence
    //this will eventually port into List<Integer>
    @Builder.Default private Set<String> validUUIDSet = new LinkedHashSet<>();

    //Status when dealing with file/folder opener
    @Builder.Default private FileSystemStatus fileSystemStatus = FileSystemStatus.DID_NOT_INITIATED;

    //list to send the new added datapoints as thumbnails to front end
    @Builder.Default private List<String> fileSysNewUuidList = new ArrayList<>();

    //list to iterate through uuid list from existing database to remove if necessary
    @Builder.Default private List<String> dbListBuffer = new ArrayList<>();
    @Builder.Default private List<String> reloadAdditionList = new ArrayList<>();
    @Builder.Default private List<String> reloadDeletionList = new ArrayList<>();

    @Builder.Default private List<Integer> progressUpdate = Arrays.asList(0, 1);

    @Builder.Default private List<String> unsupportedImageList = new ArrayList<>();

    public String getCurrentVersionUuid()
    {
        return projectVersion.getCurrentVersion().getVersionUuid();
    }

    public void toggleFrontEndLoaderParam()
    {
        if (isProjectNew)
        {
            //update database to be old project
//            portfolioDB.updateIsNewParam(projectId);
        }

        isLoadedFrontEndToggle = Boolean.TRUE;
    }

    //loading project from database
    public List<Integer> getProgress()
    {
        List<Integer> progressBar = new ArrayList<>();

        progressBar.add(currentUuidMarker);
        progressBar.add(totalUuidMaxLen);

        return progressBar;
    }

    public void setDbOriUUIDSize(Integer totalUUIDSizeBuffer)
    {
        totalUuidMaxLen = totalUUIDSizeBuffer;

        if (totalUuidMaxLen.equals(0))
        {
            projectLoaderStatus = ProjectLoaderStatus.LOADED;
        }
        else if (totalUuidMaxLen.compareTo(0) < 0)
        {
            log.debug("UUID Size less than 0. UUIDSize: " + totalUUIDSizeBuffer);
            projectLoaderStatus = ProjectLoaderStatus.ERROR;
        }
    }

    public void updateDBLoadingProgress(Integer currentSize)
    {
        currentUuidMarker = currentSize;

        //if done, offload set to list
        if (currentUuidMarker.equals(totalUuidMaxLen))
        {
            sanityUuidList = new ArrayList<>(validUUIDSet);

            validUUIDSet.clear();

            projectLoaderStatus = ProjectLoaderStatus.LOADED;
        }
    }

    public void pushDBValidUUID(String uuid)
    {
        validUUIDSet.add(uuid);
    }

    public void pushFileSysNewUUIDList(String uuid)
    {
        fileSysNewUuidList.add(uuid);
    }

    public void updateLoadingProgress(Integer currentSize)
    {
        currentUuidMarker = currentSize;
        progressUpdate.set(0, currentUuidMarker);

        //if done, offload set to list
        if (currentUuidMarker.equals(totalUuidMaxLen))
        {
            if (fileSysNewUuidList.isEmpty())
            {
                fileSystemStatus = FileSystemStatus.DATABASE_NOT_UPDATED;
            }
            else
            {
                sanityUuidList.addAll(fileSysNewUuidList);
                uuidListFromDb.addAll(fileSysNewUuidList);

                projectVersion.setCurrentVersionUuidList(fileSysNewUuidList);
//                projectVersion.setCurrentVersionLabelList(labelList);

//                portfolioDB.createNewProject(projectId);

                fileSystemStatus = FileSystemStatus.DATABASE_UPDATED;
            }
        }
    }

    public void uploadUuidFromRootPath(@NonNull String uuid)
    {
        if(!uuidListFromDb.contains(uuid)) uuidListFromDb.add(uuid);

        sanityUuidList.add(uuid);
        reloadAdditionList.add(uuid);

    }

    public void uploadSanityUuidFromConfigFile(@NonNull String uuid)
    {
        sanityUuidList.add(uuid);
    }

    public void resetFileSysProgress(FileSystemStatus currentFileSystemStatus)
    {
        validUUIDSet.clear();
        fileSysNewUuidList.clear();

        currentUuidMarker = 0;
        totalUuidMaxLen = 1;

        progressUpdate = new ArrayList<>(Arrays.asList(currentUuidMarker, totalUuidMaxLen));

        fileSystemStatus = currentFileSystemStatus;
    }

    public void resetReloadingProgress(FileSystemStatus currentFileSystemStatus)
    {
        dbListBuffer = new ArrayList<>(sanityUuidList);
        reloadAdditionList.clear();
        reloadDeletionList.clear();

        currentUuidMarker = 0;
        totalUuidMaxLen = 1;

        progressUpdate = new ArrayList<>(Arrays.asList(currentUuidMarker, totalUuidMaxLen));

        fileSystemStatus = currentFileSystemStatus;
    }

    //updating project progress from reloading it
    public void updateReloadingProgress(Integer currentSize)
    {
        progressUpdate.set(0, currentSize);

        //if done, offload set to list
        if (currentSize.equals(totalUuidMaxLen))
        {
            sanityUuidList.removeAll(dbListBuffer);
            reloadDeletionList = dbListBuffer;

//            portfolioDB.updateFileSystemUuidList(projectId);
            fileSystemStatus = FileSystemStatus.DATABASE_UPDATED;
        }
    }


    public void setFileSysTotalUUIDSize(Integer totalUUIDSizeBuffer)
    {
        totalUuidMaxLen = totalUUIDSizeBuffer;
        progressUpdate = Arrays.asList(new Integer[]{0, totalUuidMaxLen});
    }

//    public void initFolderIteration() throws IOException {
//        if(!ImageHandler.loadProjectRootPath(this, annotationDB))
//        {
//            getExampleImage();
//
//            // Run loadProjectRootPath again
//            if(!ImageHandler.loadProjectRootPath(this, annotationDB))
//            {
//                log.debug("Loading files in project folder failed");
//            }
//        }
//    }

    private void getExampleImage() throws IOException {
        // Get example image from metadata
        String exampleSrcFileName = "/classifai_overview.png";
        String exampleImgFileName = "example_img.png";

        BufferedImage srcImg = ImageIO.read(
                Objects.requireNonNull(ProjectLoader.class.getResource(exampleSrcFileName)));
//        String destImgFileStr = Paths.get(projectPath.getAbsolutePath(), exampleImgFileName).toString();
//        ImageIO.write(srcImg, "png", new File(destImgFileStr));
        log.info("Empty folder. Example image added.");
    }

//    public File getDataFullPath( @NonNull String dataSubPath)
//    {
//        return Paths.get(getProjectPath().getAbsolutePath(), dataSubPath).toFile();
//    }

//    public String getAnnotationKey() {
//        if(getAnnotationType().equals(AnnotationType.BOUNDINGBOX.ordinal())) {
//            return ParamConfig.getBoundingBoxParam();
//        } else {
//            return ParamConfig.getSegmentationParam();
//        }
//    }
}