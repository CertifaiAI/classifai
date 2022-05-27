package ai.classifai.repository;

import lombok.Getter;

public class SqlQueries {
    @Getter private static final String createProjectTable = "CREATE TABLE IF NOT EXISTS Project (project_id UUID, project_name VARCHAR(255), " +
            "annotation_type INT, project_path VARCHAR(255), project_infra VARCHAR(25), label_list VARCHAR(100), PRIMARY KEY (project_id))";

    @Getter private static final String createProject = "INSERT INTO Project VALUES(?, ?, ?, ?, ?, ?)";

    @Getter private static final String updateProject = "UPDATE Project SET project_id = ?, project_name = ?, annotation_type = ?, " +
            "project_path = ?, project_infra = ?, label_list = ?";

    @Getter private static final String listAllProject = "SELECT * FROM Project";

    @Getter private static final String retrieveProjectById = "SELECT * FROM Project WHERE project_id = ?";

    @Getter private static final String deleteProjectById = "DELETE FROM Project WHERE project_id = ?";
}
