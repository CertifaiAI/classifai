package ai.classifai.core;

import ai.classifai.backend.database.portfolio.PortfolioDB;
import ai.classifai.core.entities.properties.ThumbnailProperties;
import ai.classifai.core.entities.response.ImageSourceResponse;
import ai.classifai.core.util.message.ReplyHandler;
import ai.classifai.core.util.project.ProjectHandler;
import io.vertx.core.Future;

public class ImageDataService {
    private final PortfolioDB portfolioDB;
    private final ProjectHandler projectHandler;

    public ImageDataService(PortfolioDB portfolioDB, ProjectHandler projectHandler) {
        this.portfolioDB = portfolioDB;
        this.projectHandler = projectHandler;
    }

    public String getProjectId(String projectName, int projectType) {
        return projectHandler.getProjectId(projectName, projectType);
    }

    public Future<ThumbnailProperties> getThumbnail(String projectID, String uuid) {
        return portfolioDB.getThumbnail(projectID, uuid);
    }

    public Future<ImageSourceResponse> getImageSource(String projectID, String uuid, String projectName) {
        return portfolioDB.getImageSource(projectID, uuid, projectName)
                .map(result -> ImageSourceResponse.builder()
                        .message(ReplyHandler.SUCCESSFUL)
                        .imgSrc(result)
                        .build())
                .otherwise(ImageSourceResponse.builder()
                        .message(ReplyHandler.FAILED)
                        .errorMessage("Fail getting image source")
                        .build());
    }

}
