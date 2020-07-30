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

package ai.classifai.database.portfolio;

public class PortfolioSQLQuery
{
    public final static String QUEUE = "portfolio.queue";

    public final static String CREATE_PORTFOLIO_TABLE = "create table if not exists Portfolio (projectid integer identity primary key, projectname varchar(255), labellist varchar(1000), thumbnailmax integer, uuidlist clob)";
    public final static String CREATE_NEW_PROJECT = "insert into Portfolio values (?, ?, ?, ?, ?)";

    public final static String UPDATE_PROJECT = "update Portfolio set uuidlist = ? where projectname = ?";
    public final static String UPDATE_LABEL = "update Portfolio set labellist = ? where projectname = ?";

    public final static String GET_UUID_LABEL_LIST = "select labellist, uuidlist from Portfolio where projectname = ?";

    public final static String GET_PROJECT_ID_LIST = "select projectid from Portfolio";
    public final static String GET_PROJECT_NAME = "select projectname from Portfolio where projectid = ?";

    public final static String GET_PROJECT_UUID_LIST = "select uuidlist from Portfolio where projectname = ?";

    public final static String REMOVE_OBSOLETE_UUID_LIST = "Removal of obsolete uuid";

    public final static String UPDATE_THUMBNAIL_MAX_INDEX = "update Portfolio set thumbnailmax = ? where projectname = ?";

    public final static String GET_THUMBNAIL_LIST = "select uuidlist, thumbnailmax from Portfolio where projectname = ?";

    public final static String GET_ALL_PROJECTS = "select projectname from Portfolio";
}

