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
package ai.classifai.loader;

import ai.classifai.database.portfolio.PortfolioVerticle;
import ai.classifai.selector.filesystem.FileSystemStatus;
import ai.classifai.util.versioning.ProjectVersion;
import ai.classifai.util.versioning.VersionCollection;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * Class per project for managing the loading of project
 *
 * @author codenamewei
 */
@Slf4j
@Getter
@Setter
public class ProjectLoader
{
    private String projectID;
    private String projectName;

    private Integer annotationType;
    private String projectPath;

    private Boolean isProjectNew;
    private Boolean isProjectStarred;

    //Load an existing project from database
    //After loaded once, this value will be always LOADED so retrieving of project from memory than db
    private LoaderStatus loaderStatus;
    
    private List<String> labelList;

     //a list of unique uuid representing number of data points in one project
    private List<String> sanityUuidList = new ArrayList<>();
    private List<String> uuidListFromDb;

    private VersionCollection versionCollector;
    private ProjectVersion currentProjectVersion;

    private Boolean isLoadedFrontEndToggle = Boolean.FALSE;

    //used when checking for progress in
    //(1) validity of database data point
    //(2) adding new data point through file/folder
    private Integer currentUUIDMarker = 0;
    private Integer totalUUIDMaxLen = 1;

    //Set to push in unique uuid to prevent recurrence
    //this will eventually port into List<Integer>
    private Set<String> validUUIDSet = new LinkedHashSet<>();

    //Status when dealing with file/folder opener
    private FileSystemStatus fileSystemStatus = FileSystemStatus.DID_NOT_INITIATE;

    //list to send the new added datapoints as thumbnails to front end
    private List<String> fileSysNewUUIDList = new ArrayList<>();

    //list to iterate through uuid list from existing database to remove if necessary
    private List<String> dbListBuffer = new ArrayList<>();
    private List<String> reloadAdditionList = new ArrayList<>();
    private List<String> reloadDeletionList = new ArrayList<>();

    private List<Integer> progressUpdate = new ArrayList<>(Arrays.asList(currentUUIDMarker, totalUUIDMaxLen));

    private ProjectLoader(Builder build)
    {
        this.projectID = build.projectID;
        this.projectName = build.projectName;
        this.annotationType = build.annotationType;
        this.projectPath = build.projectPath;
        this.isProjectNew = build.isProjectNew;
        this.isProjectStarred = build.isProjectStarred;
        this.loaderStatus = build.loaderStatus;

        if((build.versionCollection != null) && (build.projectVersion != null))
        {
            this.versionCollector = build.versionCollection;
            this.currentProjectVersion = build.projectVersion;

            String versionUuid = currentProjectVersion.getVersionUuid();
            this.labelList = versionCollector.getLabelDict().get(versionUuid);
            this.uuidListFromDb = versionCollector.getUuidDict().get(versionUuid);
        }
    }

    public void resetFileSysProgress(FileSystemStatus currentFileSystemStatus)
    {
        validUUIDSet.clear();
        fileSysNewUUIDList.clear();

        currentUUIDMarker = 0;
        totalUUIDMaxLen = 1;

        progressUpdate = new ArrayList<>(Arrays.asList(currentUUIDMarker, totalUUIDMaxLen));

        fileSystemStatus = currentFileSystemStatus;
    }

    public void toggleFrontEndLoaderParam()
    {
        if (isProjectNew)
        {
            //update database to be old project
            PortfolioVerticle.updateIsNewParam(projectID);
        }

        isLoadedFrontEndToggle = Boolean.TRUE;
    }

    //loading project from database
    public List<Integer> getProgress()
    {
        List<Integer> progressBar = new ArrayList<>();

        progressBar.add(currentUUIDMarker);
        progressBar.add(totalUUIDMaxLen);

        return progressBar;
    }

    public void setDbOriUUIDSize(Integer totalUUIDSizeBuffer)
    {
        totalUUIDMaxLen = totalUUIDSizeBuffer;

        if (totalUUIDMaxLen.equals(0))
        {
            loaderStatus = LoaderStatus.LOADED;
        }
        else if (totalUUIDMaxLen.compareTo(0) < 0)
        {
            log.debug("UUID Size less than 0. UUIDSize: " + totalUUIDSizeBuffer);
            loaderStatus = LoaderStatus.ERROR;
        }
    }

    public void updateDBLoadingProgress(Integer currentSize)
    {
        currentUUIDMarker = currentSize;

        //if done, offload set to list
        if (currentUUIDMarker.equals(totalUUIDMaxLen))
        {
            sanityUuidList = new ArrayList<>(validUUIDSet);

            validUUIDSet.clear();

            loaderStatus = LoaderStatus.LOADED;
        }
    }

    public void pushDBValidUUID(String uuid)
    {
        validUUIDSet.add(uuid);
    }

    public void pushFileSysNewUUIDList(String uuid)
    {
        fileSysNewUUIDList.add(uuid);
    }

    //updating project from file system
    public void updateFileSysLoadingProgress(Integer currentSize)
    {
        currentUUIDMarker = currentSize;
        progressUpdate.set(0, currentUUIDMarker);

        //if done, offload set to list
        if (currentUUIDMarker.equals(totalUUIDMaxLen))
        {
            offloadFileSysNewList2List();
        }
    }

    private void offloadFileSysNewList2List()
    {
        if (fileSysNewUUIDList.isEmpty())
        {
            fileSystemStatus = FileSystemStatus.WINDOW_CLOSE_DATABASE_NOT_UPDATED;
        }
        else
        {
            sanityUuidList.addAll(fileSysNewUUIDList);
            uuidListFromDb.addAll(fileSysNewUUIDList);
            PortfolioVerticle.updateFileSystemUUIDList(projectID);
            fileSystemStatus = FileSystemStatus.WINDOW_CLOSE_DATABASE_UPDATED;
        }
    }

    public void uploadNewUuidFromReloading(@NonNull String uuid)
    {
        if(!uuidListFromDb.contains(uuid)) uuidListFromDb.add(uuid);

        sanityUuidList.add(uuid);
        reloadAdditionList.add(uuid);
    }

    public void resetReloadingProgress(FileSystemStatus currentFileSystemStatus)
    {
        dbListBuffer = new ArrayList<>(uuidListFromDb);
        reloadAdditionList.clear();
        reloadDeletionList.clear();

        currentUUIDMarker = 0;
        totalUUIDMaxLen = 1;

        progressUpdate = new ArrayList<>(Arrays.asList(currentUUIDMarker, totalUUIDMaxLen));

        fileSystemStatus = currentFileSystemStatus;
    }

    //updating project progress from reloading it
    public void updateReloadingProgress(Integer currentSize)
    {
        progressUpdate.set(0, currentSize);

        //if done, offload set to list
        if (currentSize.equals(totalUUIDMaxLen))
        {
            sanityUuidList.removeAll(dbListBuffer);
            reloadDeletionList = dbListBuffer;

            PortfolioVerticle.updateFileSystemUUIDList(projectID);
            fileSystemStatus = FileSystemStatus.WINDOW_CLOSE_DATABASE_UPDATED;
        }
    }


    public void setFileSysTotalUUIDSize(Integer totalUUIDSizeBuffer)
    {
        totalUUIDMaxLen = totalUUIDSizeBuffer;
        progressUpdate = Arrays.asList(new Integer[]{0, totalUUIDMaxLen});
    }

    public void setFileSystemStatus(FileSystemStatus status)
    {
        fileSystemStatus = status;
    }

    public static class Builder
    {
        private String projectID;
        private String projectName;

        private Integer annotationType;
        private String projectPath;

        private Boolean isProjectNew;
        private Boolean isProjectStarred;

        //Load an existing project from database
        //After loaded once, this value will be always LOADED so retrieving of project from memory than db
        private LoaderStatus loaderStatus;

        private VersionCollection versionCollection = null;
        private ProjectVersion projectVersion = null;

        public ProjectLoader build()
        {
            return new ProjectLoader(this);
        }

        public Builder projectID(String projectID)
        {
            this.projectID = projectID;
            return this;
        }

        public Builder projectName(String projectName)
        {
            this.projectName = projectName;
            return this;
        }

        public Builder annotationType(Integer annotationType)
        {
            this.annotationType = annotationType;
            return this;
        }

        public Builder projectPath(String projectPath)
        {
            this.projectPath = projectPath;
           return this;
        }

        public Builder isProjectNew(Boolean isProjectNew)
        {
            this.isProjectNew = isProjectNew;
            return this;
        }

        public Builder isProjectStarred(Boolean isProjectStarred)
        {
            this.isProjectStarred = isProjectStarred;
            return this;
        }

        public Builder loaderStatus(LoaderStatus loaderStatus)
        {
            this.loaderStatus = loaderStatus;
            return this;
        }

        public Builder versionCollection(VersionCollection versionCollection)
        {
            this.versionCollection = versionCollection;
            return this;
        }

        public Builder currentProjectVersion(ProjectVersion projectVersion)
        {
            this.projectVersion = projectVersion;
            return this;
        }

    }
}