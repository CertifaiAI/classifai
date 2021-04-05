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
package ai.classifai.util.project;

import ai.classifai.database.versioning.ProjectVersion;
import ai.classifai.loader.CLIProjectInitiator;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.type.AnnotationHandler;
import ai.classifai.util.type.AnnotationType;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Project Handler for File & Folder Selector and Database Update
 *
 * @author codenamewei
 */
@Slf4j
public class ProjectHandler {

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

    static
    {
        projectIDLoaderDict = new HashMap<String, ProjectLoader>();
        projectIDSearch = new HashMap<Pair<String, Integer>, String>();
        projectNameSearch = new HashMap<String, Pair<String, Integer>>();
    }

    public static ProjectLoader getProjectLoader(String projectName, AnnotationType annotationType)
    {
        return getProjectLoader(new ImmutablePair(projectName, annotationType.ordinal()));
    }

    private static ProjectLoader getProjectLoader(Pair<String, Integer> project)
    {
        String projectIDKey = getProjectID(project);

        if (projectIDKey == null)
        {
            log.info("Null projectLoader due to projectID cannot be identified for the project: " + project.getLeft());
            return null;
        }

        return getProjectLoader(projectIDKey);
    }

    public static ProjectLoader getProjectLoader(String projectID)
    {
        try
        {
            return (ProjectLoader) projectIDLoaderDict.get(projectID);
        }
        catch (Exception e)
        {
            log.info("Error when retriveing ProjectLoader in ProjectHandler, ", e);
        }
        return null;
    }

    public static String getProjectID(Pair<String, Integer> projectNameTypeKey)
    {
        if (projectIDSearch.containsKey(projectNameTypeKey))
        {
            return (String) projectIDSearch.get(projectNameTypeKey);
        }
        else
        {
            log.info("Project ID not found for project: " + projectNameTypeKey.getLeft() + " with annotation type: " + projectNameTypeKey.getRight());
            return null;
        }
    }

    public static String getProjectId(String projectName, Integer annotationType)
    {
        Pair key = new ImmutablePair(projectName, annotationType);

        return getProjectID(key);
    }

    public static void loadProjectLoader(ProjectLoader loader)
    {
        if (!AnnotationHandler.checkSanity(loader.getAnnotationType()))
        {
            log.debug("Saving new project of name: " + loader.getProjectName() + " failed with invalid annotation type.");
        }

        Pair projectNameWithType = new ImmutablePair(loader.getProjectName(), loader.getAnnotationType());

        projectIDSearch.put(projectNameWithType, loader.getProjectId());
        projectNameSearch.put(loader.getProjectId(), projectNameWithType);

        projectIDLoaderDict.put(loader.getProjectId(), loader);

        ProjectVersion project = loader.getProjectVersion();

        loader.getUuidListFromDb().addAll(project.getCurrentUuidList());
        loader.getSanityUuidList().addAll(project.getCurrentUuidList());

        loader.getLabelList().addAll(project.getCurrentLabelList());
    }

    public static boolean initSelector(String selection)
    {
        if ((selection.equals(ParamConfig.getFileParam())) || selection.equals(ParamConfig.getFolderParam())) {
            return true;
        }
        else
        {
            log.error("Current input selector not allowed: " + selection + ". Allowed parameters are file/folder");
            return false;
        }
    }

    public static boolean isProjectNameUnique(String projectName, Integer annotationType)
    {
        if (!AnnotationHandler.checkSanity(annotationType))
        {
            log.info("Query whether project of name: " + projectName + " unique failed as annotationType invalid.");
            return false;
        }

        Set projectIDDictKeys = projectIDSearch.keySet();

        boolean isProjectNameUnique = true;

        for (Object key : projectIDDictKeys)
        {
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

    public static void deleteProjectFromCache(String projectID)
    {
        try
        {
            Pair projectPair = (Pair) projectNameSearch.remove(projectID);

            if (projectPair == null)
            {
                throw new NullPointerException("Deletion of ProjectPair from Project Handler failed.");
            }

            if (projectIDLoaderDict.remove(projectID) == null)
            {
                throw new NullPointerException("Deletion of Project from ProjectIDLoader failed.");
            }

            if (projectIDSearch.remove(projectPair) == null)
            {
                throw new NullPointerException("Deletion of Project from ProjectIDSearch failed.");
            }
        }
        catch (Exception e)
        {
            log.debug("Error: ", e);
        }
    }
}