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
package ai.classifai.database.loader;

import ai.classifai.database.portfoliodb.PortfolioVerticle;
import ai.classifai.selector.filesystem.FileSystemStatus;
import ai.classifai.util.ProjectHandler;
import ai.classifai.util.image.ImageHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * Class per project for managing the loading of project
 *
 * @author Chiawei Lim
 */
@Slf4j
public class ProjectLoader
{
    @Getter private Integer annotationType;
    @Getter private Integer projectID;
    private String projectName;

    //Load an existing project from database
    //After loaded once, this value will be always LOADED so retrieving of project from memory than db
    @Getter @Setter private LoaderStatus loaderStatus;

    //Status when dealing with file/folder opener
    @Getter private FileSystemStatus fileSystemStatus;

    //a list of unique uuid representing number of data points in one project
    @Getter private List<Integer> sanityUUIDList;

    @Setter @Getter private Integer uuidGeneratorSeed;

    @Getter @Setter private List<String> labelList;

    //Set to push in unique uuid to prevent recurrence
    //this will eventually port into List<Integer>
    private Set<Integer> uuidUniqueSet;

    //list to send the new added datapoints as thumbnails to front end
    @Getter private List<Integer> fileSysNewUUIDList;

    //used when checking for progress in
    //(1) validity of database data point
    //(2) adding new data point through file/folder
    private Integer currentUUIDMarker;
    private Integer totalUUIDMaxLen;

    @Getter private List<Integer> progressUpdate;

    public ProjectLoader(Integer currentProjectID, String currentProjectName, Integer annotationTypeInt, LoaderStatus currentLoaderStatus)
    {
        projectID = currentProjectID;
        projectName = currentProjectName;
        annotationType = annotationTypeInt;

        loaderStatus = currentLoaderStatus;

        labelList = new ArrayList<>();
        sanityUUIDList = new ArrayList<>();

        uuidGeneratorSeed = 0;

        reset(FileSystemStatus.DID_NOT_INITIATE);
    }

    public void reset(FileSystemStatus currentFileSystemStatus)
    {
        uuidUniqueSet = new HashSet<>();
        fileSysNewUUIDList = new ArrayList<>();

        currentUUIDMarker = 0;
        totalUUIDMaxLen = 1;

        progressUpdate = new ArrayList<>(Arrays.asList(currentUUIDMarker, totalUUIDMaxLen));

        fileSystemStatus = currentFileSystemStatus;
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

        if(totalUUIDMaxLen == 0)
        {
            loaderStatus = LoaderStatus.LOADED;
        }
        else if(totalUUIDMaxLen < 0)
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
            sanityUUIDList = new ArrayList<>(uuidUniqueSet);

            loaderStatus = LoaderStatus.LOADED;
        }
    }

    public void pushDBValidUUID(Integer uuid)
    {
        uuidUniqueSet.add(uuid);
    }

    public void pushFileSysNewUUIDList(Integer uuid)
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
            System.out.println("offloadFileSysNewList2List()");
            offloadFileSysNewList2List();
        }

    }

    public void offloadFileSysNewList2List()
    {
        sanityUUIDList.addAll(fileSysNewUUIDList);

        if(fileSysNewUUIDList.isEmpty())
        {
            fileSystemStatus = FileSystemStatus.WINDOW_CLOSE_DATABASE_NOT_UPDATED;
        }
        else
        {

            fileSystemStatus = FileSystemStatus.WINDOW_CLOSE_DATABASE_UPDATED;
        }

        ProjectHandler.setIsCurrentFileSystemDBUpdating(false);

        PortfolioVerticle.updateFileSystemUUIDList(projectID);
    }

    public boolean isAllFileSysProcessed()
    {
        return currentUUIDMarker.equals(totalUUIDMaxLen);
    }

    public void setFileSysTotalUUIDSize(Integer totalUUIDSizeBuffer)
    {

        totalUUIDMaxLen = totalUUIDSizeBuffer;
        progressUpdate = Arrays.asList(new Integer[]{0, totalUUIDMaxLen});

    }

    public void setFileSystemStatus(FileSystemStatus status)
    {
        fileSystemStatus = status;

        if(fileSystemStatus.equals(FileSystemStatus.WINDOW_CLOSE_DATABASE_UPDATED) || fileSystemStatus.equals(FileSystemStatus.WINDOW_CLOSE_DATABASE_NOT_UPDATED))
        {
            ProjectHandler.setIsCurrentFileSystemDBUpdating(false);
        }
    }

}