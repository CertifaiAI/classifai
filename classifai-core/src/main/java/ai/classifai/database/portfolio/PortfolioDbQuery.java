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
    private static final String QUEUE = "portfolio.queue";

    private static final String CREATE_PORTFOLIO_TABLE = "CREATE TABLE IF NOT EXISTS Portfolio (project_id UUID, project_name VARCHAR(255), annotation_type INT, " +
            "label_list VARCHAR(10000), uuid_list CLOB, is_new BOOLEAN, is_starred BOOLEAN, created_date VARCHAR(255), PRIMARY KEY (project_id))";

    private static final String CREATE_NEW_PROJECT = "INSERT INTO Portfolio VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE_PROJECT = "UPDATE Portfolio SET uuid_list = ? WHERE project_id = ?";

    private static final String DELETE_PROJECT = "DELETE FROM Portfolio WHERE project_id = ?";

    private static final String UPDATE_LABEL_LIST = "UPDATE Portfolio set label_list = ? WHERE project_id = ?";

    private static final String LOAD_DB_PROJECT = "SELECT project_id, project_name, annotation_type, label_list, uuid_list, is_new FROM Portfolio";

    private static final String GET_ALL_PROJECTS_FOR_ANNOTATION_TYPE = "SELECT project_name FROM Portfolio WHERE annotation_type = ?";

    //V2
    private static final String GET_PROJECT_METADATA = "SELECT project_name, uuid_list, is_new, is_starred, created_date FROM Portfolio WHERE project_id = ?";

    private static final String GET_ALL_PROJECTS_METADATA = "SELECT project_name, uuid_list, is_new, is_starred, created_date FROM Portfolio WHERE annotation_type = ?";

    private static final String UPDATE_IS_NEW_PARAM = "UPDATE Portfolio SET is_new = ? WHERE project_id = ?";

    private static final String STAR_PROJECT = "UPDATE Portfolio SET is_starred = ? WHERE project_id = ?";

    private static final String GET_ALL_PROJECTS = "SELECT * FROM Portfolio";

    public static String getQueue() { return QUEUE; }

    public static String createPortfolioTable() { return CREATE_PORTFOLIO_TABLE; }

    public static String createNewProject() { return CREATE_NEW_PROJECT; }

    public static String updateProject() { return UPDATE_PROJECT; }

    public static String deleteProject() { return DELETE_PROJECT; }

    public static String updateLabelList() { return UPDATE_LABEL_LIST; }

    public static String loadDbProject() { return LOAD_DB_PROJECT; }

    public static String getAllProjectsForAnnotationType() { return GET_ALL_PROJECTS_FOR_ANNOTATION_TYPE; }
    //v2
    public static String updateIsNewParam() { return UPDATE_IS_NEW_PARAM; }

    public static String starProject() { return STAR_PROJECT; }

    public static String getProjectMetadata() { return GET_PROJECT_METADATA; }

    public static String getAllProjectsMetadata() { return GET_ALL_PROJECTS_METADATA; }

    public static String getAllProjects(){ return GET_ALL_PROJECTS; }
}