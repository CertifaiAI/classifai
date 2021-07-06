package ai.classifai.router;

import ai.classifai.database.wasabis3.WasabiQuery;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.http.HTTPResponseHandler;
import ai.classifai.util.message.ReplyHandler;
import ai.classifai.util.type.AnnotationHandler;
import ai.classifai.util.type.AnnotationType;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class CloudEndpoint
{
    @Setter private Vertx vertx = null;

    /**
     * PUT http://localhost:{port}/v2/:annotation_type/wasabi/newproject/:project_name
     *
     * response body
     * {
     *      cloud_id id / email: johndoe / johndoe@xxx.com
     *      access_key         : xxxxxxxxxxxxxxxxxxxxxxxxxx
     *      secret_access_key  : xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
     *      bucket_list        : ["bucket 1, bucket 2"]
     * }
     *
     * @param context
     */
    public void createWasabiCloudProject(RoutingContext context)
    {
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(context.request().getParam(ParamConfig.getAnnotationTypeParam()));

        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());

        context.request().bodyHandler(requestBody ->
        {
            try
            {
                JsonObject requestJsonObject = requestBody.toJsonObject();

                requestJsonObject
                        .put(ParamConfig.getProjectNameParam(), projectName)
                        .put(ParamConfig.getAnnotationTypeParam(), type.ordinal());

                DeliveryOptions createS3Ops = new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), WasabiQuery.getWriteCredential());

                vertx.eventBus().request(WasabiQuery.getQueue(), requestJsonObject, createS3Ops, fetch ->
                {
                    JsonObject response = (JsonObject) fetch.result().body();

                    if (ReplyHandler.isReplyOk(response))
                    {
                        HTTPResponseHandler.configureOK(context);

                    }
                    else
                    {
                        HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Failed to load project " + projectName));
                    }
                });

            }
            catch (Exception e)
            {
                HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError(e.getMessage()));

            }
        });


    }
}
