package ai.classifai.router.endpoint;

import ai.classifai.database.portfolio.PortfolioDB;
import ai.classifai.dto.api.response.ProjectStatisticResponse;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.util.message.ReplyHandler;
import ai.classifai.util.project.ProjectHandler;
import ai.classifai.util.type.AnnotationType;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Slf4j
@Produces(MediaType.APPLICATION_JSON)
public class ProjectStatisticEndpoint {

    private final ProjectHandler projectHandler;
    private final PortfolioDB portfolioDB;

    public ProjectStatisticEndpoint(ProjectHandler projectHandler, PortfolioDB portfolioDB){
        this.projectHandler = projectHandler;
        this.portfolioDB = portfolioDB;
    }
    /**
     * Retrieve number of labeled Image, unlabeled Image and total number of labels per class in a project
     *
     * GET http://localhost:{port}/v2/:annotation_type/projects/:project_name/statistic
     *
     */
    @GET
    @Path("/v2/{annotation_type}/projects/{project_name}/statistic")
    public ProjectStatisticResponse getProjectStatistic (@PathParam("annotation_type") String annotationType,
                                                         @PathParam("project_name") String projectName)
    {
        AnnotationType type = AnnotationType.getTypeFromEndpoint(annotationType);

        log.info("Get project statistic : " + projectName + " of annotation type: " + type.name());

        ProjectLoader projectLoader = projectHandler.getProjectLoader(projectName, type);

        if(projectLoader == null) {
            return ProjectStatisticResponse.builder()
                    .message(ReplyHandler.FAILED)
                    .errorMessage("Unable to retrieve project statistic for project: " + projectName)
                    .build();
        }

        return portfolioDB.getProjectStatistic(projectLoader);

    }
}
