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
package ai.classifai.backend.repository.query;

import lombok.Getter;

/**
 * Common SQL Query for Each Annotation Type
 *
 * @author codenamewei
 */
public class AnnotationQuery
{

    @Getter private static final String createProject = "CREATE TABLE IF NOT EXISTS Project (uuid UUID, project_id UUID, img_path VARCHAR(2000), version_list CLOB, img_depth INT, " +
            "img_ori_w INT, img_ori_h INT, file_size INT, PRIMARY KEY(uuid, project_id))";

    @Getter private static final String queryData = "Get data from cache";

    @Getter private static final String updateData = "UPDATE Project SET version_list = ?, img_depth = ?, img_ori_w = ?, img_ori_h = ?, file_size = ? WHERE uuid = ? AND project_id = ?";

    @Getter private static final String createData = "INSERT INTO Project VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    @Getter private static final String retrieveDataPath = "SELECT img_path FROM Project WHERE uuid = ? AND project_id = ?";

    @Getter private static final String loadValidProjectUuid = "SELECT img_path FROM Project WHERE project_id = ? AND uuid = ?";

    @Getter private static final String deleteProject = "DELETE FROM Project WHERE project_id = ?";

    @Getter private static final String deleteProjectData = "DELETE FROM Project WHERE project_id = ? AND uuid IN (?)";

    @Getter private static final String retrieveAllProjects = "SELECT * FROM Project";

    //v2
    @Getter private static final String queryUuid = "SELECT uuid FROM Project WHERE img_path = ? AND project_id = ?";

    @Getter private static final String extractProject = "SELECT uuid, img_path, version_list, img_depth, img_ori_w, img_ori_h, file_size FROM Project WHERE project_id = ?";

    @Getter private static final String renameProjectData = "UPDATE Project SET img_path = ? WHERE uuid = ? AND project_id = ?";

    /** Audio project queries */
    @Getter private static final String createWaveFormData = "INSERT INTO AudioWaveFormPeaks VALUES (?, ?, ?, ?, ?)";

    @Getter private static final String retrieveWavePeaks = "SELECT wave_peak FROM AudioWaveFormPeaks WHERE project_id = ?";

    @Getter private static final String createWaveFormTable = "CREATE TABLE IF NOT EXISTS AudioWaveFormPeaks (uuid UUID, project_id UUID, time_stamp DECIMAL, wave_peak INT, audio_path VARCHAR(2000)," +
            "PRIMARY KEY(uuid, project_id))";

    @Getter private static final String createAudioProject = "CREATE TABLE IF NOT EXISTS AudioProject (uuid UUID, project_id UUID, audio_path VARCHAR(2000), regions_props CLOB," +
            "PRIMARY KEY(uuid, project_id))";

    @Getter private static final String retrieveAudioData = "SELECT regions_props FROM AudioProject WHERE project_id = ?";

    @Getter private static final String createAudioData = "INSERT INTO AudioProject VALUES (?, ?, ?, ?)";

    @Getter private static final String updateAudioData = "UPDATE AudioProject SET regions_props = ? WHERE uuid = ? AND project_id = ?";

    @Getter private static final String deleteAudioData = "DELETE FROM AudioProject WHERE uuid = ? AND project_id = ?";

}
