package ai.classifai.database.s3;

import ai.classifai.database.DbConfig;
import ai.classifai.database.VerticleServiceable;
import ai.classifai.database.portfolio.PortfolioDbQuery;
import ai.classifai.database.portfolio.PortfolioVerticle;
import ai.classifai.database.versioning.ProjectVersion;
import ai.classifai.loader.LoaderStatus;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.util.CloudParamConfig;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.ProjectHandler;
import ai.classifai.util.collection.UuidGenerator;
import ai.classifai.util.message.ErrorCodes;
import ai.classifai.util.message.ReplyHandler;
import ai.classifai.util.type.AnnotationHandler;
import ai.classifai.util.type.database.H2;
import ai.classifai.util.type.database.RelationalDb;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Tuple;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * S3 Ops
 *
 * @author codenamewei
 */
@Slf4j
public class S3Verticle extends AbstractVerticle implements VerticleServiceable
{
    @Setter
    private static JDBCPool s3TablePool;

    @Override
    public void onMessage(Message<JsonObject> message)
    {
        if (!message.headers().contains(ParamConfig.getActionKeyword()))
        {
            log.error("No action header specified for message with headers {} and body {}",
                    message.headers(), message.body().encodePrettily());

            message.fail(ErrorCodes.NO_ACTION_SPECIFIED.ordinal(), "No keyword " + ParamConfig.getActionKeyword() + " specified");

            return;
        }

        String action = message.headers().get(ParamConfig.getActionKeyword());

        if (action.equals(S3Query.getCreateS3Project()))
        {
            this.createS3Project(message);
        }
    }

    private static void createS3Project(Message<JsonObject> message)
    {
        JsonObject request = message.body();

        String projectName = request.getString(ParamConfig.getProjectNameParam());
        Integer annotationInt = request.getInteger(ParamConfig.getAnnotationTypeParam());

        if (ProjectHandler.isProjectNameUnique(projectName, annotationInt))
        {
            log.info("Create s3 project with name: " + projectName);

            ProjectVersion project = new ProjectVersion();

            ProjectLoader loader = ProjectLoader.builder()
                    .projectId(UuidGenerator.generateUuid())
                    .projectName(projectName)
                    .annotationType(annotationInt)
                    .projectPath("")
                    .loaderStatus(LoaderStatus.LOADED)
                    .isProjectStarred(Boolean.FALSE)
                    .isProjectNew(Boolean.TRUE)
                    .isCloud(Boolean.TRUE)
                    .projectVersion(project)
                    .build();



            ProjectHandler.loadProjectLoader(loader);

            PortfolioVerticle.createNewProject(loader.getProjectId());

            Tuple s3Tuple = buildS3Tuple(request, loader.getProjectId());

            s3TablePool.preparedQuery(S3Query.getCreateS3Project())
                    .execute(s3Tuple)
                    .onComplete(fetch -> {
                        if(fetch.succeeded())
                        {
                            log.info("Save credential in s3 success");

                            message.replyAndRequest(ReplyHandler.getOkReply());
                        }
                        else
                        {
                            String errorMessage = "Create s3 credential in table failed";
                            log.debug(errorMessage);
                            message.replyAndRequest(ReplyHandler.reportUserDefinedError(errorMessage));
                        }
                    });

        }
        else
        {
            message.replyAndRequest(ReplyHandler.reportUserDefinedError("Project name exist. Please choose another one."));
        }
    }

    private static Tuple buildS3Tuple(@NonNull JsonObject input, @NonNull String projectId)
    {
        return Tuple.of(input.getString(CloudParamConfig.getCloudIdParam()),         //cloud_id
                        projectId,                                                   //project_id
                        input.getString(CloudParamConfig.getAccessKeyParam()),       //access_key
                        input.getString(CloudParamConfig.getSecretAccessKeyParam()),//secret_access_key
                        input.getJsonArray(CloudParamConfig.getBucketListParam()));  //bucket_list

    }

    private JDBCPool createJDBCPool(Vertx vertx, RelationalDb db)
    {
        return JDBCPool.pool(vertx, new JsonObject()
                .put("url", db.getUrlHeader() + DbConfig.getTableAbsPathDict().get(DbConfig.getS3Key()))
                .put("driver_class", db.getDriver())
                .put("user", db.getUser())
                .put("password", db.getPassword())
                .put("max_pool_size", 30));
    }


    @Override
    public void stop(Promise<Void> promise)
    {
        s3TablePool.close();

        log.info("Portfolio Verticle stopping...");
    }

    //obtain a JDBC pool connection,
    //Performs a SQL query to create the portfolio table unless existed
    @Override
    public void start(Promise<Void> promise)
    {
        H2 h2 = DbConfig.getH2();

        s3TablePool = createJDBCPool(vertx, h2);

        s3TablePool.getConnection(ar -> {

            if (ar.succeeded()) {
                s3TablePool.query(PortfolioDbQuery.getCreatePortfolioTable())
                        .execute()
                        .onComplete(create -> {
                            if (create.succeeded()) {
                                //the consumer methods registers an event bus destination handler
                                vertx.eventBus().consumer(S3Query.getQueue(), this::onMessage);

                                promise.complete();
                            }
                            else
                            {
                                log.error("S3 table preparation error", create.cause());
                                promise.fail(create.cause());
                            }
                        });
            }
            else
            {
                log.error("Could not open a s3 table connection", ar.cause());
                promise.fail(ar.cause());
            }
        });
    }
}
