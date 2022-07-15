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
    /** Image project queries */
    @Getter private static final String createImageProject = "CREATE TABLE IF NOT EXISTS ImageProject (uuid UUID, project_id UUID, img_path VARCHAR(2000), version_list CLOB, img_depth INT, " +
            "img_ori_w INT, img_ori_h INT, file_size INT, PRIMARY KEY(uuid, project_id))";

    @Getter private static final String queryData = "Get data from cache";

    @Getter private static final String updateImageData = "UPDATE ImageProject SET version_list = ?, img_depth = ?, img_ori_w = ?, img_ori_h = ?, file_size = ? WHERE uuid = ? AND project_id = ?";

    @Getter private static final String createImageData = "INSERT INTO ImageProject VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    @Getter private static final String retrieveDataPathFromImageProject = "SELECT img_path FROM ImageProject WHERE uuid = ? AND project_id = ?";

    @Getter private static final String loadValidImageProjectUuid = "SELECT img_path FROM ImageProject WHERE project_id = ? AND uuid = ?";

    @Getter private static final String deleteImageProject = "DELETE FROM ImageProject WHERE project_id = ?";

    @Getter private static final String deleteImageProjectData = "DELETE FROM ImageProject WHERE project_id = ? AND uuid IN (?)";

    @Getter private static final String retrieveAllImageProjects = "SELECT * FROM ImageProject";

    @Getter private static final String retrieveImageProjectById = "SELECT * FROM ImageProject WHERE project_id = ?";

    //v2
    @Getter private static final String queryImageUuid = "SELECT uuid FROM ImageProject WHERE img_path = ? AND project_id = ?";

    @Getter private static final String  extractImageProject = "SELECT uuid, img_path, version_list, img_depth, img_ori_w, img_ori_h, file_size FROM ImageProject WHERE project_id = ?";

    @Getter private static final String renameImageProjectData = "UPDATE ImageProject SET img_path = ? WHERE uuid = ? AND project_id = ?";

    /** Video project queries */
    @Getter private static final String createVideoProject = "CREATE TABLE IF NOT EXISTS VideoProject (uuid UUID, project_id UUID, video_frame_index INT, video_time_stamp INT, img_path VARCHAR(2000), video_file_path VARCHAR(2000), " +
            " version_list CLOB, img_depth INT, img_ori_w INT, img_ori_h INT, file_size INT, PRIMARY KEY(uuid, project_id))";

    @Getter private static final String createVideoData = "INSERT INTO VideoProject VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    @Getter private static final String retrieveVideoDataPath = "SELECT img_path FROM VideoProject WHERE uuid = ? AND project_id = ?";

    @Getter private static final String loadValidVideoProjectUuid = "SELECT img_path FROM VideoProject WHERE project_id = ? AND uuid = ?";

    @Getter private static final String deleteVideoProject = "DELETE FROM VideoProject WHERE project_id = ?";

    @Getter private static final String extractVideoProject = "SELECT uuid, img_path, video_frame_index, video_time_stamp, video_file_path, version_list, img_depth, img_ori_w, img_ori_h, file_size FROM VideoProject WHERE project_id = ?";

    @Getter private static final String updateVideoData = "UPDATE VideoProject SET version_list = ?, img_depth = ?, img_ori_w = ?, img_ori_h = ?, file_size = ? WHERE uuid = ? AND project_id = ?";

    @Getter private static final String deleteVideoProjectData = "DELETE FROM VideoProject WHERE project_id = ? AND uuid IN (?)";

    /** Audio project queries */
    @Getter private static final String createWaveFormData = "INSERT INTO AudioWaveFormPeaks VALUES (?, ?, ?, ?, ?)";

    @Getter private static final String retrieveWavePeaks = "SELECT wave_peak FROM AudioWaveFormPeaks WHERE project_id = ?";

    @Getter private static final String createWaveFormTable = "CREATE TABLE IF NOT EXISTS AudioWaveFormPeaks (uuid UUID, project_id UUID, time_stamp DECIMAL, wave_peak INT, audio_path VARCHAR(2000)," +
            "PRIMARY KEY(uuid, project_id))";

    @Getter private static final String createAudioProject = "CREATE TABLE IF NOT EXISTS AudioProject (uuid UUID, project_id UUID, audio_path VARCHAR(2000), audio_duration FLOAT, frame_rate FLOAT, frame_size INT, channel INT, sample_rate FLOAT, bit INT, regions_props CLOB," +
            "PRIMARY KEY(uuid, project_id))";

    @Getter private static final String retrieveAudioData = "SELECT * FROM AudioProject WHERE project_id = ?";

    @Getter private static final String createAudioData = "INSERT INTO AudioProject VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    @Getter private static final String updateAudioData = "UPDATE AudioProject SET regions_props = ? WHERE uuid = ? AND project_id = ?";

    @Getter private static final String deleteAudioData = "DELETE FROM AudioProject WHERE uuid = ? AND project_id = ?";

    @Getter private static final String deleteAudioProject = "DELETE FROM AudioProject WHERE project_id = ?";

}
