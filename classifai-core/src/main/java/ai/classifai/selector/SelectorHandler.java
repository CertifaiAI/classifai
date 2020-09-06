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
package ai.classifai.selector;

import ai.classifai.annotation.AnnotationType;
import ai.classifai.database.loader.LoaderStatus;
import ai.classifai.database.loader.ProjectLoader;
import ai.classifai.server.ParamConfig;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Selector Handler for File & Folder Selector and Database Update
 *
 * @author ChiaWei
 */
@Slf4j
public class SelectorHandler {

    //key: projectID
    //value: ProjectLoader
    private static Map projectIDLoaderDict;

    //key: Pair<String projectName, Integer annotationType>
    //value: projectID
    private static Map projectIDSearch;

    private static AtomicInteger projectIDGenerator;

    //Variable contains String projectName and Integer annotationType for current file/folder selector
    @Getter private static Pair selectorProjectBuffer;

    private static boolean isWindowOpen = false;

    // only one file/folder selector can open at one time. even for multiple projects.
    private static boolean isLoaderProcessing = false;

    @Setter @Getter private static SelectorStatus selectorStatus;

    @Getter private static String currentWindowSelection;//FILE FOLDER

    @Getter private static final File rootSearchPath = new File(System.getProperty("user.home"));


    static {
        projectIDLoaderDict = new HashMap<Integer, ProjectLoader>();
        projectIDSearch = new HashMap<Pair<String, Integer>, String>();
        projectIDGenerator = new AtomicInteger(0);
        selectorProjectBuffer = null;
    }

    public static ProjectLoader getProjectLoader(Pair project)
    {
        Integer projectIDKey = getProjectID(project);

        return getProjectLoader(projectIDKey);
    }

    public static ProjectLoader getProjectLoader(Integer projectID)
    {
        try
        {
            return (ProjectLoader) projectIDLoaderDict.get(projectID);
        }
        catch (Exception e)
        {
            log.info("Error when retriveing ProjectLoader in SelectorHandler, ", e);
        }
        return null;
    }


    public static Integer getProjectID(String projectName, Integer annotationType)
    {
        Pair key = new ImmutablePair(projectName, annotationType);

        return getProjectID(key);
    }

    private static Integer getProjectID(Pair projectNameTypeKey)
    {
        if(projectIDSearch.containsKey(projectNameTypeKey))
        {
            return (Integer) projectIDSearch.get(projectNameTypeKey);
        }
        else
        {
            log.info("Project ID not found for project: " + projectNameTypeKey.getLeft() + " with annotation type: " + projectNameTypeKey.getRight());
            return null;
        }
    }


    public static boolean isWindowOpen() {
        return isWindowOpen;
    }

    public static void configureNewProject(@NonNull String projectName, @NonNull Integer projectID, Integer annotationType)
    {
        if(!checkAnnotationSanity(annotationType))
        {
            log.info("Saving new project of name: " + projectName + " failed.");
            return;
        }

        Pair projectNameWithType = new ImmutablePair(projectName, annotationType);

        projectIDSearch.put(projectNameWithType, projectID);

        projectIDLoaderDict.put(projectID, new ProjectLoader(projectID, projectName, annotationType));
    }

    private static boolean checkAnnotationSanity(Integer annotationTypeInt)
    {
        if(annotationTypeInt.equals(AnnotationType.BOUNDINGBOX.ordinal()))
        {
            return true;
        }
        else if(annotationTypeInt.equals(AnnotationType.SEGMENTATION.ordinal()))
        {
            return true;
        }
        else
        {
            log.debug("Annotation integer unmatched in AnnotationType: " + annotationTypeInt);
            return false;
        }
    }

    public static boolean initSelector(String selection)
    {
        if((selection.equals(ParamConfig.FILE)) || selection.equals(ParamConfig.FOLDER))
        {
            currentWindowSelection = selection;
        }
        else
        {
            log.error("Current input selector not allowed: " + selection + ". Allowed parameters are file/folder");
            return false;
        }
        return true;
    }

    public static boolean isProjectNameInMemory(String projectName, Integer annotationType)
    {
        if(!checkAnnotationSanity(annotationType))
        {
            log.info("Query whether project of name: " + projectName + " unique failed as annotationType invalid.");
            return false;
        }

        Set projectIDDictKeys = projectIDSearch.keySet();

        Boolean isProjectNameExist = false;

        for(Object key : projectIDDictKeys)
        {
            Pair projectNameType = (Pair) key;

            if(projectNameType.getLeft().equals(projectName) && projectNameType.getRight().equals(annotationType))
            {
                isProjectNameExist = true;
                break;
            }
        }

        return isProjectNameExist;
    }

    public static Boolean isProjectNameUnique(String projectName, Integer annotationType)
    {
        if(!checkAnnotationSanity(annotationType))
        {
            log.info("Query whether project of name: " + projectName + " unique failed as annotationType invalid.");
            return false;
        }

        Set projectIDDictKeys = projectIDSearch.keySet();

        Boolean isProjectNameUnique = true;

        for(Object key : projectIDDictKeys)
        {
            Pair projectNameType = (Pair) key;

            if(projectNameType.getLeft().equals(projectName) && projectNameType.getRight().equals(annotationType))
            {
                log.info("Project name: " + projectName + " exist. Proceed with choosing another project name");
                isProjectNameUnique = false;
                break;
            }
        }

        return isProjectNameUnique;
    }

    public static ProjectLoader getCurrentProjectLoader()
    {
        try
        {
            Integer projectID = getProjectID(selectorProjectBuffer);
            return getProjectLoader(projectID);
        }
        catch(Exception e)
        {
            log.info("Error in retrieving project loader from SelectorHandler");
        }
        return null;
    }


    public static Integer getProjectIDFromBuffer()
    {
        return getProjectID(selectorProjectBuffer);
    }

    public static void setProjectNameBuffer(String projectName, AnnotationType annotationType)
    {
        selectorProjectBuffer = new ImmutablePair(projectName, annotationType.ordinal());
    }

    public static boolean isLoaderProcessing()
    {
        return isLoaderProcessing;
    }

    public static void configureOpenWindow(String projectName, AnnotationType annotationType, Integer uuidGenerator)
    {
        ProjectLoader projectLoader = getProjectLoader(new ImmutablePair(projectName, annotationType.ordinal()));

        if(uuidGenerator == 0)
        {
            //prevent nan
            projectLoader.setProgressUpdate(new ArrayList<>(Arrays.asList(0, 1)));
        }
        else
        {
            projectLoader.setProgressUpdate(new ArrayList<>(Arrays.asList(0, uuidGenerator)));

        }

        isWindowOpen = true;
    }

    public static List<Integer> getProgressUpdate(String projectName, AnnotationType annotationType)
    {
        try
        {
            Integer projectID = getProjectID(projectName, annotationType.ordinal());

            ProjectLoader projectLoader = (ProjectLoader) projectIDLoaderDict.get(projectID);

            return projectLoader.getProgressUpdate();
        }
        catch(Exception e)
        {
            log.info("Error occurs when getting progress update", e);
            return null;
        }
    }

    public static void startDatabaseUpdate(@NonNull String projectName, AnnotationType annotationType) {
        selectorProjectBuffer = new ImmutablePair(projectName, annotationType.ordinal());
        selectorStatus = SelectorStatus.WINDOW_CLOSE_LOADING_FILES;
        getCurrentProjectLoader().setLoaderStatus(LoaderStatus.LOADING);
        isLoaderProcessing = true;
        isWindowOpen = false;
    }

    public static void stopDatabaseUpdate()
    {
        ProjectLoader loader = getCurrentProjectLoader();

        if(loader == null)
        {
            log.info("ProjectLoader is null for project name: " + selectorProjectBuffer.getLeft());
        }

        if(loader.getSanityUUIDList().isEmpty())
        {
            loader.setLoaderStatus(LoaderStatus.EMPTY);
        }
        else
        {
            loader.setLoaderStatus(LoaderStatus.LOADED);
        }

        isLoaderProcessing = false;
        selectorProjectBuffer = null;
    }
    /**
     * @param state true = open, false = close
     */
    public static void setWindowState(boolean state)
    {
        isWindowOpen = state;
    }

    public static Integer generateProjectID()
    {
        return projectIDGenerator.incrementAndGet();
    }

    public static void setProjectIDGenerator(Integer seedNumber)
    {
        projectIDGenerator = new AtomicInteger(seedNumber);
    }

}