package ai.classifai.frontend.api;

import ai.classifai.backend.utility.LabelListImport;
import ai.classifai.core.dto.BoundingBoxDTO;
import ai.classifai.core.dto.ProjectDTO;
import ai.classifai.core.dto.SegmentationDTO;
import ai.classifai.core.dto.properties.ImageProperties;
import ai.classifai.core.enumeration.ProjectInfra;
import ai.classifai.core.enumeration.ProjectType;
import ai.classifai.core.service.annotation.AnnotationService;
import ai.classifai.core.service.project.ProjectService;
import ai.classifai.frontend.request.CreateProjectBody;
import ai.classifai.frontend.response.ActionStatus;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.File;

@Slf4j
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProjectController {
    private final ProjectService projectService;
    private final AnnotationService<BoundingBoxDTO, ImageProperties> imageBoundingBoxService;
    private final AnnotationService<SegmentationDTO, ImageProperties> imageSegmentationService;

    public ProjectController(ProjectService projectService,
                             AnnotationService<BoundingBoxDTO, ImageProperties> imageBoundingBoxService,
                             AnnotationService<SegmentationDTO, ImageProperties> imageSegmentationService) {
        this.projectService = projectService;
        this.imageBoundingBoxService = imageBoundingBoxService;
        this.imageSegmentationService = imageSegmentationService;
    }

    @POST
    @Path("/projects")
    public Future<ActionStatus> createProject(CreateProjectBody createProjectBody) {
        Promise<ActionStatus> promise = Promise.promise();

        ProjectDTO projectDTO = ProjectDTO.builder()
                .projectName(createProjectBody.getProjectName())
                .projectPath(createProjectBody.getProjectPath())
                .projectType(ProjectType.getProjectType(createProjectBody.getAnnotationType()))
                .labelList(new LabelListImport(new File(createProjectBody.getLabelFilePath())).getValidLabelList())
                .projectInfra(ProjectInfra.getProjectInfra(createProjectBody.getProjectInfra()))
                .build();

        projectService.createProject(projectDTO)
                .onComplete(res -> {
                    if (res.succeeded()) {
                        initAnnotationProjectByType(projectDTO);
                        promise.complete(ActionStatus.okWithResponse(res.result()));
                    }

                    else if (res.failed()) {
                        promise.fail(res.cause());
                    }
                });

        return promise.future();
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
        ProjectDTO projectDTO = ProjectDTO.builder()
                .projectName(projectName)
                .projectType(ProjectType.getProjectType(annotationType))
                .build();
        return projectService.getProjectByNameAndType(projectDTO)
                .map(res -> ActionStatus.okWithResponse(res.get()))
                .otherwise(cause -> ActionStatus.failedWithMessage("Failed to retrieve project meta data for " + projectName));
    }

    @PUT
    @Path("/{annotation_type}/projects/{project_name}")
    public Future<ActionStatus> updateProject(@PathParam("annotation_type") String annotationType,
                                              @PathParam("project_name") String projectName) {
        Integer projectType = ProjectType.getProjectType(annotationType);
        ProjectDTO projectDTO = ProjectDTO.builder().projectName(projectName).projectType(projectType).build();
        return projectService.updateProject(projectDTO)
                .map(ActionStatus::okWithResponse)
                .otherwise(cause -> ActionStatus.failedWithMessage("Failed to update project meta data for " + projectName));
    }

    @DELETE
    @Path("/{annotation_type}/projects/{project_name}")
    public Future<ActionStatus> deleteProject(@PathParam("annotation_type") String annotationType,
                                           @PathParam("project_name") String projectName) {
        Integer projectType = ProjectType.getProjectType(annotationType);
        ProjectDTO projectDTO = ProjectDTO.builder().projectName(projectName).projectType(projectType).build();
        return projectService.deleteProject(projectDTO)
                .map(ActionStatus::okWithResponse)
                .otherwise(cause -> ActionStatus.failedWithMessage("Failed to delete project " + projectName));
    }

    private void initAnnotationProjectByType(ProjectDTO projectDTO) {
        Integer projectType = projectDTO.getProjectType();

        switch (projectType) {
            case 0 -> {
                ImageProperties imageProperties = ImageProperties.builder()
                        .projectName(projectDTO.getProjectName())
                        .projectPath(projectDTO.getProjectPath())
                        .build();
                this.imageBoundingBoxService.parseData(imageProperties);
            }

            case 1 -> {
                ImageProperties imageProperties = ImageProperties.builder()
                        .projectName(projectDTO.getProjectName())
                        .projectPath(projectDTO.getProjectPath())
                        .build();
                this.imageSegmentationService.parseData(imageProperties);
            }
        }
    }
 }
