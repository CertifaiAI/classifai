package ai.classifai.database.portfolio;

import ai.classifai.action.ActionConfig;
import ai.classifai.database.annotation.AnnotationQuery;
import ai.classifai.util.ParamConfig;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PortfolioDB {
    private final EventBus eventBus;
    private final String queue;

    public PortfolioDB(EventBus eventBus) {
        this.eventBus = eventBus;
        queue = PortfolioDbQuery.getQueue();
    }

    private Future<JsonObject> runAnnotationQuery(JsonObject msg, String action, String annotationQueue) {
        DeliveryOptions options = new DeliveryOptions()
                .addHeader(ParamConfig.getActionKeyword(), action);

        log.info("DEVEN: runAnnotationQuery " + msg);

        final Promise<JsonObject> promise = Promise.promise();
        eventBus.request(annotationQueue, msg, options, fetch -> {
            JsonObject response = (JsonObject) fetch.result().body();
            log.info("DEVEN: runAnnotationQuery response " + response);
            promise.complete(response);
        });
        return promise.future();
    }

    private Future<JsonObject> runPortfolioQuery(JsonObject msg, String action) {
        DeliveryOptions options = new DeliveryOptions()
                .addHeader(ParamConfig.getActionKeyword(), action);

        log.info("DEVEN: runPortfolioQuery " + msg);

        final Promise<JsonObject> promise = Promise.promise();
        eventBus.request(queue, msg, options, fetch -> {
            JsonObject response = (JsonObject) fetch.result().body();
            log.info("DEVEN: runPortfolioQuery response " + response);
            promise.complete(response);
        });
        return promise.future();
    }

    public Future<JsonObject> renameProject(String projectId, String newProjectName) {
        JsonObject jsonObject = new JsonObject()
                .put(ParamConfig.getProjectIdParam(), projectId)
                .put(ParamConfig.getNewProjectNameParam(), newProjectName);

        return runPortfolioQuery(jsonObject, PortfolioDbQuery.getRenameProject());
    }

    public Future<JsonObject> exportProject(String projectId, int annotationType, int exportType) {
        JsonObject jsonObject = new JsonObject()
                .put(ParamConfig.getProjectIdParam(), projectId)
                .put(ParamConfig.getAnnotationTypeParam(), annotationType)
                .put(ActionConfig.getExportTypeParam(), exportType);
        
        return runPortfolioQuery(jsonObject, PortfolioDbQuery.getExportProject());
    }

    public Future<JsonObject> deleteProjectData(String projectId, String annotationQueue,
                                                JsonArray uuidListArray, JsonArray uuidImgPathList) {
        JsonObject jsonObject = new JsonObject()
                .put(ParamConfig.getProjectIdParam(), projectId)
                .put(ParamConfig.getUuidListParam(), uuidListArray)
                .put(ParamConfig.getImgPathListParam(), uuidImgPathList);
        
        return runAnnotationQuery(jsonObject, AnnotationQuery.getDeleteProjectData(), annotationQueue);
    }

    public Future<JsonObject> renameData(String projectId, String annotationQueue, String uuid, String newFilename) {
        JsonObject jsonObject = new JsonObject()
                .put(ParamConfig.getProjectIdParam(), projectId)
                .put(ParamConfig.getUuidParam(), uuid)
                .put(ParamConfig.getNewFileNameParam(), newFilename);

        return runAnnotationQuery(jsonObject, AnnotationQuery.getRenameProjectData(), annotationQueue);
    }

    public Future<JsonObject> startProject(String projectId, Boolean isStarred) {
        JsonObject jsonObject = new JsonObject()
                .put(ParamConfig.getStatusParam(), isStarred)
                .put(ParamConfig.getProjectIdParam(), projectId);
        
        return runPortfolioQuery(jsonObject, PortfolioDbQuery.getStarProject());
    }

    public Future<JsonObject> reloadProject(String projectId) {
        JsonObject jsonObject = new JsonObject()
                .put(ParamConfig.getProjectIdParam(), projectId);
        
        return runPortfolioQuery(jsonObject, PortfolioDbQuery.getReloadProject());
    }
}
