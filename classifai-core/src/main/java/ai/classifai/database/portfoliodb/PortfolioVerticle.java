/*
 * Copyright (c) 2020 CertifAI
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

package ai.classifai.database.portfoliodb;

import ai.classifai.annotation.AnnotationType;
import ai.classifai.database.DatabaseConfig;
import ai.classifai.database.loader.LoaderStatus;
import ai.classifai.database.loader.ProjectLoader;
import ai.classifai.server.ParamConfig;
import ai.classifai.util.ConversionHandler;
import ai.classifai.util.ProjectHandler;
import ai.classifai.util.message.ErrorCodes;
import ai.classifai.util.message.ReplyHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Portfolio is a collection of projects
 * Each projects contains respective UUIDs
 *
 * @author Chiawei Lim
 */
@Slf4j
public class PortfolioVerticle extends AbstractVerticle implements PortfolioServiceable
{
    //connection to database
    private static JDBCClient portfolioDbClient;

    public void onMessage(Message<JsonObject> message)
    {
        if (!message.headers().contains(ParamConfig.ACTION_KEYWORD))
        {
            log.error("No action header specified for message with headers {} and body {}",
                    message.headers(), message.body().encodePrettily());

            message.fail(ErrorCodes.NO_ACTION_SPECIFIED.ordinal(), "No keyword " + ParamConfig.ACTION_KEYWORD + " specified");

            return;
        }

        String action = message.headers().get(ParamConfig.ACTION_KEYWORD);

        if(action.equals(PortfolioDbQuery.CREATE_NEW_PROJECT))
        {
            this.createNewProject(message);
        }
        else if(action.equals(PortfolioDbQuery.GET_ALL_PROJECTS_FOR_ANNOTATION_TYPE))
        {
            this.getAllProjectsForAnnotationType(message);
        }
        else if(action.equals(PortfolioDbQuery.UPDATE_LABEL_LIST))
        {
            this.updateLabelList(message);
        }
        else if(action.equals(PortfolioDbQuery.GET_PROJECT_UUID_LIST))
        {
            this.getProjectUUIDList(message);
        }
        else if(action.equals(PortfolioDbQuery.GET_PROJECT_LABEL_LIST))
        {
            this.getLabelList(message);
        }
        else
        {
            log.error("Portfolio query error. Action did not have an assigned function for handling.");
        }
    }

    public void createNewProject(Message<JsonObject> message)
    {
        JsonObject request = message.body();

        String projectName = request.getString(ParamConfig.PROJECT_NAME_PARAM);
        Integer annotationType = request.getInteger(ParamConfig.ANNOTATE_TYPE_PARAM);

        if(ProjectHandler.isProjectNameUnique(projectName, annotationType)) {

            String annotationName = "";

            if(annotationType.equals(AnnotationType.BOUNDINGBOX.ordinal()))
            {
                annotationName = AnnotationType.BOUNDINGBOX.name();
            }
            else if(annotationType.equals(AnnotationType.SEGMENTATION.ordinal()))
            {
                annotationName = AnnotationType.SEGMENTATION.name();
            }

            log.info("Create project with name: " + projectName + " for " + annotationName + " project.");

            Integer projectID = ProjectHandler.generateProjectID();

            JsonArray params = new JsonArray().add(projectID).add(projectName).add(annotationType).add(ParamConfig.EMPTY_ARRAY).add(0).add(ParamConfig.EMPTY_ARRAY);

            portfolioDbClient.queryWithParams(PortfolioDbQuery.CREATE_NEW_PROJECT, params, fetch -> {

                if (fetch.succeeded())
                {
                    ProjectHandler.buildProjectLoader(projectName, projectID, annotationType, LoaderStatus.LOADED);
                    message.reply(ReplyHandler.getOkReply());
                }
                else
                {
                    //query database failed
                    message.reply(ReplyHandler.reportDatabaseQueryError(fetch.cause()));
                }
            });
        }
        else
        {
            message.reply(ReplyHandler.reportUserDefinedError("Project name exist. Please choose another one."));
        }

    }

    public static void updateUUIDGeneratorSeed(@NonNull Integer projectID, @NonNull Integer seedNumber)
    {
        ProjectHandler.getProjectLoader(projectID).setUuidGeneratorSeed(seedNumber);

        JsonArray params = new JsonArray().add(seedNumber).add(projectID);

        portfolioDbClient.queryWithParams(PortfolioDbQuery.UPDATE_UUID_GENERATOR_SEED, params, fetch -> {

            if(!fetch.succeeded()) {
                log.error("Update seed number in Portfolio failed. Project expected to hit error: ", fetch.cause().getMessage());
            }
        });
    }

    public void updateLabelList(Message<JsonObject> message)
    {
        Integer projectID = message.body().getInteger(ParamConfig.PROJECT_ID_PARAM);
        JsonArray labelList = message.body().getJsonArray(ParamConfig.LABEL_LIST_PARAM);

        portfolioDbClient.queryWithParams(PortfolioDbQuery.UPDATE_LABEL_LIST, new JsonArray().add(labelList.toString()).add(projectID), fetch ->{

            if(fetch.succeeded())
            {
                //update Project loader too
                List<String> labelListArray = ConversionHandler.jsonArray2StringList(labelList);

                ProjectLoader loader = ProjectHandler.getProjectLoader(projectID);

                loader.setLabelList(labelListArray);

                message.reply(ReplyHandler.getOkReply());
            }
            else {
                message.reply(ReplyHandler.reportDatabaseQueryError(fetch.cause()));
            }
        });
    }

    public void getProjectUUIDList(Message<JsonObject> message)
    {
        Integer projectID = message.body().getInteger(ParamConfig.PROJECT_ID_PARAM);

        JsonArray params = new JsonArray().add(projectID);

        portfolioDbClient.queryWithParams(PortfolioDbQuery.GET_PROJECT_UUID_LIST, params, fetch -> {

            if(fetch.succeeded()) {
                try {
                    ResultSet resultSet = fetch.result();

                    JsonArray row = resultSet.getResults().get(0);
                    JsonObject response = ReplyHandler.getOkReply();

                    String strList = row.getString(0);
                    Integer uuidGeneratorSeed = row.getInteger(1);

                    List<Integer> uuidList = ConversionHandler.string2IntegerList(strList);
                    //FIX ME: GET THE MAXIMUM AND set the generator if needed
                    response.put(ParamConfig.UUID_LIST_PARAM, uuidList);//row.getString(0)));
                    response.put(ParamConfig.UUID_GENERATOR_PARAM, uuidGeneratorSeed);

                    message.reply(response);
                }
                catch (Exception e) {
                    log.info("Failed in getting uuid list, ", e);
                };
            }
            else {
                //query database failed
                message.reply(ReplyHandler.reportDatabaseQueryError(fetch.cause()));
            }
        });
    }

    public void getLabelList(Message<JsonObject> message)
    {
        Integer projectID = message.body().getInteger(ParamConfig.PROJECT_ID_PARAM);

        JsonArray params = new JsonArray().add(projectID);

        portfolioDbClient.queryWithParams(PortfolioDbQuery.GET_PROJECT_LABEL_LIST, params, fetch -> {

            if (fetch.succeeded()) {
                ResultSet resultSet = fetch.result();
                JsonArray row = resultSet.getResults().get(0);

                List<String> labelList = ConversionHandler.string2StringList(row.getString(0));

                ProjectLoader loader = ProjectHandler.getProjectLoader(projectID);

                if(loader != null)
                {
                    loader.setLabelList(labelList);
                }
                else if(loader == null)
                {
                    log.info("Project Loader null. New label list failed to add into Project Loader. Program expected to failed");
                }

                message.reply(ReplyHandler.getOkReply());

            } else
            {
                message.reply(ReplyHandler.reportDatabaseQueryError(fetch.cause()));
            }
        });
    }

    public void getAllProjectsForAnnotationType(Message<JsonObject> message)
    {
        Integer annotationTypeIndex = message.body().getInteger(ParamConfig.ANNOTATE_TYPE_PARAM);

        portfolioDbClient.queryWithParams(PortfolioDbQuery.GET_ALL_PROJECTS_FOR_ANNOTATION_TYPE, new JsonArray().add(annotationTypeIndex), fetch -> {
            if (fetch.succeeded()) {

                List<String> projectNameList = fetch.result()
                        .getResults()
                        .stream()
                        .map(json -> json.getString(0))
                        .sorted()
                        .collect(Collectors.toList());

                JsonObject response = ReplyHandler.getOkReply();
                response.put(ParamConfig.CONTENT, projectNameList);

                message.reply(response);
            }
            else {
                message.reply(ReplyHandler.reportDatabaseQueryError(fetch.cause()));
            }
        });
    }

    public static void updateFileSystemUUIDList(@NonNull Integer projectID)
    {
        ProjectLoader loader = ProjectHandler.getProjectLoader(projectID);

        List<Integer> uuidList = loader.getSanityUUIDList();

        JsonArray jsonUpdateBody = new JsonArray().add(uuidList.toString()).add(projectID);

        portfolioDbClient.queryWithParams(PortfolioDbQuery.UPDATE_PROJECT, jsonUpdateBody, reply -> {

            if(!reply.succeeded())
            {
                log.error("Update list of uuids to Portfolio Database failed");
            }
        });
    }

    public void configurePortfolioVerticle()
    {
        portfolioDbClient.query(PortfolioDbQuery.GET_PROJECT_ID_LIST, fetch -> {
            if (fetch.succeeded()) {

                List<Integer> projectIDList = fetch.result()
                        .getResults()
                        .stream()
                        .map(json -> json.getInteger(0))
                        .sorted()
                        .collect(Collectors.toList());

                if(projectIDList.isEmpty())
                {
                    log.debug("Project ID List is empty. Initiate generator id from 0");
                    ProjectHandler.setProjectIDGenerator(0);
                }
                else
                {
                    //set the seed generator when creating new project
                    Integer maxProjectID = Collections.max(projectIDList);
                    ProjectHandler.setProjectIDGenerator(maxProjectID);

                    //set projectIDLoaderDict and projectIDSearch in ProjectHandler
                    for (Integer projectID : projectIDList)
                    {
                        JsonArray projectIDJson = new JsonArray().add(projectID);

                        portfolioDbClient.queryWithParams(PortfolioDbQuery.GET_PROJECT_NAME, projectIDJson, projectNameFetch -> {

                            if (projectNameFetch.succeeded()) {
                                ResultSet resultSet = projectNameFetch.result();

                                if (resultSet.getNumRows() != 0) {

                                    JsonArray row = resultSet.getResults().get(0);

                                    String projectName = row.getString(0);
                                    Integer annotationType = row.getInteger(1);
                                    Integer thisProjectID = projectIDJson.getInteger(0);

                                    ProjectHandler.buildProjectLoader(projectName, thisProjectID, annotationType, LoaderStatus.DID_NOT_INITIATED);
                                }
                            } else {
                                log.info("Retrieving project name failed: ", projectNameFetch.cause().getMessage());
                            }
                        });
                    }
                }

            }
        });
    }

    @Override
    public void stop(Promise<Void> promise) throws Exception
    {
        log.info("Portfolio Verticle stopping...");

        File lockFile = new File(DatabaseConfig.PORTFOLIO_DB_LCKFILE);

        if(lockFile.exists()) lockFile.delete();
    }


    //obtain a JDBC client connection,
    //Performs a SQL query to create the portfolio table unless existed
    @Override
    public void start(Promise<Void> promise) throws Exception
    {

        portfolioDbClient = JDBCClient.create(vertx, new JsonObject()
                .put("url", "jdbc:hsqldb:file:" + DatabaseConfig.PORTFOLIO_DB)
                .put("driver_class", "org.hsqldb.jdbcDriver")
                .put("max_pool_size", 30));

        portfolioDbClient.getConnection(ar -> {
                if (ar.succeeded()) {

                    SQLConnection connection = ar.result();
                    connection.execute(PortfolioDbQuery.CREATE_PORTFOLIO_TABLE, create -> {
                        connection.close();

                        if (create.succeeded()) {

                            //the consumer methods registers an event bus destination handler
                            vertx.eventBus().consumer(PortfolioDbQuery.QUEUE, this::onMessage);

                            configurePortfolioVerticle();

                            promise.complete();

                        } else
                        {
                            log.error("Portfolio database preparation error", create.cause());
                            promise.fail(create.cause());
                        }
                    });

                } else {

                    log.error("Could not open a portfolio database connection", ar.cause());
                    promise.fail(ar.cause());
                }
        });
    }
}
