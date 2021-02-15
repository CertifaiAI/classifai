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

import ai.classifai.database.DbConfig;
import ai.classifai.database.VerticleServiceable;
import ai.classifai.loader.CLIProjectInitiator;
import ai.classifai.loader.LoaderStatus;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.util.DateTime;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.ProjectHandler;
import ai.classifai.util.collection.ConversionHandler;
import ai.classifai.util.data.ImageHandler;
import ai.classifai.util.message.ErrorCodes;
import ai.classifai.util.message.ReplyHandler;
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
    private static JDBCPool portfolioDbPool;

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

        if (action.equals(PortfolioDbQuery.createNewProject()))
        {
            this.createNewProject(message);
        }
        else if (action.equals(PortfolioDbQuery.getAllProjectsForAnnotationType()))
        {
            this.getAllProjectsForAnnotationType(message);
        }
        else if (action.equals(PortfolioDbQuery.updateLabelList()))
        {
            this.updateLabelList(message);
        }
        else if (action.equals(PortfolioDbQuery.deleteProject()))
        {
            this.deleteProject(message);
        }
        //v2
        else if (action.equals(PortfolioDbQuery.getProjectMetadata()))
        {
            this.getProjectMetadata(message);
        }
        else if (action.equals(PortfolioDbQuery.getAllProjectsMetadata()))
        {
            this.getAllProjectsMetadata(message);
        }
        else if (action.equals(PortfolioDbQuery.starProject()))
        {
            this.starProject(message);
        }
        else
        {
            log.error("Portfolio query error. Action did not have an assigned function for handling.");
        }
    }

    public void createNewProject(Message<JsonObject> message)
    {
        JsonObject request = message.body();

        String projectName = request.getString(ParamConfig.getProjectNameParam());
        Integer annotationType = request.getInteger(ParamConfig.getAnnotateTypeParam());

        if (ProjectHandler.isProjectNameUnique(projectName, annotationType))
        {
            String annotationName = AnnotationHandler.getType(annotationType).name();

            log.info("Create " + annotationName + " project with name: " + projectName);

            String projectID = ProjectHandler.generateProjectID();

            Tuple params = buildNewProject(projectName, annotationType, projectID);

            portfolioDbPool.preparedQuery(PortfolioDbQuery.createNewProject())
                    .execute(params)
                    .onComplete(fetch -> {

                        if (fetch.succeeded())
                        {
                            ProjectHandler.buildProjectLoader(projectName, projectID, annotationType, LoaderStatus.LOADED, Boolean.TRUE);
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
        String projectID = message.body().getString(ParamConfig.getProjectIDParam());
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

    public void deleteProject(Message<JsonObject> message)
    {
        String projectID = message.body().getString(ParamConfig.getProjectIDParam());

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
        Integer annotationTypeIndex = message.body().getInteger(ParamConfig.getAnnotateTypeParam());

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
        portfolioDbPool.query(PortfolioDbQuery.loadDbProject())
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
                            List<String> labelList = new ArrayList<>();
                            List<String> uuidsFromDatabaseList = new ArrayList<>();
                            List<Boolean> isNewList = new ArrayList<>();

                            for (Row row : rowSet)
                            {
                                projectIDList.add(row.getString(0));
                                projectNameList.add(row.getString(1));
                                annotationTypeList.add(row.getInteger(2));
                                labelList.add(row.getString(3));
                                uuidsFromDatabaseList.add(row.getString(4));
                                isNewList.add(row.getBoolean(5));
                            }

                            for (int i = 0; i < projectIDList.size(); ++i)
                            {
                                ProjectLoader loader = ProjectHandler.buildProjectLoader(projectNameList.get(i), projectIDList.get(i), annotationTypeList.get(i), LoaderStatus.DID_NOT_INITIATED, isNewList.get(i));

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
                            ProjectHandler.buildProjectLoader(projectName, projectID, annotationInt, LoaderStatus.LOADED, Boolean.TRUE);

                            if(dataPath != null) ImageHandler.processFolder(projectID, dataPath);
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
        String projectID = message.body().getString(ParamConfig.getProjectIDParam());

        Tuple params = Tuple.of(projectID);

        portfolioDbPool.preparedQuery(PortfolioDbQuery.getProjectMetadata())
                .execute(params)
                .onComplete(fetch -> {

                    if (fetch.succeeded())
                    {
                        List<JsonObject> result = new ArrayList<>();

                        RowSet<Row> rowSet = fetch.result();

                        for (Row row : rowSet)
                        {
                            String projectName = row.getString(0);
                            List<String> uuidList = ConversionHandler.string2StringList(row.getString(1));
                            Boolean isNew = row.getBoolean(2);
                            Boolean isStarred = row.getBoolean(3);
                            Boolean isLoaded = ProjectHandler.getProjectLoader(projectID).getIsLoadedFrontEndToggle();
                            String dataTime = row.getString(4);

                            //project_name, uuid_list, is_new, is_starred, is_loaded, created_date
                            result.add(new JsonObject()
                                    .put(ParamConfig.getProjectNameParam(), projectName)
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
        Integer annotationTypeIndex = message.body().getInteger(ParamConfig.getAnnotateTypeParam());

        Tuple params = Tuple.of(annotationTypeIndex);

        portfolioDbPool.preparedQuery(PortfolioDbQuery.getAllProjectsMetadata())
                .execute(params)
                .onComplete(fetch -> {

                    if (fetch.succeeded())
                    {
                        List<JsonObject> result = new ArrayList<>();

                        RowSet<Row> rowSet = fetch.result();

                        for (Row row : rowSet)
                        {
                            String projectName = row.getString(0);
                            String uuidList = row.getString(1);
                            Boolean isNew = row.getBoolean(2);
                            Boolean isStarred = row.getBoolean(3);
                            String  dateTime = row.getString(4);

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

        portfolioDbPool.preparedQuery(PortfolioDbQuery.updateIsNewParam())
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
        String projectID = message.body().getString(ParamConfig.getProjectIDParam());
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

        portfolioDbPool.preparedQuery(PortfolioDbQuery.starProject())
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
        return Tuple.of(
                projectID,                   //project_id
                projectName,                 //project_name
                annotationType,              //annotation_type
                ParamConfig.getEmptyArray(), //label_list
                ParamConfig.getEmptyArray(), //uuid_list
                true,                       //is_new
                false,                       //is_starred
                DateTime.get());             //created_date
    }

    private static JDBCPool createJDBCPool(Vertx vertx, RelationalDb db)
    {
        return JDBCPool.pool(vertx, new JsonObject()
                .put("url", db.getUrlHeader() + DbConfig.getTableAbsPathDict().get(DbConfig.getPortfolioKey()))
                .put("driver_class", db.getDriver())
                .put("user", db.getUser())
                .put("password", db.getPassword())
                .put("max_pool_size", 30));
    }

    @Override
    public void stop(Promise<Void> promise)
    {
        portfolioDbPool.close();

        log.info("Portfolio Verticle stopping...");
    }

    //obtain a JDBC client connection,
    //Performs a SQL query to create the portfolio table unless existed
    @Override
    public void start(Promise<Void> promise)
    {
        H2 h2 = DbConfig.getH2();

        portfolioDbPool = createJDBCPool(vertx, h2);

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
