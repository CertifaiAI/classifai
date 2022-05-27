package ai.classifai.client.api;

import ai.classifai.client.request.CreateProjectBody;
import ai.classifai.client.response.ResponseStatus;
import ai.classifai.core.services.ProjectServiceImpl;
import ai.classifai.data.enumeration.ProjectInfra;
import ai.classifai.data.enumeration.ProjectType;
import ai.classifai.dto.ProjectDTO;
import ai.classifai.utility.LabelListImport;
import ai.classifai.utility.UuidGenerator;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.List;

@Slf4j
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProjectController {
    @Inject
    private ProjectServiceImpl projectService;

    @POST
    @Path("/projects")
    public Future<Response> createProject(CreateProjectBody createProjectBody) {
        Promise<Response> promise = Promise.promise();

        ProjectDTO projectDTO = ProjectDTO.builder()
                .projectId(UuidGenerator.generateUuid())
                .projectName(createProjectBody.getProjectName())
                .projectPath(createProjectBody.getProjectPath())
                .projectType(ProjectType.getProjectType(createProjectBody.getAnnotationType()))
                .labelList(new LabelListImport(new File(createProjectBody.getLabelFilePath())).getValidLabelList())
                .projectInfra(ProjectInfra.getProjectInfra(createProjectBody.getProjectInfra()))
                .build();

        projectService.createProject(projectDTO);

        ResponseStatus status = ResponseStatus.builder()
                .message("Project created")
                .jsonObject(new JsonObject(projectDTO.toString()))
                .build();

        Response response = Response
                .status(Response.Status.OK)
                .entity(status)
                .build();

        promise.complete(response);

        return promise.future();
    }

    @GET
    @Path("/projects")
    public List<ProjectDTO> listProjectsMeta() {
        return projectService.listProjects();
    }

}
