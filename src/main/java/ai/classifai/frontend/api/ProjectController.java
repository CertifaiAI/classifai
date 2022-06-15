package ai.classifai.frontend.api;

import ai.classifai.backend.utility.action.LabelListImport;
import ai.classifai.core.dto.AudioDTO;
import ai.classifai.core.dto.BoundingBoxDTO;
import ai.classifai.core.dto.ProjectDTO;
import ai.classifai.core.dto.SegmentationDTO;
import ai.classifai.core.properties.AudioProperties;
import ai.classifai.core.properties.ImageProperties;
import ai.classifai.core.enumeration.ProjectInfra;
import ai.classifai.core.enumeration.AnnotationType;
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
    private final AnnotationService<AudioDTO, AudioProperties> audioService;

    public ProjectController(ProjectService projectService,
                             AnnotationService<BoundingBoxDTO, ImageProperties> imageBoundingBoxService,
                             AnnotationService<SegmentationDTO, ImageProperties> imageSegmentationService,
                             AnnotationService<AudioDTO, AudioProperties> audioService) {
        this.projectService = projectService;
        this.imageBoundingBoxService = imageBoundingBoxService;
        this.imageSegmentationService = imageSegmentationService;
        this.audioService = audioService;
    }

    @POST
    @Path("/projects")
    public Future<ActionStatus> createProject(CreateProjectBody createProjectBody) {
        Promise<ActionStatus> promise = Promise.promise();

        ProjectDTO projectDTO = ProjectDTO.builder()
                .projectName(createProjectBody.getProjectName())
                .projectPath(createProjectBody.getProjectPath())
                .annotationType(AnnotationType.getType(createProjectBody.getAnnotationType()).ordinal())
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
        return projectService.listProjects(AnnotationType.getAnnotationType(annotationType))
                .map(ActionStatus::okWithResponse)
                .otherwise(cause -> ActionStatus.failedWithMessage("Failed to retrieve projects meta data"));
    }

    @GET
    @Path("/{annotation_type}/projects/{project_name}")
    public Future<ActionStatus> getProjectMetaById(@PathParam("annotation_type") String annotationType,
                                                   @PathParam("project_name") String projectName) {
        ProjectDTO projectDTO = ProjectDTO.builder()
                .projectName(projectName)
                .annotationType(AnnotationType.getAnnotationType(annotationType))
                .build();

        return projectService.getProjectById(projectDTO.getProjectId())
                .map(res -> ActionStatus.okWithResponse(res.get()))
                .otherwise(cause -> ActionStatus.failedWithMessage("Failed to retrieve project meta data for " + projectName));
    }

    @PUT
    @Path("/{annotation_type}/projects/{project_name}")
    public Future<ActionStatus> updateProject(@PathParam("annotation_type") String annotationType,
                                              @PathParam("project_name") String projectName) {
        Integer projectType = AnnotationType.getAnnotationType(annotationType);
        ProjectDTO projectDTO = ProjectDTO.builder().projectName(projectName).annotationType(projectType).build();
        return projectService.updateProject(projectDTO)
                .map(ActionStatus::okWithResponse)
                .otherwise(cause -> ActionStatus.failedWithMessage("Failed to update project meta data for " + projectName));
    }

    @DELETE
    @Path("/{annotation_type}/projects/{project_name}")
    public Future<ActionStatus> deleteProject(@PathParam("annotation_type") String annotationType,
                                              @PathParam("project_name") String projectName) {
        Integer projectType = AnnotationType.getAnnotationType(annotationType);
        ProjectDTO projectDTO = ProjectDTO.builder()
                .projectName(projectName)
                .annotationType(projectType)
                .build();

        return projectService.deleteProject(projectDTO)
                .compose(res -> deleteAnnotationProject(projectDTO))
                .map(ActionStatus::okWithResponse)
                .otherwise(cause -> ActionStatus.failedWithMessage("Failed to delete project " + projectName));
    }

    private void initAnnotationProjectByType(ProjectDTO projectDTO) {
        Integer projectType = projectDTO.getAnnotationType();
        String projectName = projectDTO.getProjectName();
        String projectPath = projectDTO.getProjectPath();

        switch (projectType) {
            case 0 -> {
                ImageProperties imageProperties = ImageProperties.builder()
                        .projectName(projectName)
                        .projectPath(projectPath)
                        .build();
                this.imageBoundingBoxService.parseData(imageProperties);
            }

            case 1 -> {
                ImageProperties imageProperties = ImageProperties.builder()
                        .projectName(projectName)
                        .projectPath(projectPath)
                        .build();
                this.imageSegmentationService.parseData(imageProperties);
            }

            case 2 -> {
                AudioProperties audioProperties = AudioProperties.builder()
                        .projectName(projectName)
                        .projectPath(projectPath)
                        .build();
                this.audioService.parseData(audioProperties);
            }
        }
    }

    private Future<Void> deleteAnnotationProject(ProjectDTO projectDTO) {
        Integer projectType = projectDTO.getAnnotationType();
        Promise<Void> promise = Promise.promise();

        switch(projectType) {
            case 0 -> {
                 imageBoundingBoxService.deleteProjectById(projectDTO.getProjectName())
                         .onComplete(res -> {
                             if (res.succeeded()) {
                                 promise.complete(res.result());
                             }

                             else if (res.failed()) {
                                 promise.fail(res.cause());
                             }
                         });
            }

            case 1 -> {
                imageSegmentationService.deleteProjectById(projectDTO.getProjectName())
                        .onComplete(res -> {
                            if (res.succeeded()) {
                                promise.complete(res.result());
                            }

                            else if (res.failed()) {
                                promise.fail(res.cause());
                            }
                        });
            }

            case 2 -> {
                audioService.deleteProjectById(projectDTO.getProjectName())
                        .onComplete(res -> {
                            if (res.succeeded()) {
                                promise.complete(res.result());
                            }

                            else if (res.failed()) {
                                promise.fail(res.cause());
                            }
                        });
            }
        }

        return promise.future();
    }
 }
