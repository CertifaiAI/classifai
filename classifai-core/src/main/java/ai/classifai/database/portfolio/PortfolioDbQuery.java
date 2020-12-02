/*
 * Copyright (c) 2020 CertifAI Sdn. Bhd.
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

/***
 * Generic Database Query
 *
 * @author codenamewei
 */
public class PortfolioDbQuery
{
    private final static String QUEUE = "portfolio.queue";

    private final static String CREATE_PORTFOLIO_TABLE = "create table if not exists Portfolio (project_id integer identity primary key, project_name varchar(255), annotation_type integer, label_list varchar(8000), uuid_generator_seed integer, uuid_list clob)";

    private final static String CREATE_NEW_PROJECT = "insert into Portfolio values (?, ?, ?, ?, ?, ?)";

    private final static String UPDATE_PROJECT = "update Portfolio set uuid_list = ? where project_id = ?";

    private final static String DELETE_PROJECT = "delete from Portfolio where project_id = ?";

    private final static String UPDATE_UUID_GENERATOR_SEED = "update Portfolio set uuid_generator_seed = ? where project_id = ?";

    private final static String UPDATE_LABEL_LIST = "update Portfolio set label_list = ? where project_id = ?";

    private final static String GET_PROJECT_LABEL_LIST = "select label_list from Portfolio where project_id = ?";

    private final static String GET_PROJECT_ID_LIST = "select project_id from Portfolio";

    private final static String GET_PROJECT_NAME = "select project_name, annotation_type from Portfolio where project_id = ?";

    private final static String GET_PROJECT_UUID_LIST = "select uuid_list, uuid_generator_seed from Portfolio where project_id = ?";

    private final static String GET_ALL_PROJECTS_FOR_ANNOTATION_TYPE = "select project_name from Portfolio where annotation_type = ?";

    public static String getQueue(){ return QUEUE; }

    public static String createPortfolioTable() { return CREATE_PORTFOLIO_TABLE; }

    public static String createNewProject() { return CREATE_NEW_PROJECT; }

    public static String updateProject() { return UPDATE_PROJECT; }

    public static String deleteProject() { return DELETE_PROJECT; }

    public static String updateUUIDGeneratorSeed() { return UPDATE_UUID_GENERATOR_SEED; }

    public static String updateLabelList() { return UPDATE_LABEL_LIST; }

    public static String getProjectLabelList() { return GET_PROJECT_LABEL_LIST; }

    public static String getProjectName() { return GET_PROJECT_NAME; }

    public static String getProjectIDList() { return GET_PROJECT_ID_LIST; }

    public static String getProjectUUIDList() { return GET_PROJECT_UUID_LIST; }

    public static String getAllProjectsForAnnotationType() { return GET_ALL_PROJECTS_FOR_ANNOTATION_TYPE; }

}