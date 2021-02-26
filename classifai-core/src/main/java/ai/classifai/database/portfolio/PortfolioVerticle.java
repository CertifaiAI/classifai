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
import ai.classifai.util.data.StringHandler;
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
import ai.classifai.util.versioning.ProjectVersion;
import ai.classifai.util.versioning.VersionCollection;
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
import java.util.Arrays;
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

    public static void createV2NewProject(@NonNull String projectName, @NonNull Integer annotationInt, @NonNull File rootPath)
    {
        if (ProjectHandler.isProjectNameUnique(projectName, annotationInt))
        {
            String annotationName = AnnotationHandler.getType(annotationInt).name();

            log.info("Create " + annotationName + " project with name: " + projectName);

            String projectID = UUIDGenerator.generateUUID();

            ProjectVersion projectVersion = new ProjectVersion();
            VersionCollection versionCollection = new VersionCollection(Arrays.asList(projectVersion));

            String rootProjectPath = rootPath == null ? "" : rootPath.getAbsolutePath();

            ProjectLoader loader = new ProjectLoader.Builder()
                    .projectID(projectID)
                    .projectName(projectName)
                    .annotationType(annotationInt)
                    .projectPath(rootProjectPath)
                    .loaderStatus(LoaderStatus.LOADED)
                    .isProjectStarred(Boolean.FALSE)
                    .isProjectNewlyCreated(Boolean.TRUE)
                    .currentProjectVersion(projectVersion)
                    .versionCollection(versionCollection)
                    .build();

            Tuple params = PortfolioVerticle.buildNewProject(loader);

            portfolioDbPool.preparedQuery(PortfolioDbQuery.getCreateNewProject())
                    .execute(params)
                    .onComplete(fetch -> {

                    if (fetch.succeeded())
                    {
                        ProjectHandler.loadProjectLoader(loader);

                        if(rootPath != null)
                        {
                            loader.setFileSystemStatus(FileSystemStatus.WINDOW_CLOSE_LOADING_FILES);
                            ImageHandler.processFolder(projectID, rootPath);
                        }

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

            VersionCollection versionCollection = new VersionCollection(Arrays.asList(new ProjectVersion()));

            ProjectLoader loader = new ProjectLoader.Builder()
                    .projectID(projectID)
                    .projectName(projectName)
                    .annotationType(annotationType)
                    .projectPath("")
                    .loaderStatus(LoaderStatus.LOADED)
                    .isProjectNewlyCreated(Boolean.TRUE)
                    .versionCollection(versionCollection)
                    .build();

            Tuple params = PortfolioVerticle.buildNewProject(loader);

            portfolioDbPool.preparedQuery(PortfolioDbQuery.getCreateNewProject())
                    .execute(params)
                    .onComplete(fetch -> {

                        if (fetch.succeeded())
                        {
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
        String projectId = message.body().getString(ParamConfig.getProjectIdParam());
        JsonArray labelListArray = message.body().getJsonArray(ParamConfig.getLabelListParam());

        System.out.println("LabelListArray: " + labelListArray);

        List<String> labelList = ConversionHandler.jsonArray2StringList(labelListArray);

        ProjectLoader loader = ProjectHandler.getProjectLoader(projectId);

        //Badly written, fix this;
        loader.getVersionCollector().updateLabelList(loader.getCurrentProjectVersion(), labelList);
        loader.setLabelList(labelList);

        Tuple params = Tuple.of(loader.getVersionCollector().getLabelDictObject2Db(), projectId);

        portfolioDbPool.preparedQuery(PortfolioDbQuery.getUpdateLabelList())
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

        /*
        loader.getVersionCollector().updateUuidList(loader.getCurrentProjectVersion(), uuidList);

        String projectId = message.body().getString(ParamConfig.getProjectIdParam());
        JsonArray labelListArray = message.body().getJsonArray(ParamConfig.getLabelListParam());

        System.out.println("LabelListArray: " + labelListArray);

        List<String> labelList = ConversionHandler.jsonArray2StringList(labelListArray);

        ProjectLoader loader = ProjectHandler.getProjectLoader(projectId);
        loader.getVersionCollector().updateLabelList(loader.getCurrentProjectVersion(), labelList);

        Tuple params = Tuple.of(loader.getVersionCollector().getLabelDictObject2Db(), projectId);

        portfolioDbPool.preparedQuery(PortfolioDbQuery.getUpdateLabelList())
                .execute(params)
                .onComplete(fetch ->{

                    if (fetch.succeeded())
                    {
                        loader.setLabelList(labelList);

                        message.replyAndRequest(ReplyHandler.getOkReply());
                    }
                    else
                    {
                        message.replyAndRequest(ReplyHandler.reportDatabaseQueryError(fetch.cause()));
                    }
                });

         */

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

        //FIXME: badlywritten
        loader.getVersionCollector().updateUuidList(loader.getCurrentProjectVersion(), uuidList);

        String dbUuidDict = loader.getVersionCollector().getUuidDictObject2Db();

        Tuple updateUuidListBody = Tuple.of(dbUuidDict, projectID);
        portfolioDbPool.preparedQuery(PortfolioDbQuery.getUpdateProject())
                .execute(updateUuidListBody)
                .onComplete(reply -> {
                    if (reply.succeeded())
                    {
                        System.out.println("Update v2 list of uuids to Portfolio Database success");
                        loader.setUuidListFromDatabase(uuidList);
                    }
                    else
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
                                VersionCollection versionCollector = new VersionCollection(row.getString(7));
                                ProjectVersion projVersion = versionCollector.getVersionUuidDict().get(row.getString(6));
                                versionCollector.setUuidDict(row.getString(8));
                                versionCollector.setLabelDict(row.getString(9));

                                ProjectLoader loader = new ProjectLoader.Builder()
                                    .projectID(row.getString(0))
                                    .projectName(row.getString(1))
                                    .annotationType(row.getInteger(2))
                                    .projectPath(row.getString(3))
                                    .loaderStatus(LoaderStatus.DID_NOT_INITIATED)
                                    .isProjectNewlyCreated(row.getBoolean(4))
                                    .isProjectStarred(row.getBoolean(5))
                                    .versionCollection(versionCollector)
                                    .currentProjectVersion(projVersion)
                                    .build();


                                //loader.setLabelList(ConversionHandler.string2StringList(row.getString(4)));

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

        if(!ProjectHandler.isProjectNameUnique(projectName, annotationInt))
        {
            String projectID = ProjectHandler.getProjectID(projectName, annotationInt);

            if (dataPath != null) ImageHandler.processFolder(projectID, dataPath);

            return;
        }

        createV2NewProject(projectName, annotationInt, dataPath);
    }

    //V2 API
    public void getProjectMetadata(Message<JsonObject> message)
    {
        String projectId = message.body().getString(ParamConfig.getProjectIdParam());

        List<JsonObject> result = new ArrayList<>();

        getProjectMetadata(result, projectId);

        JsonObject response = ReplyHandler.getOkReply();
        response.put(ParamConfig.getContent(), result);

        message.replyAndRequest(response);
    }

    private void getProjectMetadata(@NonNull List<JsonObject> result, @NonNull String projectId)
    {
        ProjectLoader loader = ProjectHandler.getProjectLoader(projectId);

        result.add(new JsonObject()
                .put(ParamConfig.getProjectNameParam(), loader.getProjectName())
                .put(ParamConfig.getProjectPathParam(), loader.getProjectPath())
                .put(ParamConfig.getIsNewParam(), loader.getIsProjectNewlyCreated())
                .put(ParamConfig.getIsStarredParam(), loader.getIsProjectStarred())
                .put(ParamConfig.getIsLoadedParam(), loader.getIsLoadedFrontEndToggle())
                .put(ParamConfig.getCreatedDateParam(), loader.getCurrentProjectVersion().getDateTime().toString())
                .put(ParamConfig.getCurrentVersionUuidParam(), loader.getCurrentProjectVersion().getVersionUuid())
                .put(ParamConfig.getVersionListParam(), loader.getVersionCollector().toString())
                .put(ParamConfig.getTotalUuidParam(), loader.getUuidListFromDatabase().size()));
    }

    //V2 API
    public void getAllProjectsMetadata(Message<JsonObject> message)
    {
        Integer annotationTypeIndex = message.body().getInteger(ParamConfig.getAnnotationTypeParam());

        Tuple params = Tuple.of(annotationTypeIndex);

        portfolioDbPool.preparedQuery(PortfolioDbQuery.getRetrieveAllProjectsForAnnotationType())
                .execute(params)
                .onComplete(fetch -> {

                    if (fetch.succeeded())
                    {
                        RowSet<Row> rowSet = fetch.result();

                        List<JsonObject> result = new ArrayList<>();

                        for (Row row : rowSet)
                        {
                            String projectName = row.getString(0);

                            getProjectMetadata(result, ProjectHandler.getProjectID(projectName, annotationTypeIndex));
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

    private static Tuple buildNewProject(@NonNull ProjectLoader loader)
    {
        ProjectVersion projVersion = loader.getCurrentProjectVersion();
        JsonArray versionList = new JsonArray().add(projVersion.getJsonObject());

        return Tuple.of(loader.getProjectID(),              //project_id
                loader.getProjectName(),              //project_name
                loader.getAnnotationType(),                 //annotation_type
                loader.getProjectPath(),                    //project_path
                true,                                       //is_new
                false,                                      //is_starred
                projVersion.getVersionUuid(),               //current_version
                versionList.toString(),                     //version_list
                ParamConfig.getEmptyArray(),                //uuid_version_list
                ParamConfig.getEmptyArray());               //label_version_list

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
