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
import ai.classifai.action.parser.PortfolioParser;
import ai.classifai.action.parser.ProjectParser;
import ai.classifai.database.DbConfig;
import ai.classifai.database.VerticleServiceable;
import ai.classifai.database.annotation.AnnotationQuery;
import ai.classifai.database.annotation.AnnotationVerticle;
import ai.classifai.database.versioning.ProjectVersion;
import ai.classifai.database.versioning.Version;
import ai.classifai.loader.CLIProjectInitiator;
import ai.classifai.loader.LoaderStatus;
import ai.classifai.loader.NameGenerator;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.selector.project.ProjectImportSelector;
import ai.classifai.ui.SelectionWindow;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.collection.ConversionHandler;
import ai.classifai.util.collection.UuidGenerator;
import ai.classifai.util.data.ImageHandler;
import ai.classifai.util.data.StringHandler;
import ai.classifai.util.message.ErrorCodes;
import ai.classifai.util.message.ReplyHandler;
import ai.classifai.util.project.ProjectHandler;
import ai.classifai.util.project.ProjectInfra;
import ai.classifai.util.project.ProjectInfraHandler;
import ai.classifai.util.type.AnnotationHandler;
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

import javax.swing.*;
import java.io.File;
import java.util.*;

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
        else if(action.equals(PortfolioDbQuery.getRenameProject()))
        {
            renameProject(message);
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

    public static void renameProject(@NonNull Message<JsonObject> message)
    {
        String projectId = message.body().getString(ParamConfig.getProjectIdParam());
        String newProjectName = message.body().getString(ParamConfig.getNewProjectNameParam());

        Tuple params = Tuple.of(newProjectName, projectId);

        portfolioDbPool.preparedQuery(PortfolioDbQuery.getRenameProject())
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

    public static void loadProjectFromImportingConfigFile(@NonNull JsonObject input)
    {
        ProjectLoader loader = PortfolioParser.parseIn(input);

        String newProjName = "";
        while (!ProjectHandler.isProjectNameUnique(loader.getProjectName(), loader.getAnnotationType()))
        {
            newProjName = new NameGenerator().getNewProjectName();
            loader.setProjectName(newProjName);
            loader.setProjectId(UuidGenerator.generateUuid());
        }

        // Only show popup if there is duplicate project name
        if(!newProjName.equals(""))
        {
            log.info("Name Overlapped. Rename project as " + newProjName);
        }

        ProjectHandler.loadProjectLoader(loader);

        //load project table first
        JsonObject contentJsonObject = input.getJsonObject(ParamConfig.getProjectContentParam());
        ProjectParser.parseIn(loader, contentJsonObject);

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

    }

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

            ProjectVersion project = new ProjectVersion();

            ProjectLoader loader = ProjectLoader.builder()
                    .projectId(UuidGenerator.generateUuid())
                    .projectName(projectName)
                    .annotationType(annotationInt)
                    .projectPath("")
                    .loaderStatus(LoaderStatus.LOADED)
                    .isProjectStarred(Boolean.FALSE)
                    .isProjectNew(Boolean.TRUE)
                    .projectVersion(project)
                    .projectInfra(ProjectInfra.ON_PREMISE)
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
        JsonArray newLabelListJson = new JsonArray(message.body().getString(ParamConfig.getLabelListParam()));

        ProjectLoader loader = Objects.requireNonNull(ProjectHandler.getProjectLoader(projectId));

        List<String> newLabelList = new ArrayList<>();

        for(Object label: newLabelListJson)
        {
            String trimmedLabel = StringHandler.removeEndOfLineChar((String) label);

            newLabelList.add(trimmedLabel);
        }

        ProjectVersion project = loader.getProjectVersion();

        project.setCurrentVersionLabelList(newLabelList);
        loader.setLabelList(newLabelList);

        Tuple updateUuidListBody = Tuple.of(project.getLabelVersionDbFormat(), projectId);

        portfolioDbPool.preparedQuery(PortfolioDbQuery.getUpdateLabelList())
                .execute(updateUuidListBody)
                .onComplete(reply -> {
                    if (reply.succeeded())
                    {
                        message.replyAndRequest(ReplyHandler.getOkReply());
                    }
                    else
                    {
                        log.info("Update list of uuids to Portfolio Database failed");
                        message.replyAndRequest(ReplyHandler.reportDatabaseQueryError(reply.cause()));
                    }
                });
    }


    public void exportProject(Message<JsonObject> message)
    {
        String projectId = message.body().getString(ParamConfig.getProjectIdParam());
        Tuple params = Tuple.of(projectId);

        //export portfolio table relevant
        portfolioDbPool.preparedQuery(PortfolioDbQuery.getExportProject())
                .execute(params)
                .onComplete(fetch ->{

                    if (fetch.succeeded())
                    {
                        //export project table relevant
                        ProjectLoader loader = ProjectHandler.getProjectLoader(projectId);
                        JDBCPool client = AnnotationHandler.getJDBCPool(Objects.requireNonNull(loader));

                        client.preparedQuery(AnnotationQuery.getExtractProject())
                                .execute(params)
                                .onComplete(annotationFetch ->{
                                    if (annotationFetch.succeeded())
                                    {
                                        exportProjectOnSuccess(annotationFetch.result(), fetch.result(),
                                                message, loader);
                                    }
                                });
                    }
                    else
                    {
                        message.replyAndRequest(ReplyHandler.reportDatabaseQueryError(fetch.cause()));
                    }
                });

    }

    private void exportProjectOnSuccess(RowSet<Row> projectRowSet, RowSet<Row> rowSet, Message<JsonObject> message,
                                             ProjectLoader loader)
    {
        JsonObject configContent = ProjectExport.getConfigContent(rowSet, projectRowSet);
        if(configContent == null) return;

        int exportType = message.body().getInteger(ActionConfig.getExportTypeParam());

        String exportPath = ProjectExport.runExportProcess(loader, configContent, exportType);
        if(exportPath != null)
        {
            message.reply(ReplyHandler.getOkReply().put(
                    ActionConfig.getProjectConfigPathParam(), exportPath));

            return;
        }
        message.reply(ReplyHandler.getFailedReply());
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

        ProjectVersion project = loader.getProjectVersion();

        project.setCurrentVersionUuidList(uuidList);

        Tuple updateUuidListBody = Tuple.of(project.getUuidVersionDbFormat(), projectID);

        portfolioDbPool.preparedQuery(PortfolioDbQuery.getUpdateProject())
                .execute(updateUuidListBody)
                .onComplete(reply -> {
                    if (!reply.succeeded())
                    {
                        log.info("Update list of uuids to Portfolio Database failed");
                    }
                });
    }

    public void configProjectLoaderFromDb()
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
                                Version currentVersion = new Version(row.getString(7));

                                ProjectVersion project = PortfolioParser.loadProjectVersion(row.getString(8));     //project_version

                                project.setCurrentVersion(currentVersion.getVersionUuid());

                                Map uuidDict = ActionOps.getKeyWithArray(row.getString(9));
                                project.setUuidListDict(uuidDict);                                                      //uuid_project_version

                                Map labelDict = ActionOps.getKeyWithArray(row.getString(10));
                                project.setLabelListDict(labelDict);                                                    //label_project_version

                                ProjectLoader loader = ProjectLoader.builder()
                                    .projectId(row.getString(0))                                                   //project_id
                                    .projectName(row.getString(1))                                                 //project_name
                                    .annotationType(row.getInteger(2))                                             //annotation_type
                                    .projectPath(row.getString(3))                                                 //project_path
                                    .loaderStatus(LoaderStatus.DID_NOT_INITIATED)
                                    .isProjectNew(row.getBoolean(4))                                               //is_new
                                    .isProjectStarred(row.getBoolean(5))                                           //is_starred
                                    .projectInfra(ProjectInfraHandler.getInfra(row.getString(6)))                  //project_infra
                                    .projectVersion(project)                                                           //project_version
                                    .build();

                                //load each data points
                                AnnotationVerticle.configProjectLoaderFromDb(loader);
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
        ProjectLoader loader = Objects.requireNonNull(ProjectHandler.getProjectLoader(projectId));

        Version currentVersion = loader.getProjectVersion().getCurrentVersion();

        List<File> existingDataInDir = ImageHandler.getValidImagesFromFolder(new File(loader.getProjectPath()));

        result.add(new JsonObject()
                .put(ParamConfig.getProjectNameParam(), loader.getProjectName())
                .put(ParamConfig.getProjectPathParam(), loader.getProjectPath())
                .put(ParamConfig.getIsNewParam(), loader.getIsProjectNew())
                .put(ParamConfig.getIsStarredParam(), loader.getIsProjectStarred())
                .put(ParamConfig.getIsLoadedParam(), loader.getIsLoadedFrontEndToggle())
                .put(ParamConfig.getIsCloudParam(), loader.isCloud())
                .put(ParamConfig.getProjectInfraParam(), loader.getProjectInfra())
                .put(ParamConfig.getCreatedDateParam(), currentVersion.getDateTime().toString())
                .put(ParamConfig.getCurrentVersionParam(), currentVersion.getVersionUuid())
                .put(ParamConfig.getTotalUuidParam(), existingDataInDir.size()));
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
        //version list
        ProjectVersion project = loader.getProjectVersion();

        return Tuple.of(loader.getProjectId(),              //project_id
                loader.getProjectName(),                    //project_name
                loader.getAnnotationType(),                 //annotation_type
                loader.getProjectPath(),                    //project_path
                loader.getIsProjectNew(),                   //is_new
                loader.getIsProjectStarred(),               //is_starred
                loader.getProjectInfra().name(),            //project_infra
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
