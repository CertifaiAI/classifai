package ai.classifai.router;

import ai.classifai.util.ParamConfig;
import ai.classifai.util.type.AnnotationHandler;
import ai.classifai.util.type.AnnotationType;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.UUID;

public class ParamHandler {
    public ParamHandler() {}

    public String getNewProjectName(RoutingContext context)
    {
        return context.request().getParam(ParamConfig.getNewProjectNameParam());
    }

    public UUID getDataId(RoutingContext context)
    {
        return UUID.fromString(context.request().getParam(ParamConfig.getUuidParam()));
    }

    public AnnotationType getAnnotationType(RoutingContext context)
    {
        return AnnotationHandler.getTypeFromEndpoint(context.request()
                .getParam(ParamConfig.getAnnotationTypeParam()));
    }

    public String getProjectName(RoutingContext context)
    {
        return context.request().getParam(ParamConfig.getProjectNameParam());
    }
}
