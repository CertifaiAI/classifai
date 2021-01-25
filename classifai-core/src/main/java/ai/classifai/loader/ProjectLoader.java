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
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * Class per project for managing the loading of project
 *
 * @author codenamewei
 */
@Slf4j
public class ProjectLoader
{
    //Load an existing project from database
    //After loaded once, this value will be always LOADED so retrieving of project from memory than db
    @Getter @Setter private LoaderStatus loaderStatus;

    @Getter @Setter private Integer uuidGeneratorSeed;
    @Getter @Setter private List<String> labelList;

    @Getter private Integer annotationType;
    @Getter private Integer projectID;
    private String projectName;

    //Status when dealing with file/folder opener
    @Getter private FileSystemStatus fileSystemStatus;

    //list to send the new added datapoints as thumbnails to front end
    @Getter private List<Integer> fileSysNewUUIDList;

    //a list of unique uuid representing number of data points in one project
    @Setter @Getter private List<Integer> sanityUUIDList;

    @Setter @Getter private Boolean isLoadedFrontEndToggle;
    @Setter private Boolean isProjectNewlyCreated;

    @Getter private List<Integer> progressUpdate;

    //Set to push in unique uuid to prevent recurrence
    //this will eventually port into List<Integer>
    private Set<Integer> validUUIDSet;
    @Getter @Setter private List<Integer> uuidListFromDatabase;

    //used when checking for progress in
    //(1) validity of database data point
    //(2) adding new data point through file/folder
    private Integer currentUUIDMarker;
    private Integer totalUUIDMaxLen;

    public ProjectLoader(Integer currentProjectID, String currentProjectName, Integer annotationTypeInt, LoaderStatus currentLoaderStatus, Boolean isNewlyCreated)
    {
        projectID = currentProjectID;
        projectName = currentProjectName;
        annotationType = annotationTypeInt;

        loaderStatus = currentLoaderStatus;

        labelList = new ArrayList<>();
        sanityUUIDList = new ArrayList<>();
        uuidListFromDatabase = new ArrayList<>();

        uuidGeneratorSeed = 0;

        isLoadedFrontEndToggle = Boolean.FALSE;
        isProjectNewlyCreated = isNewlyCreated;

        reset(FileSystemStatus.DID_NOT_INITIATE);
    }

    public void reset(FileSystemStatus currentFileSystemStatus)
    {
        validUUIDSet = new HashSet<>();
        fileSysNewUUIDList = new ArrayList<>();

        currentUUIDMarker = 0;
        totalUUIDMaxLen = 1;

        progressUpdate = new ArrayList<>(Arrays.asList(currentUUIDMarker, totalUUIDMaxLen));

        fileSystemStatus = currentFileSystemStatus;
    }

    public void toggleFrontEndLoaderParam()
    {
        if (isProjectNewlyCreated)
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

    public void setDbOriUUIDSize(int totalUUIDSizeBuffer)
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
            sanityUUIDList = new ArrayList<>(validUUIDSet);

            loaderStatus = LoaderStatus.LOADED;

            validUUIDSet.clear();
        }
    }

    public void pushDBValidUUID(Integer uuid)
    {
        validUUIDSet.add(uuid);
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
            offloadFileSysNewList2List();
        }

    }

    private void offloadFileSysNewList2List()
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

        PortfolioVerticle.updateFileSystemUUIDList(projectID);
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

}