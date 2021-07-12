package ai.classifai.router;

import ai.classifai.util.ParamConfig;
import ai.classifai.util.type.AnnotationHandler;
import ai.classifai.util.type.AnnotationType;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class ParamHandler extends EndpointVariableHandler
{
    public ParamHandler() {}

    public String getNewProjectName(RoutingContext context)
    {
        return context.request().getParam(ParamConfig.getNewProjectNameParam());
    }

    public String getUuid(RoutingContext context)
    {
        return context.request().getParam(ParamConfig.getUuidParam());
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

    public JsonObject annoParamToJson(RoutingContext context)
    {
        AnnotationType type = getAnnotationType(context);

        return new JsonObject()
                .put(ParamConfig.getAnnotationTypeParam(), type.ordinal());
    }

    public JsonObject projectParamToJson(RoutingContext context)
    {
        String projectName = getProjectName(context);

        return new JsonObject()
                .put(ParamConfig.getProjectNameParam(), projectName)
                .mergeIn(annoParamToJson(context));
    }

    public JsonObject dataParamToJson(RoutingContext context)
    {
        String uuid = getUuid(context);

        return new JsonObject()
                .put(ParamConfig.getUuidParam(), uuid)
                .mergeIn(projectParamToJson(context));
    }

    public JsonObject renameParamToJson(RoutingContext context)
    {
        String newProjectName = getNewProjectName(context);

        return new JsonObject()
                .put(ParamConfig.getNewProjectNameParam(), newProjectName)
                .mergeIn(projectParamToJson(context));
    }
}
