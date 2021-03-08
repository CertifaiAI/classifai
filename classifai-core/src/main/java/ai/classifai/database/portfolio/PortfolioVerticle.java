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

import ai.classifai.action.ActionConfig;
import ai.classifai.action.ActionOps;
import ai.classifai.action.ProjectExport;
import ai.classifai.action.parser.AnnotationParser;
import ai.classifai.action.parser.PortfolioParser;
import ai.classifai.database.DbConfig;
import ai.classifai.database.VerticleServiceable;
import ai.classifai.database.annotation.AnnotationQuery;
import ai.classifai.database.versioning.Version;
import ai.classifai.loader.CLIProjectInitiator;
import ai.classifai.loader.LoaderStatus;
import ai.classifai.loader.NameGenerator;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.ProjectHandler;
import ai.classifai.util.collection.ConversionHandler;
import ai.classifai.util.collection.UuidGenerator;
import ai.classifai.util.data.ImageHandler;
import ai.classifai.util.message.ErrorCodes;
import ai.classifai.util.message.ReplyHandler;
import ai.classifai.util.type.AnnotationHandler;
import ai.classifai.util.type.AnnotationType;
import ai.classifai.util.type.database.H2;
import ai.classifai.util.type.database.RelationalDb;
import ai.classifai.database.versioning.ProjectVersion;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowIterator;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
        //*******************************V2*******************************

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

    public static void createNewProject(@NonNull String projectId)
    {
        ProjectLoader loader = ProjectHandler.getProjectLoader(projectId);

        Tuple params = PortfolioVerticle.buildNewProject(loader);

        portfolioDbPool.preparedQuery(PortfolioDbQuery.getCreateNewProject())
                .execute(params)
                .onComplete(fetch -> {

                    if (fetch.succeeded())
                    {
                        String annotation = AnnotationHandler.getType(loader.getAnnotationType()).name();
                        log.info("Project " + loader.getProjectName() + " of " + annotation.toLowerCase(Locale.ROOT) + " created");
                    }
                    else
                    {
                        log.debug("Create project failed from database");
                    }
                });
    }


    public static void loadProjectFromImportingConfigFile(@NonNull JsonObject input)
    {
        ProjectLoader loader = PortfolioParser.parseIn(input);

        while (!ProjectHandler.isProjectNameUnique(loader.getProjectName(), loader.getAnnotationType()))
        {
            String newProjName = new NameGenerator().getNewProjectName();
            loader.setProjectName(newProjName);
            loader.setProjectId(UuidGenerator.generateUuid());
            log.info("Project name overlapped. Rename project as " + newProjName);
        }
        ProjectHandler.loadProjectLoader(loader);

        //load project table first
        JsonObject contentJsonObject = input.getJsonObject(ParamConfig.getProjectContentParam());
        AnnotationParser.parseIn(loader, contentJsonObject);

        //load portfolio table last
        Tuple params = PortfolioVerticle.buildNewProject(loader);

        portfolioDbPool.preparedQuery(PortfolioDbQuery.getCreateNewProject())
                .execute(params)
                .onComplete(fetch -> {

                    if (fetch.succeeded())
                    {
                        log.info("Import project " + loader.getProjectName() + " success!");
                    }
                    else
                    {
                        log.info("Failed to import project " + loader.getProjectName() + " from configuration file");
                    }
                });


        //save the .updateFileSystemUuidList(loader.getProjectId());


        /*
        Tuple params = PortfolioVerticle.buildNewProject(loader);

        portfolioDbPool.preparedQuery(PortfolioDbQuery.getCreateNewProject())
                .execute(params)
                .onComplete(fetch -> {

                    if (fetch.succeeded())
                    {
                        AnnotationParser.parseIn(loader, input.getJsonObject(ParamConfig.getProjectContentParam()));

                        //write to database on the uuid_Version_list
                        PortfolioVerticle.updateFileSystemUuidList(loader.getProjectId());

                        log.info("Import project " + loader.getProjectName() + " success!");
                    }
                    else
                    {
                        log.info("Failed to import project " + loader.getProjectName() + " from configuration file");
                    }
                });

         */
    }
    /**
     public static void loadProjectFromImportingConfigFile(@NonNull JsonObject input)
     {
     ProjectLoader loader = PortfolioParser.parseIn(input);

     while (!ProjectHandler.isProjectNameUnique(loader.getProjectName(), loader.getAnnotationType()))
     {
     String newProjName = new NameGenerator().getNewProjectName();
     loader.setProjectName(newProjName);
     loader.setProjectID(UUIDGenerator.generateUUID());
     log.info("Project name overlapped. Rename project as " + newProjName);
     }

     Tuple params = PortfolioVerticle.buildNewProject(loader);

     //craete an empty project without uuid_list
     portfolioDbPool.preparedQuery(PortfolioDbQuery.getCreateNewProject())
     .execute(params)
     .onComplete(fetch -> {

     if (fetch.succeeded())
     {
     ProjectHandler.loadProjectLoader(loader);

     AnnotationParser.parseIn(loader, input.getJsonObject(ParamConfig.getProjectContentParam()));

     //write to database on the uuid_Version_list
     PortfolioVerticle.updateFileSystemUuidList(loader.getProjectId());

     log.info("Import project " + loader.getProjectName() + " success!");
     }
     else
     {
     log.info("Failed to import project " + loader.getProjectName() + " from configuration file");
     }
     });
     }
     */

    /**
     * v1 create new project
     * @param message
     */
    @Deprecated
    public void createV1NewProject(Message<JsonObject> message)
    {
        JsonObject request = message.body();

        String projectName = request.getString(ParamConfig.getProjectNameParam());
        Integer annotationInt = request.getInteger(ParamConfig.getAnnotationTypeParam());

        if (ProjectHandler.isProjectNameUnique(projectName, annotationInt))
        {
            String annotationName = AnnotationHandler.getType(annotationInt).name();

            log.info("Create " + annotationName + " project with name: " + projectName);

            //ProjectId is redundant
            String projectID = UuidGenerator.generateUuid();

            ProjectVersion project = new ProjectVersion();

            ProjectLoader loader = new ProjectLoader.Builder()
                    .projectId(projectID)
                    .projectName(projectName)
                    .annotationType(annotationInt)
                    .projectPath("")
                    .loaderStatus(LoaderStatus.LOADED)
                    .isProjectStarred(Boolean.FALSE)
                    .isProjectNew(Boolean.TRUE)
                    .projectVersion(project)
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

        List<String> labelList = ConversionHandler.jsonArray2StringList(labelListArray);

        ProjectLoader loader = ProjectHandler.getProjectLoader(projectId);

        //Badly written, fix this;
        /*
        loader.getVersionCollector().updateLabelList(loader.getCurrentVersion(), labelList);
        loader.setLabelList(labelList);

        Tuple params = Tuple.of(loader.getProjectVersion().getLabelDictObject2Db(), projectId);

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

         */
    }


    public void exportProject(Message<JsonObject> message)
    {
        ProjectExport exporter = new ProjectExport();

        String projectId = message.body().getString(ParamConfig.getProjectIdParam());

        Tuple params = Tuple.of(projectId);


        //export portfolio table relevant
        portfolioDbPool.preparedQuery(PortfolioDbQuery.getExportProject())
                .execute(params)
                .onComplete(fetch ->{

                    if (fetch.succeeded())
                    {
                        RowSet<Row> rowSet = fetch.result();

                        if(rowSet.size() == 0)
                        {
                            log.debug("Export project retrieve 0 rows. Project not found from portfolio database");
                            return;
                        }

                        Row portfolioRow = rowSet.iterator().next();

                        JsonObject configContent = exporter.getConfigSkeletonStructure();

                        PortfolioParser.parseOut(portfolioRow, configContent);

                        AnnotationType type = AnnotationHandler.getType(configContent.getString(ParamConfig.getAnnotationTypeParam()));

                        //export project table relevant
                        JDBCPool client = AnnotationHandler.getJDBCPool(type.ordinal());

                        ProjectLoader loader = ProjectHandler.getProjectLoader(projectId);

                        client.preparedQuery(AnnotationQuery.getExportProject())
                                .execute(params)
                                .onComplete(annotationFetch ->{

                                    if (annotationFetch.succeeded())
                                    {
                                        RowSet<Row> projectRowSet = annotationFetch.result();

                                        if(projectRowSet.size() == 0)
                                        {
                                            log.debug("Export project annotation retrieve 0 rows. Project not found from project database");
                                        }
                                        else
                                        {

                                            RowIterator<Row> projectRowIterator = projectRowSet.iterator();

                                            AnnotationParser.parseOut(projectRowIterator, configContent);
                                        }

                                        //export to configuration file
                                        String file = exporter.exportToFile(projectId, configContent);

                                        message.reply(ReplyHandler.getOkReply().put(ActionConfig.getProjectConfigPathParam(), file));
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

    public static void updateFileSystemUuidList(@NonNull String projectID)
    {
        ProjectLoader loader = ProjectHandler.getProjectLoader(projectID);

        List<String> uuidList = loader.getUuidListFromDb();

        /*
        loader.getProjectVersion().setCurrentVersionUuidList(uuidList);
        projVersion.updateUuidList(loader.getCurrentVersion(), uuidList);

        String dbUuidDict = loader.getVersionCollector().getUuidDictObject2Db();

        Tuple updateUuidListBody = Tuple.of(dbUuidDict, projectID);

        portfolioDbPool.preparedQuery(PortfolioDbQuery.getUpdateProject())
                .execute(updateUuidListBody)
                .onComplete(reply -> {
                    if (!reply.succeeded())
                    {
                        log.info("Update list of uuids to Portfolio Database failed");
                    }
                });
        */
    }

    private void configProjectLoaderFromDb()
    {
        portfolioDbPool.query(PortfolioDbQuery.getRetrieveAllProjects())
                .execute()
                .onComplete(projectNameFetch -> {

                    if (projectNameFetch.succeeded())
                    {
                        RowSet<Row> rowSet = projectNameFetch.result();
                        
                        if (rowSet.size() == 0)
                        {
                            log.debug("No projects founds.");
                        }
                        else
                        {
                            for (Row row : rowSet)
                            {

                                Version currentVersion = new Version(row.getString(6));

                                ProjectVersion project = PortfolioParser.loadProjectVersion(row.getString(7));     //project_version
                                project.setCurrentVersion(currentVersion.getVersionUuid());

                                Map uuidDict = ActionOps.getKeyWithArray(row.getString(8));
                                project.setUuidDict(uuidDict);                                                         //uuid_version_list

                                Map labelDict = ActionOps.getKeyWithArray(row.getString(9));
                                project.setLabelDict(labelDict);                                                       //label_version_list

                                ProjectLoader loader = new ProjectLoader.Builder()
                                    .projectId(row.getString(0))                                                   //project_id
                                    .projectName(row.getString(1))                                                 //project_name
                                    .annotationType(row.getInteger(2))                                             //annotation_type
                                    .projectPath(row.getString(3))                                                 //project_path
                                    .loaderStatus(LoaderStatus.DID_NOT_INITIATED)
                                    .isProjectNew(row.getBoolean(4))                                               //is_new
                                    .isProjectStarred(row.getBoolean(5))                                           //is_starred
                                    .projectVersion(project)                                                           //project_version
                                    .build();


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

        String projName = initiator.getProjectName();
        Integer annotationInt = initiator.getProjectType().ordinal();
        File dataPath = initiator.getRootDataPath();

        if(!ProjectHandler.isProjectNameUnique(projName, annotationInt))
        {
            String projectID = ProjectHandler.getProjectId(projName, annotationInt);

            if (dataPath != null) ImageHandler.processFolder(projectID, dataPath);

            return;
        }

        //createNewProject(projName, annotationInt, dataPath);
    }

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
                .put(ParamConfig.getIsNewParam(), loader.getIsProjectNew())
                .put(ParamConfig.getIsStarredParam(), loader.getIsProjectStarred())
                .put(ParamConfig.getIsLoadedParam(), loader.getIsLoadedFrontEndToggle())
                /*
                .put(ParamConfig.getCreatedDateParam(), loader.getCurrentVersion().getDateTime().toString())
                .put(ParamConfig.getCurrentVersionUuidParam(), loader.getCurrentVersion().getVersionUuid())
                .put(ParamConfig.getVersionListParam(), loader.getVersionCollector().toString())
                */
                .put(ParamConfig.getTotalUuidParam(), loader.getUuidListFromDb().size()));
    }

    /**
     * V2 get all available project metadata
     * @param message
     */
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

                            getProjectMetadata(result, ProjectHandler.getProjectId(projectName, annotationTypeIndex));
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
                        ProjectHandler.getProjectLoader(projectID).setIsProjectNew(Boolean.FALSE);
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
        //Version list
        ProjectVersion project = loader.getProjectVersion();

        System.out.println("1: " + loader.getProjectId());

        System.out.println("2: " + loader.getProjectName());

        System.out.println("3: " + loader.getAnnotationType());
        System.out.println("4: " + loader.getProjectPath());
        System.out.println("5: " + loader.getIsProjectNew());
        System.out.println("6: " + loader.getIsProjectStarred());

        System.out.println("7: " + project.getCurrentVersion().getDbFormat());

        System.out.println("8: " + project.getDbFormat());

        System.out.println("9: " + project.getUuidVersionDbFormat());

        System.out.println("10: " + project.getLabelVersionDbFormat());


        return Tuple.of(loader.getProjectId(),              //project_id
                loader.getProjectName(),                    //project_name
                loader.getAnnotationType(),                 //annotation_type
                loader.getProjectPath(),                    //project_path
                loader.getIsProjectNew(),                   //is_new
                loader.getIsProjectStarred(),               //is_starred
                project.getCurrentVersion().getDbFormat(),  //current_version
                project.getDbFormat(),                      //version_list
                project.getUuidVersionDbFormat(),           //uuid_version_list
                project.getLabelVersionDbFormat());         //label_version_list

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

        portfolioDbPool = createJDBCPool(vertx, h2);

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
