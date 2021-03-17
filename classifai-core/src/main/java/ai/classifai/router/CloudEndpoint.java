package ai.classifai.router;

import ai.classifai.database.s3.S3Query;
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
     * PUT http://localhost:{port}/v2/:annotation_type/s3/newproject/:project_name
     *
     * response body
     * {
     *      cloud_id id / email: codenamewei/codenamewei@yahoo.com
     *      access_key         : DFRTDROTOX8Q30ZSHV15
     *      secret_access_key  : 2C7EYgzkfDWdtNCe12B5E7iQeQuCCEYEg9v62zqB
     *      bucket_list        : ["bucket 1, bucket 2"]
     * }
     *
     * @param context
     */
    public void createS3CloudProject(RoutingContext context)
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

                DeliveryOptions createS3Ops = new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), S3Query.getCreateS3Project());

                vertx.eventBus().request(S3Query.getQueue(), requestJsonObject, createS3Ops, fetch ->
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