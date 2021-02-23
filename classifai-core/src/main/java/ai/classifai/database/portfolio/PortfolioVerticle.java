/*
 * Copyright (c) 2020-2021 CertifAI Sdn. Bhd.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package ai.classifai.database.portfolio;

import ai.classifai.action.ProjectExport;
import ai.classifai.action.parser.ParserHelper;
import ai.classifai.database.DbConfig;
import ai.classifai.database.VerticleServiceable;
import ai.classifai.database.annotation.AnnotationQuery;
import ai.classifai.database.annotation.bndbox.BoundingBoxVerticle;
import ai.classifai.database.annotation.seg.SegVerticle;
import ai.classifai.loader.CLIProjectInitiator;
import ai.classifai.loader.LoaderStatus;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.selector.filesystem.FileSystemStatus;
import ai.classifai.util.DateTime;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.ProjectHandler;
import ai.classifai.util.collection.ConversionHandler;
import ai.classifai.util.collection.UUIDGenerator;
import ai.classifai.util.data.ImageHandler;
import ai.classifai.util.message.ErrorCodes;
import ai.classifai.util.message.ReplyHandler;
import ai.classifai.util.type.AnnotationHandler;
import ai.classifai.util.type.AnnotationType;
import ai.classifai.util.type.database.H2;
import ai.classifai.util.type.database.RelationalDb;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * General database processing to get high level infos of each created project
 *
 * @author codenamewei
 */
@Slf4j
public class PortfolioVerticle extends AbstractVerticle implements VerticleServiceable, PortfolioServiceable
{
    @Setter private static JDBCPool portfolioDbPool;

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

        if (action.equals(PortfolioDbQuery.getCreateNewProject()))
        {
            this.createV1NewProject(message);
        }
        else if (action.equals(PortfolioDbQuery.getRetrieveAllProjectsForAnnotationType()))
        {
            this.getAllProjectsForAnnotationType(message);
        }
        else if (action.equals(PortfolioDbQuery.getUpdateLabelList()))
        {
            this.updateLabelList(message);
        }
        else if (action.equals(PortfolioDbQuery.getDeleteProject()))
        {
            this.deleteProject(message);
        }
        //v2
        else if (action.equals(PortfolioDbQuery.getRetrieveProjectMetadata()))
        {
            this.getProjectMetadata(message);
        }
        else if (action.equals(PortfolioDbQuery.getRetrieveAllProjectsMetadata()))
        {
            this.getAllProjectsMetadata(message);
        }
        else if (action.equals(PortfolioDbQuery.getStarProject()))
        {
            this.starProject(message);
        }
        else if(action.equals(PortfolioDbQuery.getReloadProject()))
        {
            this.reloadProject(message);
        }
        else if(action.equals(PortfolioDbQuery.getExportProject()))
        {
            this.exportProject(message);
        }
        else
        {
            log.error("Portfolio query error. Action did not have an assigned function for handling.");
        }
    }

    public static void createV2NewProject(@NonNull String projectName, @NonNull Integer annotationType, @NonNull File rootPath)
    {
        if (ProjectHandler.isProjectNameUnique(projectName, annotationType)) {
            String annotationName = AnnotationHandler.getType(annotationType).name();

            log.info("Create " + annotationName + " project with name: " + projectName);

            String projectID = UUIDGenerator.generateUUID();

            Tuple params = PortfolioVerticle.buildNewProject(projectName, annotationType, projectID, rootPath.getAbsolutePath());


            portfolioDbPool.preparedQuery(PortfolioDbQuery.getCreateNewProject())
                    .execute(params)
                    .onComplete(fetch -> {

                    if (fetch.succeeded())
                    {
                        ProjectLoader loader = new ProjectLoader.Builder()
                                .projectID(projectID)
                                .projectName(projectName)
                                .annotationType(annotationType)
                                .projectPath(rootPath.toString())
                                .loaderStatus(LoaderStatus.LOADED)
                                .isProjectNewlyCreated(Boolean.TRUE)
                                .build();

                        ProjectHandler.loadProjectLoader(loader);

                        loader.setFileSystemStatus(FileSystemStatus.WINDOW_CLOSE_LOADING_FILES);

                        ImageHandler.processFolder(projectID, rootPath);

                        String exportPath = ParserHelper.getProjectExportPath(projectID);
                        if(ProjectExport.exportToFile(new File(exportPath), loader))
                        {
                            log.debug("Create initial project file success");
                        }
                    }
                    else
                    {
                        log.debug("Create project failed from database");
                    }
            });
        }
    }

    //v1 create new project
    public void createV1NewProject(Message<JsonObject> message)
    {
        JsonObject request = message.body();

        String projectName = request.getString(ParamConfig.getProjectNameParam());
        Integer annotationType = request.getInteger(ParamConfig.getAnnotationTypeParam());

        if (ProjectHandler.isProjectNameUnique(projectName, annotationType))
        {
            String annotationName = AnnotationHandler.getType(annotationType).name();

            log.info("Create " + annotationName + " project with name: " + projectName);

            String projectID = UUIDGenerator.generateUUID();

            Tuple params = PortfolioVerticle.buildNewProject(projectName, annotationType, projectID, "");

            portfolioDbPool.preparedQuery(PortfolioDbQuery.getCreateNewProject())
                    .execute(params)
                    .onComplete(fetch -> {

                        if (fetch.succeeded())
                        {
                            ProjectLoader loader = new ProjectLoader.Builder()
                            .projectID(projectID)
                            .projectName(projectName)
                            .annotationType(annotationType)
                            .projectPath("")
                            .loaderStatus(LoaderStatus.LOADED)
                            .isProjectNewlyCreated(Boolean.TRUE)
                            .build();

                            ProjectHandler.loadProjectLoader(loader);
                            message.replyAndRequest(ReplyHandler.getOkReply());
                        }
                        else
                        {
                            //query database failed
                            message.replyAndRequest(ReplyHandler.reportDatabaseQueryError(fetch.cause()));
                        }
                    });
        }
        else
        {
            message.replyAndRequest(ReplyHandler.reportUserDefinedError("Project name exist. Please choose another one."));
        }
    }

    public void updateLabelList(Message<JsonObject> message)
    {
        String projectID = message.body().getString(ParamConfig.getProjectIdParam());
        JsonArray labelList = message.body().getJsonArray(ParamConfig.getLabelListParam());

        Tuple params = Tuple.of(labelList.toString(), projectID);

        portfolioDbPool.preparedQuery(PortfolioDbQuery.getUpdateLabelList())
                .execute(params)
                .onComplete(fetch ->{

                    if (fetch.succeeded())
                    {
                        //update Project loader too
                        List<String> labelListArray = ConversionHandler.jsonArray2StringList(labelList);

                        ProjectLoader loader = ProjectHandler.getProjectLoader(projectID);

                        loader.setLabelList(labelListArray);

                        message.replyAndRequest(ReplyHandler.getOkReply());
                    }
                    else
                    {
                        message.replyAndRequest(ReplyHandler.reportDatabaseQueryError(fetch.cause()));
                    }
                });
    }


    public void exportProject(Message<JsonObject> message)
    {
        String projectID = message.body().getString(ParamConfig.getProjectIdParam());
        Integer annotationType = message.body().getInteger(ParamConfig.getAnnotationTypeParam());

        Tuple params = Tuple.of(projectID);

        portfolioDbPool.preparedQuery(PortfolioDbQuery.getExportProject())
                .execute(params)
                .onComplete(fetch ->{

                    if (fetch.succeeded())
                    {
                        RowSet<Row> rowSet = fetch.result();

                        JsonArray portfolioJsonArray = rowSet.iterator().next().getJsonArray(0);
                        //export
                        String exportPath = ParserHelper.getProjectExportPath(projectID);

                        JDBCPool client = (AnnotationType.BOUNDINGBOX.ordinal() == annotationType) ? BoundingBoxVerticle.getJdbcPool() : SegVerticle.getJdbcPool();

                        client.preparedQuery(AnnotationQuery.getExportProject())
                                .execute(params)
                                .onComplete(annotationFetch ->{

                                if (annotationFetch.succeeded())
                                {
                                    message.reply(ReplyHandler.getOkReply().put(ParamConfig.getProjectJsonPathParam(), exportPath));

                                    RowSet<Row> annotationRowSet = annotationFetch.result();

                                    Row annotationRow = annotationRowSet.iterator().next();

                                    JsonArray annotationJsonArray = annotationRow.getJsonArray(0);

                                    ProjectExport.exportToFile(new File(exportPath), portfolioJsonArray, annotationJsonArray);
                                }
                        });

                    }
                    else
                    {
                        message.replyAndRequest(ReplyHandler.reportDatabaseQueryError(fetch.cause()));
                    }
                });

    }

    public void deleteProject(Message<JsonObject> message)
    {
        String projectID = message.body().getString(ParamConfig.getProjectIdParam());

        Tuple params = Tuple.of(projectID);

        portfolioDbPool.preparedQuery(PortfolioDbQuery.getDeleteProject())
                .execute(params)
                .onComplete(fetch -> {

                    if (fetch.succeeded())
                    {
                        message.replyAndRequest(ReplyHandler.getOkReply());
                    }
                    else
                    {
                        message.replyAndRequest(ReplyHandler.reportDatabaseQueryError(fetch.cause()));
                    }
                });
    }

    public void getAllProjectsForAnnotationType(Message<JsonObject> message)
    {
        Integer annotationTypeIndex = message.body().getInteger(ParamConfig.getAnnotationTypeParam());

        Tuple params = Tuple.of(annotationTypeIndex);
        
        portfolioDbPool.preparedQuery(PortfolioDbQuery.getRetrieveAllProjectsForAnnotationType())
                .execute(params)
                .onComplete(fetch -> {

                    if (fetch.succeeded())
                    {
                        RowSet<Row> rowSet = fetch.result();
                        List<String> projectNameList = new ArrayList<>();
                        for (Row row : rowSet)
                        {
                            projectNameList.add(row.getString(0));
                        }

                        JsonObject response = ReplyHandler.getOkReply();
                        response.put(ParamConfig.getContent(), projectNameList);

                        message.replyAndRequest(response);
                    }
                    else
                    {
                        message.replyAndRequest(ReplyHandler.reportDatabaseQueryError(fetch.cause()));
                    }
                });
    }

    public static void updateFileSystemUUIDList(@NonNull String projectID)
    {
        ProjectLoader loader = ProjectHandler.getProjectLoader(projectID);

        List<String> uuidList = loader.getUuidListFromDatabase();

        Tuple jsonUpdateBody = Tuple.of(uuidList.toString(), projectID);

        portfolioDbPool.preparedQuery(PortfolioDbQuery.getUpdateProject())
                .execute(jsonUpdateBody)
                .onComplete(reply -> {
                    if (!reply.succeeded())
                    {
                        log.info("Update list of uuids to Portfolio Database failed");
                    }
                });
    }

    private void configProjectLoaderFromDb()
    {
        portfolioDbPool.query(PortfolioDbQuery.getLoadDbProject())
                .execute()
                .onComplete(projectNameFetch -> {

                    if (projectNameFetch.succeeded())
                    {
                        RowSet<Row> rowSet = projectNameFetch.result();
                        
                        if (rowSet.size() == 0)
                        {
                            log.debug("Project ID List is empty.");
                        }
                        else
                        {
                            for (Row row : rowSet)
                            {
                                ProjectLoader loader = new ProjectLoader.Builder()
                                    .projectID(row.getString(0))
                                    .projectName(row.getString(1))
                                    .annotationType(row.getInteger(2))
                                    .projectPath(row.getString(3))
                                    .loaderStatus(LoaderStatus.DID_NOT_INITIATED)
                                    .isProjectNewlyCreated(row.getBoolean(6))
                                    .build();

                                loader.setLabelList(ConversionHandler.string2StringList(row.getString(4)));
                                loader.setUuidListFromDatabase(ConversionHandler.string2StringList(row.getString(5)));

                                ProjectHandler.loadProjectLoader(loader);
                            }
                        }
                    }
                    else
                    {
                        log.info("Retrieving from portfolio database to project loader failed");
                    }
                });
    }

    public void buildProjectFromCLI()
    {
        //from cli argument
        CLIProjectInitiator initiator = ProjectHandler.getCliProjectInitiator();

        if (initiator == null) return;

        String projectName = initiator.getProjectName();
        Integer annotationInt = initiator.getProjectType().ordinal();
        File dataPath = initiator.getRootDataPath();

        if (ProjectHandler.isProjectNameUnique(projectName, annotationInt))
        {
            log.info("Create project (from cli) with name: " + projectName + " in " + AnnotationHandler.getType(annotationInt).name() + " project.");

            String projectID = UUIDGenerator.generateUUID();
            Tuple params = PortfolioVerticle.buildNewProject(projectName, annotationInt, projectID, dataPath.getAbsolutePath());

            portfolioDbPool.preparedQuery(PortfolioDbQuery.getCreateNewProject())
                    .execute(params)
                    .onComplete(fetch -> {

                        if (fetch.succeeded())
                        {
                            String rootProjectPath = dataPath == null ? "" : dataPath.getAbsolutePath();

                            ProjectLoader loader = new ProjectLoader.Builder()
                                      .projectID(projectID)
                                      .projectName(projectName)
                                      .annotationType(annotationInt)
                                      .projectPath(rootProjectPath)
                                      .loaderStatus(LoaderStatus.LOADED)
                                      .isProjectNewlyCreated(Boolean.TRUE)
                                      .build();

                            if(dataPath != null)
                            {
                                loader.setFileSystemStatus(FileSystemStatus.WINDOW_CLOSE_LOADING_FILES);
                                ImageHandler.processFolder(projectID, dataPath);
                            }
                        }
                        else
                        {
                            log.info("Create project failed. Classifai expect not to work fine in docker mode");
                        }
                    });
        }
        else
        {
            String projectID = ProjectHandler.getProjectID(projectName, annotationInt);

            if (dataPath != null) ImageHandler.processFolder(projectID, dataPath);
        }
    }

    //V2 API
    public void getProjectMetadata(Message<JsonObject> message)
    {
        String projectID = message.body().getString(ParamConfig.getProjectIdParam());

        Tuple params = Tuple.of(projectID);

        portfolioDbPool.preparedQuery(PortfolioDbQuery.getRetrieveProjectMetadata())
                .execute(params)
                .onComplete(fetch -> {

                    if (fetch.succeeded())
                    {
                        List<JsonObject> result = new ArrayList<>();

                        RowSet<Row> rowSet = fetch.result();

                        for (Row row : rowSet)
                        {
                            String projectName = row.getString(0);
                            String projectPath = row.getString(1);
                            List<String> uuidList = ConversionHandler.string2StringList(row.getString(2));
                            Boolean isNew = row.getBoolean(3);
                            Boolean isStarred = row.getBoolean(4);
                            Boolean isLoaded = ProjectHandler.getProjectLoader(projectID).getIsLoadedFrontEndToggle();
                            String dataTime = row.getString(5);

                            //project_name, uuid_list, is_new, is_starred, is_loaded, created_date
                            result.add(new JsonObject()
                                    .put(ParamConfig.getProjectNameParam(), projectName)
                                    .put(ParamConfig.getProjectPathParam(), projectPath)
                                    .put(ParamConfig.getIsNewParam(), isNew)
                                    .put(ParamConfig.getIsStarredParam(), isStarred)
                                    .put(ParamConfig.getIsLoadedParam(), isLoaded)
                                    .put(ParamConfig.getCreatedDateParam(), dataTime)
                                    .put(ParamConfig.getTotalUuidParam(), uuidList.size()));

                            JsonObject response = ReplyHandler.getOkReply();
                            response.put(ParamConfig.getContent(), result);

                            message.replyAndRequest(response);
                        }
                    }
                    else
                    {
                        message.replyAndRequest(ReplyHandler.reportDatabaseQueryError(fetch.cause()));
                    }
                });
    }

    //V2 API
    public void getAllProjectsMetadata(Message<JsonObject> message)
    {
        Integer annotationTypeIndex = message.body().getInteger(ParamConfig.getAnnotationTypeParam());

        Tuple params = Tuple.of(annotationTypeIndex);

        portfolioDbPool.preparedQuery(PortfolioDbQuery.getRetrieveAllProjectsMetadata())
                .execute(params)
                .onComplete(fetch -> {

                    if (fetch.succeeded())
                    {
                        List<JsonObject> result = new ArrayList<>();

                        RowSet<Row> rowSet = fetch.result();

                        for (Row row : rowSet)
                        {
                            String projectName = row.getString(0);
                            String projectPath = row.getString(1);
                            String uuidList = row.getString(2);

                            int totalUuid = ConversionHandler.string2StringList(uuidList).size();

                            String projectUuid = ProjectHandler.getProjectID(projectName, annotationTypeIndex);
                            Boolean isLoaded = ProjectHandler.getProjectLoader(projectUuid).getIsLoadedFrontEndToggle();

                            result.add(new JsonObject()
                                    .put(ParamConfig.getProjectNameParam(), projectName)
                                    .put(ParamConfig.getProjectPathParam(), projectPath)
                                    .put(ParamConfig.getIsNewParam(), row.getBoolean(3))
                                    .put(ParamConfig.getIsStarredParam(), row.getBoolean(4))
                                    .put(ParamConfig.getIsLoadedParam(), isLoaded)
                                    .put(ParamConfig.getCreatedDateParam(), row.getString(5))
                                    .put(ParamConfig.getTotalUuidParam(), totalUuid));
                        }

                        JsonObject response = ReplyHandler.getOkReply();
                        response.put(ParamConfig.getContent(), result);

                        message.replyAndRequest(response);
                    }
                    else
                    {
                        message.replyAndRequest(ReplyHandler.reportDatabaseQueryError(fetch.cause()));
                    }
                });
    }

    public static void updateIsNewParam(@NonNull String projectID)
    {
        Tuple params = Tuple.of(Boolean.FALSE, projectID);

        portfolioDbPool.preparedQuery(PortfolioDbQuery.getUpdateIsNewParam())
                .execute(params)
                .onComplete(fetch ->{

                    if (fetch.succeeded())
                    {
                        ProjectHandler.getProjectLoader(projectID).setIsProjectNewlyCreated(Boolean.FALSE);
                    }
                    else
                    {
                        log.info("Update is_new param for project of projectid: " + projectID + " failed");
                    }
                });
    }

    //V2 API
    public void starProject(Message<JsonObject> message)
    {
        String projectID = message.body().getString(ParamConfig.getProjectIdParam());
        Object isStarObject = message.body().getString(ParamConfig.getStatusParam());

        boolean isStarStatus;

        try
        {
            if (isStarObject instanceof String)
            {
                String isStarStr = (String) isStarObject;

                isStarStatus = ConversionHandler.String2boolean(isStarStr);
            }
            else
            {
                throw new Exception("Status != String. Could not convert to boolean for starring.");
            }
        }
        catch (Exception e)
        {
            message.replyAndRequest(ReplyHandler.reportBadParamError("Starring object value is not boolean. Failed to execute"));
            return;
        }

        Tuple params = Tuple.of(isStarStatus,projectID);

        portfolioDbPool.preparedQuery(PortfolioDbQuery.getStarProject())
                .execute(params)
                .onComplete(fetch ->{

                    if (fetch.succeeded())
                    {
                        message.replyAndRequest(ReplyHandler.getOkReply());
                    }
                    else
                    {
                        message.replyAndRequest(ReplyHandler.reportDatabaseQueryError(fetch.cause()));
                    }
                });
    }

    private static Tuple buildNewProject(String projectName, Integer annotationType, String projectID, String projectPath)
    {
        return Tuple.of(projectID,                   //project_id
                        projectName,           //project_name
                        annotationType,              //annotation_type
                        projectPath,                 //project_path
                        ParamConfig.getEmptyArray(), //label_list
                        ParamConfig.getEmptyArray(), //uuid_list
                        true,                        //is_new
                        false,                       //is_starred
                        DateTime.get());             //created_date
    }

    private JDBCPool createJDBCPool(Vertx vertx, RelationalDb db)
    {
        return JDBCPool.pool(vertx, new JsonObject()
                .put("url", db.getUrlHeader() + DbConfig.getTableAbsPathDict().get(DbConfig.getPortfolioKey()))
                .put("driver_class", db.getDriver())
                .put("user", db.getUser())
                .put("password", db.getPassword())
                .put("max_pool_size", 30));
    }
    
    public void reloadProject(Message<JsonObject> message)
    {
        message.reply(ReplyHandler.getOkReply());

        String projectID = message.body().getString(ParamConfig.getProjectIdParam());

        ImageHandler.refreshProjectRootPath(projectID);
    }

    @Override
    public void stop(Promise<Void> promise)
    {
        portfolioDbPool.close();

        log.info("Portfolio Verticle stopping...");
    }

    //obtain a JDBC pool connection,
    //Performs a SQL query to create the portfolio table unless existed
    @Override
    public void start(Promise<Void> promise)
    {
        H2 h2 = DbConfig.getH2();

        setPortfolioDbPool(createJDBCPool(vertx, h2));

        portfolioDbPool.getConnection(ar -> {

                if (ar.succeeded()) {
                     portfolioDbPool.query(PortfolioDbQuery.getCreatePortfolioTable())
                            .execute()
                            .onComplete(create -> {
                                if (create.succeeded()) {
                                    //the consumer methods registers an event bus destination handler
                                    vertx.eventBus().consumer(PortfolioDbQuery.getQueue(), this::onMessage);

                                    configProjectLoaderFromDb();

                                    promise.complete();
                                }
                                else
                                {
                                    log.error("Portfolio database preparation error", create.cause());
                                    promise.fail(create.cause());
                                }
                            });
                }
                else
                {
                    log.error("Could not open a portfolio database connection", ar.cause());
                    promise.fail(ar.cause());
                }
        });
    }
}
