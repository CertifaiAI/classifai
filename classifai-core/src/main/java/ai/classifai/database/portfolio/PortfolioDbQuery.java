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
 * Portfolio Table query
 *
 * @author codenamewei
 */
public class PortfolioDbQuery
{
    @Getter private static final String queue = "portfolio.queue";

    @Getter private static final String createPortfolioTable = "CREATE TABLE IF NOT EXISTS Portfolio (project_id UUID, project_name VARCHAR(255), annotation_type INT, " +
            "project_path VARCHAR(255), is_new BOOLEAN, is_starred BOOLEAN, is_cloud BOOLEAN, current_version VARCHAR(200), project_version VARCHAR(5000), uuid_project_version CLOB, label_project_version CLOB, PRIMARY KEY (project_id))";

    @Getter private static final String createNewProject = "INSERT INTO Portfolio VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    @Getter private static final String updateProject = "UPDATE Portfolio SET uuid_project_version = ? WHERE project_id = ?";

    @Getter private static final String deleteProject = "DELETE FROM Portfolio WHERE project_id = ?";

    @Getter private static final String updateLabelList = "UPDATE Portfolio set label_project_version = ? WHERE project_id = ?";

    @Getter private static final String retrieveAllProjectsForAnnotationType = "SELECT project_name FROM Portfolio WHERE annotation_type = ? ORDER BY project_name";

    //*******************************V2*******************************

    @Getter private static final String retrieveProjectMetadata = "retrieveProjectMetadata";

    @Getter private static final String retrieveAllProjectsMetadata = "retrieveAllProjectsMetadata";

    @Getter private static final String updateIsNewParam = "UPDATE Portfolio SET is_new = ? WHERE project_id = ?";

    @Getter private static final String starProject = "UPDATE Portfolio SET is_starred = ? WHERE project_id = ?";

    @Getter private static final String retrieveAllProjects = "SELECT * FROM Portfolio";

    @Getter private static final String exportProject = "SELECT * FROM Portfolio WHERE project_id = ?";

    @Getter private static final String reloadProject = "SELECT project_path FROM Portfolio WHERE project_id = ?";
}