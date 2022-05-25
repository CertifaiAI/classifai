package ai.classifai.repository.project;

import ai.classifai.dto.ProjectDTO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProjectRepoService implements ProjectRepository {

    @Override
    public ProjectDTO createProject(ProjectDTO projectDTO) {
        ProjectEntity projectEntity = ProjectEntity.builder()
                .projectId(projectDTO.getProjectId())
                .projectName(projectDTO.getProjectName())
                .projectPath(projectDTO.getProjectPath())
                .projectType(projectDTO.getProjectType())
                .projectInfra(projectDTO.getProjectInfra())
                .labelList(projectDTO.getLabelList())
                .build();

        return projectDTO;
    }

    @Override
    public ProjectDTO listProject() {
        return null;
    }

    @Override
    public ProjectDTO getProjectById() {
        return null;
    }

    @Override
    public ProjectDTO updateProject() {
        return null;
    }

    @Override
    public ProjectDTO deleteProject() {
        return null;
    }
}
