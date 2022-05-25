package ai.classifai.application;

import ai.classifai.core.services.ProjectServiceImpl;
import ai.classifai.dto.ProjectDTO;

import javax.inject.Inject;
import java.util.List;

public class ProjectApplicationService {
    @Inject
    private ProjectServiceImpl projectService;

    public ProjectDTO createProject(ProjectDTO projectDTO) {
        return projectService.createProject(projectDTO);
    }

    public List<ProjectDTO> listProjects() {
        return projectService.listProjects();
    }

    public ProjectDTO getProjectById(String projectId) {
        return projectService.getProjectById(projectId);
    }

    public ProjectDTO updateProject(ProjectDTO projectDTO) {
        return projectService.updateProject(projectDTO);
    }

    public void deleteProject(String projectId) {
        projectService.deleteProject(projectId);
    }
}
