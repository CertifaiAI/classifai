package ai.classifai.database.portfolio;

import ai.classifai.util.ParamConfig;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

public class PortfolioDB {
    private final EventBus eventBus;
    private final String queue;

    public PortfolioDB(EventBus eventBus) {
        this.eventBus = eventBus;
        queue = PortfolioDbQuery.getQueue();
    }

    public Future<JsonObject> renameProject(String projectId, String newProjectName) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put(ParamConfig.getProjectIdParam(), projectId);
        jsonObject.put(ParamConfig.getNewProjectNameParam(), newProjectName);

        return runQuery(jsonObject, PortfolioDbQuery.getRenameProject());
    }

    private Future<JsonObject> runQuery(JsonObject msg, String action) {
        DeliveryOptions options = new DeliveryOptions()
                .addHeader(ParamConfig.getActionKeyword(), action);

        final Promise<JsonObject> promise = Promise.promise();
        eventBus.request(this.queue, msg, options, fetch -> {
            JsonObject response = (JsonObject) fetch.result().body();
            promise.complete(response);
        });
        return promise.future();
    }

}
