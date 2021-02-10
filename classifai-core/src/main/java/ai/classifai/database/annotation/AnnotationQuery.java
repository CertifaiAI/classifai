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
public abstract class AnnotationQuery
{
    @Getter private static final String createProject = "CREATE TABLE IF NOT EXISTS Project (uuid UUID, projectId UUID, imgPath VARCHAR(2000), annotation CLOB, imgDepth INT, " +
            "imgX INT, imgY INT, imgW DOUBLE, imgH DOUBLE, fileSize INT, imgOriW INT, imgOriH INT, PRIMARY KEY(uuid, projectId))";

    @Getter private static final String retrieveData = "SELECT imgPath, annotation, imgX, imgY, imgW, imgH, fileSize, imgOriW, imgOriH from Project WHERE uuid = ? AND projectId = ?";

    @Getter private static final String updateData = "UPDATE Project SET annotation = ?, imgDepth = ?, imgX = ?, imgY = ?, imgW = ?, imgH = ?, fileSize = ?, imgOriW = ?, imgOriH = ? WHERE uuid = ? AND projectId = ?";

    @Getter private static final String createData = "INSERT INTO Project VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    @Getter private static final String retrieveDataPath = "SELECT imgPath FROM Project WHERE uuid = ? AND projectId = ?";

    @Getter private static final String loadValidProjectUUID = "SELECT imgPath FROM Project WHERE projectId = ? AND uuid = ?";

    @Getter private static final String DeleteProjectUuidListWithProjectId = "DELETE FROM Project WHERE projectId = ?";

    @Getter private static final String deleteProjectUuidList = "DELETE FROM Project WHERE projectId = ? AND uuid = ?";

    @Getter private static final String retrieveAllProjects = "SELECT * FROM Project";
}