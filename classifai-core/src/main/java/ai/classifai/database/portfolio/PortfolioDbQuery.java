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
public class PortfolioDbQuery
{
    @Getter private static final String queue = "portfolio.queue";

    @Getter private static final String createPortfolioTable = "CREATE TABLE IF NOT EXISTS Portfolio (projectId UUID, projectName VARCHAR(255), annotationType INT, " +
            "labelList VARCHAR(10000), uuidList CLOB, isNew BOOLEAN, isStarred BOOLEAN, createdDate VARCHAR(255), PRIMARY KEY (projectId))";

    @Getter private static final String createNewProject = "INSERT INTO Portfolio VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    @Getter private static final String updateProject = "UPDATE Portfolio SET uuidList = ? WHERE projectId = ?";

    @Getter private static final String deleteProject = "DELETE FROM Portfolio WHERE projectId = ?";

    @Getter private static final String updateLabelList = "UPDATE Portfolio set labelList = ? WHERE projectId = ?";

    @Getter private static final String loadDbProject = "SELECT projectId, projectName, annotationType, labelList, uuidList, isNew FROM Portfolio";

    @Getter private static final String retrieveAllProjectsForAnnotationType = "SELECT projectName FROM Portfolio WHERE annotationType = ?";

    //V2
    @Getter private static final String retrieveProjectMetadata = "SELECT projectName, uuidList, isNew, isStarred, createdDate FROM Portfolio WHERE projectId = ?";

    @Getter private static final String retrieveAllProjectsMetadata = "SELECT projectName, uuidList, isNew, isStarred, createdDate FROM Portfolio WHERE annotationType = ?";

    @Getter private static final String updateIsNewParam = "UPDATE Portfolio SET isNew = ? WHERE projectId = ?";

    @Getter private static final String starProject = "UPDATE Portfolio SET isStarred = ? WHERE projectId = ?";

    @Getter private static final String retrieveAllProjects = "SELECT * FROM Portfolio";
}