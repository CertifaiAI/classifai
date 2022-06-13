package ai.classifai.backend.repository;

import lombok.Getter;

public class SqlQueries {
    /** Project queries */
    @Getter private static final String createProjectTable = "CREATE TABLE IF NOT EXISTS Project (project_id UUID, project_name VARCHAR(255), " +
            "annotation_type INT, project_path VARCHAR(255), project_infra INT, label_list CLOB, PRIMARY KEY (project_id))";

    @Getter private static final String createProject = "INSERT INTO Project VALUES(?, ?, ?, ?, ?, ?)";

    @Getter private static final String updateProject = "UPDATE Project SET project_id = ?, project_name = ?, annotation_type = ?, " +
            "project_path = ?, project_infra = ?, label_list = ?";

    @Getter private static final String listAllProject = "SELECT * FROM Project";

    @Getter private static final String retrieveProjectById = "SELECT * FROM Project WHERE project_id = ?";

    @Getter private static final String retrieveProjectByNameAndType = "SELECT * FROM Project WHERE project_name = ? AND annotation_type = ?";

    @Getter private static final String deleteProjectById = "DELETE FROM Project WHERE project_id = ?";

    /** Image Project queries */
    @Getter private static final String createImageProject = "CREATE TABLE IF NOT EXISTS ImageProject (uuid UUID, project_id UUID, project_name VARCHAR(255), img_path VARCHAR(2000), img_depth INT, " +
            "img_ori_w INT, img_ori_h INT, img_x INT, img_y INT, img_w INT, img_h INT, bnd_box CLOB, file_size INT, img_thumbnail CLOB, PRIMARY KEY(uuid, project_id))";

    @Getter private static final String queryData = "Get data from cache";

    @Getter private static final String updateData = "UPDATE ImageProject SET img_depth = ?, img_ori_w = ?, img_ori_h = ?, file_size = ? WHERE uuid = ? AND project_id = ?";

    @Getter private static final String createData = "INSERT INTO ImageProject VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    @Getter private static final String updateImageData = "UPDATE ImageProject SET bnd_box = ? WHERE uuid = ? AND project_name = ?";

    @Getter private static final String retrieveDataPath = "SELECT img_path FROM ImageProject WHERE uuid = ? AND project_id = ?";

    @Getter private static final String loadValidProjectUuid = "SELECT img_path FROM ImageProject WHERE project_id = ? AND uuid = ?";

    @Getter private static final String deleteImageProject = "DELETE FROM ImageProject WHERE project_id = ?";

    @Getter private static final String deleteImageProjectData = "DELETE FROM ImageProject WHERE project_name = ? AND uuid IN (?)";

    @Getter private static final String retrieveAllProject = "SELECT * FROM ImageProject";

    @Getter private static final String retrieveImageProjectByName = "SELECT * FROM ImageProject WHERE project_name = ?";

    //v2
    @Getter private static final String queryUuid = "SELECT uuid FROM ImageProject WHERE img_path = ? AND project_id = ?";

    @Getter private static final String extractProject = "SELECT uuid, img_path, img_depth, img_ori_w, img_ori_h, file_size FROM ImageProject WHERE project_id = ?";

    @Getter private static final String renameProjectData = "UPDATE ImageProject SET img_path = ? WHERE uuid = ? AND project_id = ?";

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
