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
import ai.classifai.database.annotation.AnnotationVerticle;
import ai.classifai.database.annotation.bndbox.BoundingBoxDbQuery;
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

            JsonArray params = PortfolioVerticle.buildNewProject(projectName, annotationType, projectID, rootPath.getAbsolutePath());

            portfolioDbClient.queryWithParams(PortfolioDbQuery.getCreateNewProject(), params, fetch -> {

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

                    ProjectHandler.buildProjectLoader(loader);

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

            Tuple params = buildNewProject(projectName, annotationType, projectID);

            portfolioDbPool.preparedQuery(PortfolioDbQuery.createNewProject())
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

                            ProjectHandler.buildProjectLoader(loader);
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

        portfolioDbPool.preparedQuery(PortfolioDbQuery.updateLabelList())
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

        JsonArray params = new JsonArray().add(projectID);

        portfolioDbClient.queryWithParams(PortfolioDbQuery.getExportProject(), params, fetch ->
        {
            if (fetch.succeeded())
            {
                JsonArray portfolioJsonArray = fetch.result().getResults().get(0);
                //export
                String exportPath = ParserHelper.getProjectExportPath(projectID);

                JDBCClient client = (AnnotationType.BOUNDINGBOX.ordinal() == annotationType) ? BoundingBoxVerticle.getJdbcClient() : SegVerticle.getJdbcClient();

                client.queryWithParams(BoundingBoxDbQuery.getExportProject(), params, annotationFetch ->
                {
                    if (annotationFetch.succeeded())
                    {
                        message.reply(ReplyHandler.getOkReply().put(ParamConfig.getProjectJsonPathParam(), exportPath));

                        JsonArray annotationJsonArray = annotationFetch.result().getResults().get(0);

                        ProjectExport.exportToFile(new File(exportPath), portfolioJsonArray, annotationJsonArray);
                    }
                });

            }
            else
            {
                message.reply(ReplyHandler.reportDatabaseQueryError(fetch.cause()));
            }
        });
    }

    public void deleteProject(Message<JsonObject> message)
    {
        String projectID = message.body().getString(ParamConfig.getProjectIdParam());

        Tuple params = Tuple.of(projectID);

        portfolioDbPool.preparedQuery(PortfolioDbQuery.deleteProject())
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
        
        portfolioDbPool.preparedQuery(PortfolioDbQuery.getAllProjectsForAnnotationType())
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

        portfolioDbPool.preparedQuery(PortfolioDbQuery.updateProject())
                .execute(jsonUpdateBody)
                .onComplete(reply -> {
                    if (!reply.succeeded())
                    {
                        log.info("Update list of uuids to Portfolio Database failed");
                    }
                });
    }

    private void loadProjectLoader()
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
                            List<String> projectIDList = new ArrayList<>();
                            List<String> projectNameList = new ArrayList<>();
                            List<Integer> annotationTypeList = new ArrayList<>();
                            List<String> projectPathList = new ArrayList<>();
                            List<String> labelList = new ArrayList<>();
                            List<String> uuidsFromDatabaseList = new ArrayList<>();
                            List<Boolean> isNewList = new ArrayList<>();

                            for (Row row : rowSet)
                            {
                                projectIDList.add(row.getString(0));
                                projectNameList.add(row.getString(1));
                                annotationTypeList.add(row.getInteger(2));
                                projectPathList.add(row.getInteger(3));
                                labelList.add(row.getString(4));
                                uuidsFromDatabaseList.add(row.getString(5));
                                isNewList.add(row.getBoolean(6));
                            }

                            for (int i = 0; i < projectIDList.size(); ++i)
                            {
                                ProjectLoader loader = new ProjectLoader.Builder()
                                    .projectID(projectIDList.get(i))
                                    .projectName(projectNameList.get(i))
                                    .annotationType(annotationTypeList.get(i))
                                    .projectPath(projectPathList.get(i))
                                    .loaderStatus(LoaderStatus.DID_NOT_INITIATED)
                                    .isProjectNewlyCreated(isNewList.get(i))
                                    .build();

                                ProjectLoader loader = ProjectHandler.buildProjectLoader(loader);

                                loader.setUuidListFromDatabase(ConversionHandler.string2StringList(uuidsFromDatabaseList.get(i)));
                                loader.setLabelList(ConversionHandler.string2StringList(labelList.get(i)));
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

            String projectID = ProjectHandler.generateProjectID();
            Tuple params = buildNewProject(projectName, annotationInt, projectID);

            portfolioDbPool.preparedQuery(PortfolioDbQuery.createNewProject())
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
                                    .put(ParamConfig.getTotalUUIDParam(), uuidList.size()));

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
                            String projectPath = row.getString(1)
                            String uuidList = row.getString(2);
                            Boolean isNew = row.getBoolean(3);
                            Boolean isStarred = row.getBoolean(4);
                            String  dateTime = row.getString(5);

                            int total_uuid = ConversionHandler.string2StringList(uuidList).size();

                            String projectID = ProjectHandler.getProjectID(projectName, annotationTypeIndex);
                            Boolean isLoaded = ProjectHandler.getProjectLoader(projectID).getIsLoadedFrontEndToggle();

                            result.add(new JsonObject()
                                    .put(ParamConfig.getProjectNameParam(), projectName)
                                    .put(ParamConfig.getIsNewParam(), isNew)
                                    .put(ParamConfig.getIsStarredParam(), isStarred)
                                    .put(ParamConfig.getIsLoadedParam(), isLoaded)
                                    .put(ParamConfig.getCreatedDateParam(), dateTime)
                                    .put(ParamConfig.getTotalUUIDParam(), total_uuid));
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

    private Tuple buildNewProject(String projectName, Integer annotationType, String projectID)
    {
        return Tuple.of(projectID,                   //project_id
                        projectName,                 //project_name
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
        String projectID = message.body().getString(ParamConfig.getProjectIdParam());

        ImageHandler.recheckProjectRootPath(projectID);

        message.reply(ReplyHandler.getOkReply());

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
                     portfolioDbPool.query(PortfolioDbQuery.createPortfolioTable())
                            .execute()
                            .onComplete(create -> {
                                if (create.succeeded()) {
                                    //the consumer methods registers an event bus destination handler
                                    vertx.eventBus().consumer(PortfolioDbQuery.getQueue(), this::onMessage);

                                    loadProjectLoader();

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
