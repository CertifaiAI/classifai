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
package ai.classifai.database.annotation;

import lombok.Getter;

/**
 * Common SQL Query for Each Annotation Type
 *
 * @author codenamewei
 */
public class AnnotationQuery
{
    @Getter private static final String createProject = "CREATE TABLE IF NOT EXISTS Project (uuid UUID, project_id UUID, img_path VARCHAR(2000), annotation CLOB, img_depth INT, " +
            "img_x INT, img_y INT, img_w DOUBLE, img_h DOUBLE, file_size INT, img_ori_w INT, img_ori_h INT, PRIMARY KEY(uuid, project_id))";

    @Getter private static final String queryData = "SELECT img_path, annotation, img_x, img_y, img_w, img_h, file_size, img_ori_w, img_ori_h from Project WHERE uuid = ? AND project_id = ?";

    @Getter private static final String updateData = "UPDATE Project SET annotation = ?, img_depth = ?,  img_x = ?, img_y = ?, img_w = ?, img_h = ?, file_size = ?, img_ori_w = ?, img_ori_h = ? WHERE uuid = ? AND project_id = ?";

    @Getter private static final String createData = "INSERT INTO Project VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    @Getter private static final String retrieveDataPath = "SELECT img_path FROM Project WHERE uuid = ? AND project_id = ?";

    @Getter private static final String loadValidProjectUUID = "SELECT img_path FROM Project WHERE project_id = ? AND uuid = ?";

    @Getter private static final String deleteProject = "DELETE FROM Project WHERE project_id = ?";

    @Getter private static final String deleteSelectionUuidList = "DELETE FROM Project WHERE project_id = ? AND uuid IN (?)";

    @Getter private static final String retrieveAllProjects = "SELECT * FROM Project";

    //v2
    @Getter private static final String queryUuid = "SELECT uuid FROM Project WHERE img_path = ? AND project_id = ?";

    @Getter private static final String exportProject = "SELECT * FROM Project WHERE project_id = ?";

}