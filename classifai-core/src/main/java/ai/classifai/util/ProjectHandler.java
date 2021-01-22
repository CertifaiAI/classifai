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
package ai.classifai.util;

import ai.classifai.loader.CLIProjectInitiator;
import ai.classifai.loader.LoaderStatus;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.util.type.AnnotationHandler;
import ai.classifai.util.type.AnnotationType;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Project Handler for File & Folder Selector and Database Update
 *
 * @author codenamewei
 */
@Slf4j
public class ProjectHandler {

    //unique project id generator for each project
    private static AtomicInteger projectIDGenerator;

    //key: projectID
    //value: ProjectLoader
    private static Map projectIDLoaderDict;

    //key: Pair<String projectName, Integer annotationType>
    //value: projectID
    private static Map projectIDSearch;

    //key: projectID
    //value: Pair<String projectName, Integer annotationType>
    private static Map projectNameSearch;

    @Getter @Setter private static CLIProjectInitiator cliProjectInitiator = null;


    static {

        projectIDGenerator = new AtomicInteger(0);

        projectIDLoaderDict = new HashMap<Integer, ProjectLoader>();
        projectIDSearch = new HashMap<Pair<String, Integer>, Integer>();
        projectNameSearch = new HashMap<Integer, Pair<String, Integer>>();
    }


    public static Integer generateProjectID() {
        return projectIDGenerator.incrementAndGet();
    }

    public static void setProjectIDGenerator(Integer seedNumber) {
        projectIDGenerator = new AtomicInteger(seedNumber);
    }

    public static ProjectLoader getProjectLoader(String projectName, AnnotationType annotationType) {
        return getProjectLoader(new ImmutablePair(projectName, annotationType.ordinal()));
    }

    private static ProjectLoader getProjectLoader(Pair<String, Integer> project) {
        Integer projectIDKey = getProjectID(project);

        if (projectIDKey == null) {
            log.info("Null projectLoader due to projectID cannot be identified for the project: " + project.getLeft());
            return null;
        }

        return getProjectLoader(projectIDKey);
    }

    public static ProjectLoader getProjectLoader(Integer projectID) {
        try {
            return (ProjectLoader) projectIDLoaderDict.get(projectID);
        } catch (Exception e) {
            log.info("Error when retriveing ProjectLoader in ProjectHandler, ", e);
        }
        return null;
    }

    public static Integer getProjectID(Pair<String, Integer> projectNameTypeKey) {
        if (projectIDSearch.containsKey(projectNameTypeKey)) {
            return (Integer) projectIDSearch.get(projectNameTypeKey);
        } else {
            log.info("Project ID not found for project: " + projectNameTypeKey.getLeft() + " with annotation type: " + projectNameTypeKey.getRight());
            return null;
        }
    }

    public static Integer getProjectID(String projectName, Integer annotationType) {
        Pair key = new ImmutablePair(projectName, annotationType);

        return getProjectID(key);
    }


    public static ProjectLoader buildProjectLoader(@NonNull String projectName, @NonNull Integer projectID, @NonNull Integer annotationType, LoaderStatus loaderStatus, boolean isNew)
    {
        if (!AnnotationHandler.checkSanity(annotationType))
        {
            log.debug("Saving new project of name: " + projectName + " failed.");
        }

        Pair projectNameWithType = new ImmutablePair(projectName, annotationType);

        projectIDSearch.put(projectNameWithType, projectID);
        projectNameSearch.put(projectID, projectNameWithType);

        ProjectLoader loader = new ProjectLoader(projectID, projectName, annotationType, loaderStatus, isNew);

        projectIDLoaderDict.put(projectID, loader);

        return loader;
    }

    public static boolean initSelector(String selection) {
        if ((selection.equals(ParamConfig.getFileParam())) || selection.equals(ParamConfig.getFolderParam())) {
            return true;
        } else {
            log.error("Current input selector not allowed: " + selection + ". Allowed parameters are file/folder");
            return false;
        }
    }

    public static boolean isProjectNameUnique(String projectName, Integer annotationType) {
        if (!AnnotationHandler.checkSanity(annotationType)) {
            log.info("Query whether project of name: " + projectName + " unique failed as annotationType invalid.");
            return false;
        }

        Set projectIDDictKeys = projectIDSearch.keySet();

        boolean isProjectNameUnique = true;

        for (Object key : projectIDDictKeys) {
            Pair projectNameType = (Pair) key;

            if (projectNameType.getLeft().equals(projectName) && projectNameType.getRight().equals(annotationType))
            {
                log.debug("Project name: " + projectName + " exist. Proceed with choosing another project name");
                isProjectNameUnique = false;
                break;
            }
        }

        return isProjectNameUnique;
    }

    public static void deleteProjectWithID(Integer projectID) {
        try {
            Pair projectPair = (Pair) projectNameSearch.remove(projectID);

            if (projectPair == null) {
                throw new NullPointerException("Deletion of ProjectPair from Project Handler failed.");
            }

            if (projectIDLoaderDict.remove(projectID) == null) {
                throw new NullPointerException("Deletion of Project from ProjectIDLoader failed.");
            }

            if (projectIDSearch.remove(projectPair) == null) {
                throw new NullPointerException("Deletion of Project from ProjectIDSearch failed.");
            }
        } catch (Exception e) {
            log.debug("Error: ", e);
        }
    }
}