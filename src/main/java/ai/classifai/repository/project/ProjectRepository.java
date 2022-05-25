package ai.classifai.repository.project;

import ai.classifai.dto.ProjectDTO;

public interface ProjectRepository {
    ProjectDTO createProject(ProjectDTO projectDTO);

    ProjectDTO listProject();

    ProjectDTO getProjectById();

    ProjectDTO updateProject();

    ProjectDTO deleteProject();
}
