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

package ai.certifai.database.portfolio;

import lombok.Getter;


public class PortfolioSQLQuery
{
    @Getter final static String queue = "portfolio.queue";

    private final static String CREATE_PORTFOLIO_TABLE = "create table if not exists Portfolio (projectid integer identity primary key, projectname varchar(255), labellist varchar(500), thumbnailmax integer, uuidlist clob)";
    private final static String CREATE_NEW_PROJECT = "insert into Portfolio values (?, ?, ?, ?, ?)";

    private final static String UPDATE_PROJECT = "update Portfolio set uuidlist = ? where projectname = ?";
    private final static String UPDATE_LABEL = "update Portfolio set labellist = ? where projectname = ?";

    private final static String GET_UUID_LABEL_LIST = "select labellist, uuidlist from Portfolio where projectname = ?";

    private final static String GET_PROJECT_ID_LIST = "select projectid from Portfolio";
    private final static String GET_PROJECT_NAME = "select projectname from Portfolio where projectid = ?";

    private final static String GET_PROJECT_UUID_LIST = "select uuidlist from Portfolio where projectname = ?";

    private final static String UPDATE_THUMBNAIL_MAX_INDEX = "update Portfolio set thumbnailmax = ? where projectname = ?";

    private final static String GET_THUMBNAIL_LIST = "select uuidlist, thumbnailmax from Portfolio where projectname = ?";

    private final static String GET_ALL_PROJECTS = "select projectname from Portfolio";

    public static String createPortfolioTable()
    {
        return CREATE_PORTFOLIO_TABLE;
    }

    public static String createNewProject()
    {
        return CREATE_NEW_PROJECT;
    }

    public static String updateProject()
    {
        return UPDATE_PROJECT;
    }

    public static String updateLabels()
    {
        return UPDATE_LABEL;
    }

    public static String getProjectIDList()
    {
        return GET_PROJECT_ID_LIST;
    }

    public static String getUUIDLabelList()
    {
        return GET_UUID_LABEL_LIST;
    }
    public static String getProjectUUIDList()
    {
        return GET_PROJECT_UUID_LIST;
    }

    public static String getAllProjects()
    {
        return GET_ALL_PROJECTS;
    }

    public static String getProjectName()
    {
        return GET_PROJECT_NAME;
    }

    public static String updateThumbNailMaxIndex()
    {
        return UPDATE_THUMBNAIL_MAX_INDEX;
    }

    public static String getThumbNailList()
    {
        return GET_THUMBNAIL_LIST;
    }

}

