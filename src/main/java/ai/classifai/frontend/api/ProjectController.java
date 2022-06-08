package ai.classifai.frontend.api;

import ai.classifai.core.dto.ProjectDTO;
import ai.classifai.core.enumeration.ProjectInfra;
import ai.classifai.core.enumeration.ProjectType;
import ai.classifai.core.service.project.ProjectService;
import ai.classifai.frontend.request.CreateProjectBody;
import ai.classifai.frontend.response.ActionStatus;
import io.vertx.core.Future;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;

@Slf4j
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @POST
    @Path("/projects")
    public Future<ActionStatus> createProject(CreateProjectBody createProjectBody) {
        ProjectDTO projectDTO = ProjectDTO.builder()
                .projectName(createProjectBody.getProjectName())
                .projectPath(createProjectBody.getProjectPath())
                .projectType(ProjectType.getProjectType(createProjectBody.getAnnotationType()))
                .labelList(Arrays.asList(createProjectBody.getLabelFilePath().split(",")))
                .projectInfra(ProjectInfra.getProjectInfra(createProjectBody.getProjectInfra()))
                .build();

        return projectService.createProject(projectDTO)
                .map(ActionStatus::okWithResponse)
                .otherwise(cause -> ActionStatus.failedWithMessage("Failed to create project"));
    }

    @GET
    @Path("/{annotation_type}/projects/meta")
    public Future<ActionStatus> listProjectsMeta(@PathParam("annotation_type") String annotationType) {
        return projectService.listProjects(ProjectType.getProjectType(annotationType))
                .map(ActionStatus::okWithResponse)
                .otherwise(cause -> ActionStatus.failedWithMessage("Failed to retrieve projects meta data"));
    }

    @GET
    @Path("/{annotation_type}/projects/{project_name}")
    public Future<ActionStatus> getProjectMetaById(@PathParam("annotation_type") String annotationType,
                                                   @PathParam("project_name") String projectName) {
        return projectService.getProjectById(projectName, ProjectType.getProjectType(annotationType))
                .map(res -> ActionStatus.okWithResponse(res.get()))
                .otherwise(cause -> ActionStatus.failedWithMessage("Failed to retrieve project meta data for " + projectName));
    }

    @PUT
    @Path("/{annotation_type}/projects/{project_name}")
    public Future<ActionStatus> updateProject(@PathParam("annotation_type") String annotationType,
                                              @PathParam("project_name") String projectName) {
        return projectService.updateProject(projectName, ProjectType.getProjectType(annotationType))
                .map(ActionStatus::okWithResponse)
                .otherwise(cause -> ActionStatus.failedWithMessage("Failed to update project meta data for " + projectName));
    }

    @DELETE
    @Path("/{annotation_type}/projects/{project_name}")
    public Future<ActionStatus> deleteProject(@PathParam("annotation_type") String annotationType,
                                           @PathParam("project_name") String projectName) {
        return projectService.deleteProject(projectName, ProjectType.getProjectType(annotationType))
                .map(ActionStatus::okWithResponse)
                .otherwise(cause -> ActionStatus.failedWithMessage("Failed to delete project " + projectName));
    }
}
