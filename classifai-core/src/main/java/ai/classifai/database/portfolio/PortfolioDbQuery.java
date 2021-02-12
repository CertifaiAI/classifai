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

import lombok.Getter;

/***
 * Generic database query
 *
 * @author codenamewei
 */
@Getter
public class PortfolioDbQuery
{
    private static final String queue = "portfolio.queue";

    private static final String createPortfolioTable = "CREATE TABLE IF NOT EXISTS Portfolio (project_id UUID, project_name VARCHAR(255), annotation_type INT, " +
            "project_path VARCHAR(255), label_list VARCHAR(10000), uuid_list CLOB, is_new BOOLEAN, is_starred BOOLEAN, created_date VARCHAR(255), PRIMARY KEY (project_id))";

    private static final String createNewProject = "INSERT INTO Portfolio VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String updateProject = "UPDATE Portfolio SET uuid_list = ? WHERE project_id = ?";

    private static final String deleteProject = "DELETE FROM Portfolio WHERE project_id = ?";

    private static final String updateLabelList = "UPDATE Portfolio set label_list = ? WHERE project_id = ?";

    private static final String loadDbProject = "SELECT project_id, project_name, annotation_type, project_path, label_list, uuid_list, is_new FROM Portfolio";

    private static final String retrieveAllProjectsForAnnotationType =  "SELECT project_name FROM Portfolio WHERE annotation_type = ?";

    //V2
    private static final String retrieveProjectMetadata = "SELECT project_name, uuid_list, is_new, is_starred, created_date FROM Portfolio WHERE project_id = ?";

    private static final String retrieveAllProjectsMetadata = "SELECT project_name, uuid_list, is_new, is_starred, created_date FROM Portfolio WHERE annotation_type = ?";

    private static final String updateIsNewParam = "UPDATE Portfolio SET is_new = ? WHERE project_id = ?";

    private static final String starProject = "UPDATE Portfolio SET is_starred = ? WHERE project_id = ?";

    private static final String retrieveAllProjects = "SELECT * FROM Portfolio";
}