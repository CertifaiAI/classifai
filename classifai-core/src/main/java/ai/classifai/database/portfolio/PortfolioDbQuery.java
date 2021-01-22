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
package ai.classifai.database.portfolio;

/***
 * Generic database query
 *
 * @author codenamewei
 */
public class PortfolioDbQuery
{
    private final static String QUEUE = "portfolio.queue";

    private final static String CREATE_PORTFOLIO_TABLE = "CREATE TABLE IF NOT EXISTS Portfolio (project_id integer identity PRIMARY KEY, project_name varchar(255), annotation_type integer, label_list varchar(10000), uuid_generator_seed integer, uuid_list varchar(65535), is_new boolean, is_starred boolean, created_date varchar(255))";

    private final static String CREATE_NEW_PROJECT = "INSERT INTO Portfolio VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private final static String UPDATE_PROJECT = "UPDATE Portfolio SET uuid_list = ? WHERE project_id = ?";

    private final static String DELETE_PROJECT = "DELETE FROM Portfolio WHERE project_id = ?";

    private final static String UPDATE_UUID_GENERATOR_SEED = "UPDATE Portfolio SET uuid_generator_seed = ? WHERE project_id = ?";

    private final static String UPDATE_LABEL_LIST = "UPDATE Portfolio set label_list = ? WHERE project_id = ?";

    private final static String LOAD_DB_PROJECT = "SELECT project_id, project_name, annotation_type, label_list, uuid_generator_seed, uuid_list, is_new FROM Portfolio";

    private final static String GET_PROJECT_UUID_LIST = "SELECT uuid_list FROM Portfolio WHERE project_id = ?";

    private final static String GET_ALL_PROJECTS_FOR_ANNOTATION_TYPE = "SELECT project_name FROM Portfolio WHERE annotation_type = ?";

    //V2
    private final static String GET_PROJECT_METADATA = "SELECT project_name, uuid_list, is_new, is_starred, created_date FROM Portfolio WHERE project_id = ?";

    private final static String GET_ALL_PROJECTS_METADATA = "SELECT project_name, uuid_list, is_new, is_starred, created_date FROM Portfolio WHERE annotation_type = ?";

    private final static String UPDATE_IS_NEW_PARAM = "UPDATE Portfolio SET is_new = ? WHERE project_id = ?";

    private final static String STAR_PROJECT = "UPDATE Portfolio SET is_starred = ? WHERE project_id = ?";

    public static String getQueue(){ return QUEUE; }

    public static String createPortfolioTable() { return CREATE_PORTFOLIO_TABLE; }

    public static String createNewProject() { return CREATE_NEW_PROJECT; }

    public static String updateProject() { return UPDATE_PROJECT; }

    public static String deleteProject() { return DELETE_PROJECT; }

    public static String updateUUIDGeneratorSeed() { return UPDATE_UUID_GENERATOR_SEED; }

    public static String updateLabelList() { return UPDATE_LABEL_LIST; }

    public static String loadDbProject() { return LOAD_DB_PROJECT; }

    public static String getProjectUUIDList() { return GET_PROJECT_UUID_LIST; }

    public static String getAllProjectsForAnnotationType() { return GET_ALL_PROJECTS_FOR_ANNOTATION_TYPE; }

    //v2
    public static String updateIsNewParam() { return UPDATE_IS_NEW_PARAM; }

    public static String starProject() { return STAR_PROJECT; }

    public static String getProjectMetadata() { return GET_PROJECT_METADATA; }

    public static String getAllProjectsMetadata() { return GET_ALL_PROJECTS_METADATA; }
}