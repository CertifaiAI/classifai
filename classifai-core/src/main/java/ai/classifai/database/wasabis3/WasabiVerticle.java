package ai.classifai.database.wasabis3;

import ai.classifai.data.type.image.ImageFileType;
import ai.classifai.database.DbConfig;
import ai.classifai.database.VerticleServiceable;
import ai.classifai.database.versioning.ProjectVersion;
import ai.classifai.loader.LoaderStatus;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.selector.window.FileSystemStatus;
import ai.classifai.util.CloudParamConfig;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.PasswordHash;
import ai.classifai.util.collection.UuidGenerator;
import ai.classifai.util.data.FileHandler;
import ai.classifai.util.data.ImageHandler;
import ai.classifai.util.message.ErrorCodes;
import ai.classifai.util.message.ReplyHandler;
import ai.classifai.util.project.ProjectHandler;
import ai.classifai.util.project.ProjectInfra;
import ai.classifai.util.type.database.H2;
import ai.classifai.util.type.database.RelationalDb;
import ai.classifai.wasabis3.WasabiProject;
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
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;

import java.util.ArrayList;
import java.util.List;

/**
 * Wasabi S3 Verticle
 *
 * @author codenamewei
 */
@Slf4j
public class WasabiVerticle extends AbstractVerticle implements VerticleServiceable
{
    @Setter private static JDBCPool wasabiTablePool;

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

        if (action.equals(WasabiQuery.getWriteCredential()))
        {
            this.writeWasabiCredential(message);
        }
    }

    private static void writeWasabiCredential(Message<JsonObject> message)
    {
        JsonObject request = message.body();

        String projectName = request.getString(ParamConfig.getProjectNameParam());
        Integer annotationInt = request.getInteger(ParamConfig.getAnnotationTypeParam());

        if (ProjectHandler.isProjectNameUnique(projectName, annotationInt))
        {
            log.info("Creating Wasabi S3 project with name: " + projectName);

            WasabiProject wasabiProject = new WasabiProject(request);

            ProjectLoader loader = ProjectLoader.builder()
                    .projectId(UuidGenerator.generateUuid())
                    .projectName(projectName)
                    .annotationType(annotationInt)
                    .projectPath("")
                    .loaderStatus(LoaderStatus.LOADED)
                    .isProjectStarred(Boolean.FALSE)
                    .isProjectNew(Boolean.TRUE)
                    .projectInfra(ProjectInfra.WASABI_S3)
                    .wasabiProject(wasabiProject)
                    .projectVersion(new ProjectVersion())
                    .build();

            Tuple wasabiTuple = buildWasabiTuple(request, loader.getProjectId());

            wasabiTablePool.preparedQuery(WasabiQuery.getWriteCredential())
                    .execute(wasabiTuple)
                    .onComplete(fetch -> {
                        if(fetch.succeeded())
                        {
                            message.replyAndRequest(ReplyHandler.getOkReply());

                            log.info("Save credential in wasabi table success");

                            ProjectHandler.loadProjectLoader(loader);

                            //only save project to portfolio after get all the data
                            saveObjectsInBucket(loader);

                        }
                        else
                        {
                            String errorMessage = "Create credential in wasabi table failed";
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

    private static void saveObjectsInBucket(@NonNull ProjectLoader loader)
    {
        loader.setFileSystemStatus(FileSystemStatus.WINDOW_CLOSE_ITERATING_FOLDER);

        WasabiProject project = loader.getWasabiProject();

        ListObjectsV2Request req = ListObjectsV2Request.builder()
                .bucket(project.getWasabiBucket())
                .build();

        ListObjectsV2Iterable response = project.getWasabiS3Client().listObjectsV2Paginator(req);

        List<Object> dataPaths = new ArrayList<>();

        for (ListObjectsV2Response page : response)
        {
            page.contents().forEach((S3Object object) ->
            {
                String inputObject = object.key();

                if(FileHandler.isFileSupported(inputObject, ImageFileType.getImageFileTypes()))
                {
                    System.out.println("Path: " + inputObject);
                    dataPaths.add(inputObject);
                }
            });
        }

        ImageHandler.saveToProjectTable(loader, dataPaths);
    }

    public static Tuple buildWasabiTuple(@NonNull JsonObject input, @NonNull String projectId)
    {
        PasswordHash passwordHash = new PasswordHash();

        String accessKey = input.getString(CloudParamConfig.getAccessKeyParam());
        String hashedAccessKey = passwordHash.encrypt(accessKey);

        String secretAccessKey = input.getString(CloudParamConfig.getSecretAccessKeyParam());
        String hashedSecretAccessKey = passwordHash.encrypt(secretAccessKey);

        return Tuple.of(input.getString(CloudParamConfig.getCloudIdParam()),         //cloud_id
                projectId,                                                   //project_id
                hashedAccessKey,                                             //access_key
                hashedSecretAccessKey,                                       //secret_access_key
                input.getString(CloudParamConfig.getBucketParam()));         //bucket

    }

    private JDBCPool createJDBCPool(Vertx vertx, RelationalDb db)
    {
        return JDBCPool.pool(vertx, new JsonObject()
                .put("url", db.getUrlHeader() + DbConfig.getTableAbsPathDict().get(DbConfig.getWasabiKey()))
                .put("driver_class", db.getDriver())
                .put("user", db.getUser())
                .put("password", db.getPassword())
                .put("max_pool_size", 30));
    }


    @Override
    public void stop(Promise<Void> promise)
    {
        wasabiTablePool.close();

        log.info("Wasabi Verticle stopping...");
    }

    //obtain a JDBC pool connection,
    //Performs a SQL query to create the Wasabi table unless existed
    @Override
    public void start(Promise<Void> promise)
    {
        H2 h2 = DbConfig.getH2();

        wasabiTablePool = createJDBCPool(vertx, h2);

        wasabiTablePool.getConnection(ar -> {

            if (ar.succeeded()) {
                wasabiTablePool.query(WasabiQuery.getCreateTable())
                        .execute()
                        .onComplete(create -> {
                            if (create.succeeded())
                            {
                                log.debug("Create Wasabi table success");

                                //the consumer methods registers an event bus destination handler
                                vertx.eventBus().consumer(WasabiQuery.getQueue(), this::onMessage);

                                promise.complete();
                            }
                            else
                            {
                                log.error("Wasabi table preparation error", create.cause());
                                promise.fail(create.cause());
                            }
                        });
            }
            else
            {
                log.error("Could not open a Wasabi table connection", ar.cause());
                promise.fail(ar.cause());
            }
        });
    }
}
