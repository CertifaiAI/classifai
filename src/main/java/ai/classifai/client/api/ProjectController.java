package ai.classifai.client.api;

import ai.classifai.application.ProjectApplicationService;
import ai.classifai.client.request.CreateProjectBody;
import ai.classifai.data.enumeration.ProjectInfra;
import ai.classifai.data.enumeration.ProjectType;
import ai.classifai.dto.ProjectDTO;
import ai.classifai.utility.LabelListImport;
import ai.classifai.utility.UuidGenerator;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.File;

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProjectController {
    @Inject
    private ProjectApplicationService projectApplicationService;

    @POST
    @Path("/projects")
    public ProjectDTO createProject(CreateProjectBody createProjectBody) {
        ProjectDTO projectDTO = ProjectDTO.builder()
                .projectId(UuidGenerator.generateUuid())
                .projectName(createProjectBody.getProjectName())
                .projectPath(createProjectBody.getProjectPath())
                .projectType(ProjectType.getProjectType(createProjectBody.getAnnotationType()))
                .labelList(new LabelListImport(new File(createProjectBody.getLabelFilePath())).getValidLabelList())
                .projectInfra(ProjectInfra.getProjectInfra(createProjectBody.getProjectInfra()))
                .build();

        return projectApplicationService.createProject(projectDTO);
    }

}
