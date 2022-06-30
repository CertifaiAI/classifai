package ai.classifai.backend.application;

import ai.classifai.core.loader.ProjectHandler;
import ai.classifai.core.service.project.ProjectLoadService;
import ai.classifai.core.service.project.ProjectService;

public class ProjectLoadServiceImpl implements ProjectLoadService {
    private final ProjectHandler projectHandler;
    private final ProjectService projectService;

    public ProjectLoadServiceImpl(ProjectService projectService,
                                  ProjectHandler projectHandler)
    {
        this.projectService = projectService;
        this.projectHandler = projectHandler;
    }

    @Override
    public void updateProjectLoader() {

    }

    @Override
    public void configProjectLoader() {

    }
}
