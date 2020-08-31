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
package ai.classifai.database.portfoliodb;

/***
 * Portfolio database query
 *
 * @author Chiawei Lim
 */
public class PortfolioDbQuery
{
    public final static String QUEUE = "portfolio.queue";

    public final static String CREATE_PORTFOLIO_TABLE = "create table if not exists Portfolio (project_id integer identity primary key, project_name varchar(255), annotation_type integer, label_list varchar(8000), thumbnail_max integer, uuid_list clob)";

    public final static String CREATE_NEW_PROJECT = "insert into Portfolio values (?, ?, ?, ?, ?, ?)";

    public final static String UPDATE_PROJECT = "update Portfolio set uuid_list = ? where project_name = ?";

    public final static String UPDATE_LABEL_LIST = "update Portfolio set label_list = ? where project_name = ?";

    public final static String GET_PROJECT_LABEL_LIST = "select label_list from Portfolio where project_name = ?";

    public final static String GET_PROJECT_ID_LIST = "select project_id from Portfolio";

    public final static String GET_PROJECT_NAME = "select project_name from Portfolio where project_id = ?";

    public final static String GET_PROJECT_UUID_LIST = "select uuid_list from Portfolio where project_name = ?";

    //public final static String REMOVE_OBSOLETE_UUID_LIST = "Removal of obsolete uuid";
    public final static String LOAD_PROJECT_UUID_LIST = "load project uuid list";

    public final static String UPDATE_THUMBNAIL_MAX_INDEX = "update Portfolio set thumbnail_max = ? where project_name = ?";

    public final static String GET_THUMBNAIL_LIST = "select uuid_list, thumbnail_max from Portfolio where project_name = ?";

    public final static String GET_ALL_PROJECTS_FOR_ANNOTATION_TYPE = "select project_name from Portfolio where annotation_type = ?";
}