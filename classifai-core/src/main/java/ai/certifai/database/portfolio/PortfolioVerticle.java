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

package ai.certifai.database.portfolio;

import ai.certifai.database.DatabaseConfig;
import ai.certifai.selector.SelectorHandler;
import ai.certifai.server.ServerConfig;
import ai.certifai.util.ConversionHandler;
import ai.certifai.util.message.ErrorCodes;
import ai.certifai.util.message.ReplyHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Portfolio is a collection of projects
 * Each projects contains respective UUIDs
 */
@Slf4j
public class PortfolioVerticle extends AbstractVerticle implements PortfolioServiceable
{
    //connection to database
    private static JDBCClient portfolioDbClient;

    private static String projectNameExistMessage = "Project name did not exist. Retrieve uuid list from existing project name failed";

    public void onMessage(Message<JsonObject> message)
    {
        if (!message.headers().contains(ServerConfig.ACTION_KEYWORD))
        {
            log.error("No action header specified for message with headers {} and body {}",
                    message.headers(), message.body().encodePrettily());

            message.fail(ErrorCodes.NO_ACTION_SPECIFIED.ordinal(), "No keyword " + ServerConfig.ACTION_KEYWORD + " specified");

            return;
        }

        String action = message.headers().get(ServerConfig.ACTION_KEYWORD);

        if(action.equals(PortfolioSQLQuery.CREATE_NEW_PROJECT))
        {
            this.createNewProject(message);
        }
        else if(action.equals(PortfolioSQLQuery.GET_ALL_PROJECTS))
        {
            this.getAllProjects(message);
        }
        else if(action.equals(PortfolioSQLQuery.UPDATE_LABEL))
        {
            this.updateLabel(message);
        }
        else if(action.equals(PortfolioSQLQuery.GET_PROJECT_UUID_LIST))
        {
            this.getProjectUUIDList(message);
        }
        else if(action.equals(PortfolioSQLQuery.GET_THUMBNAIL_LIST))
        {
            this.getThumbNailList(message);
        }
        else if(action.equals(PortfolioSQLQuery.GET_UUID_LABEL_LIST))
        {
            this.getUUIDLabelList(message);
        }
        else if(action.equals(PortfolioSQLQuery.UPDATE_PROJECT))
        {
            this.updateUUIDList(message);
        }
        else if(action.equals(PortfolioSQLQuery.REMOVE_OBSOLETE_UUID_LIST))
        {
            this.removeObsoleteUUID(message);
        }
        else
        {
            log.error("Portfolio SQL query error: Action did not found follow up with function");
        }
    }


    public void createNewProject(Message<JsonObject> message)
    {
        JsonObject request = message.body();

        String projectName = request.getString(ServerConfig.PROJECT_NAME_PARAM);

        if(!SelectorHandler.isProjectNameRegistered(projectName)) {

            log.info("Create project with name: " + projectName + " in portfolio table");

            Integer projectID = SelectorHandler.generateProjectID();

            JsonArray params = new JsonArray().add(projectID).add(projectName).add(ServerConfig.EMPTY_ARRAY).add(0).add(ServerConfig.EMPTY_ARRAY);

            portfolioDbClient.queryWithParams(PortfolioSQLQuery.CREATE_NEW_PROJECT, params, fetch -> {

                if (fetch.succeeded()) {
                    SelectorHandler.setUUIDGenerator(0);
                    SelectorHandler.setProjectNameNID(projectName, projectID);
                    message.reply(ReplyHandler.getOkReply());
                } else {
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

    public void updateLabel(Message<JsonObject> message)
    {
        String projectName = message.body().getString(ServerConfig.PROJECT_NAME_PARAM);
        JsonArray labelList = message.body().getJsonArray(ServerConfig.LABEL_LIST_PARAM);

        if(SelectorHandler.isProjectNameRegistered(projectName))
        {
            portfolioDbClient.queryWithParams(PortfolioSQLQuery.UPDATE_LABEL, new JsonArray().add(labelList.toString()).add(projectName), fetch ->{

                if(fetch.succeeded())
                {
                    message.reply(ReplyHandler.getOkReply());
                }
                else {
                    message.reply(ReplyHandler.reportDatabaseQueryError(fetch.cause()));
                }
            });
        }
        else
        {
            message.reply(ReplyHandler.reportUserDefinedError(projectNameExistMessage));
        }
    }

    public void removeObsoleteUUID(Message<JsonObject> message)
    {
        String projectName = message.body().getString(ServerConfig.PROJECT_NAME_PARAM);
        JsonArray uuidListArray = message.body().getJsonArray(ServerConfig.UUID_LIST_PARAM);

        List<Integer> uuidListToRemove = ConversionHandler.jsonArray2IntegerList(uuidListArray);

        if(SelectorHandler.isProjectNameRegistered(projectName))
        {
            JsonArray params = new JsonArray().add(projectName);

            portfolioDbClient.queryWithParams(PortfolioSQLQuery.GET_PROJECT_UUID_LIST, params, fetch -> {

                if(fetch.succeeded())
                {
                    ResultSet resultSet = fetch.result();
                    JsonArray row = resultSet.getResults().get(0);

                    List<Integer> uuidList =  ConversionHandler.string2IntegerList(row.getString(0));

                    for(Integer uuid : uuidListToRemove) uuidList.removeIf(index -> (index == uuid));

                    JsonArray updateParam = new JsonArray().add(uuidList.toString()).add(projectName);

                    portfolioDbClient.queryWithParams(PortfolioSQLQuery.UPDATE_PROJECT, updateParam, result ->
                    {
                        if(result.succeeded())
                        {
                            message.reply(ReplyHandler.getOkReply());
                        }
                        else
                        {
                            message.reply(ReplyHandler.reportDatabaseQueryError(fetch.cause()));

                        }
                    });
                } else {
                    //query database failed
                    message.reply(ReplyHandler.reportDatabaseQueryError(fetch.cause()));
                }
            });
        } else {
            message.reply(ReplyHandler.reportBadParamError(projectNameExistMessage));
        }
    }

    public void getProjectUUIDList(Message<JsonObject> message)
    {
        String projectName = message.body().getString(ServerConfig.PROJECT_NAME_PARAM);

        if(SelectorHandler.isProjectNameRegistered(projectName))
        {
            JsonArray params = new JsonArray().add(projectName);

            portfolioDbClient.queryWithParams(PortfolioSQLQuery.GET_PROJECT_UUID_LIST, params, fetch -> {

                if(fetch.succeeded())
                {
                    ResultSet resultSet = fetch.result();
                    JsonArray row = resultSet.getResults().get(0);
                    JsonObject response = ReplyHandler.getOkReply();

                    response.put(ServerConfig.UUID_LIST_PARAM, ConversionHandler.string2IntegerList(row.getString(0)));
                    message.reply(response);
                }
                else {
                    //query database failed
                    message.reply(ReplyHandler.reportDatabaseQueryError(fetch.cause()));
                }
            });
        }
        else
        {
            message.reply(ReplyHandler.reportBadParamError(projectNameExistMessage));
        }
    }

    public void getThumbNailList(Message<JsonObject> message)
    {
        String projectName = message.body().getString(ServerConfig.PROJECT_NAME_PARAM);

        if(SelectorHandler.isProjectNameRegistered(projectName))
        {
            JsonArray params = new JsonArray().add(projectName);

            portfolioDbClient.queryWithParams(PortfolioSQLQuery.GET_THUMBNAIL_LIST, params, fetch -> {

                if(fetch.succeeded())
                {
                    ResultSet resultSet = fetch.result();
                    JsonArray row = resultSet.getResults().get(0);

                    List<Integer> wholeUUIDList = ConversionHandler.string2IntegerList(row.getString(0));

                    List<Integer> subList = new ArrayList<>();

                    if(wholeUUIDList.isEmpty() == false)
                    {
                        Integer previousUUIDPointer = row.getInteger(1);

                        for(Integer item : wholeUUIDList)
                        {
                            if(item > previousUUIDPointer)
                            {
                                subList.add(item);
                            }
                        }

                        Integer currentUUIDMarker = Collections.max(wholeUUIDList);

                        JsonArray markerUpdateParams = new JsonArray().add(currentUUIDMarker).add(projectName);

                        //this is for update of thumbnail list to not include the existing send one to front end
                        portfolioDbClient.queryWithParams(PortfolioSQLQuery.UPDATE_THUMBNAIL_MAX_INDEX, markerUpdateParams, markerFetch -> {

                            if (!markerFetch.succeeded()) {
                                log.error("Update thumbnail marker failed");
                            }
                        });

                    }
                    JsonObject response = ReplyHandler.getOkReply();
                    response.put(ServerConfig.UUID_LIST_PARAM, subList);
                    message.reply(response);

                }
                else {
                    //query database failed
                    message.reply(ReplyHandler.reportDatabaseQueryError(fetch.cause()));
                }
            });

        }
        else
        {
            message.reply(ReplyHandler.reportUserDefinedError(projectNameExistMessage));
        }
    }

    public void getUUIDLabelList(Message<JsonObject> message)
    {
        String projectName = message.body().getString(ServerConfig.PROJECT_NAME_PARAM);

        if(SelectorHandler.isProjectNameRegistered(projectName)) {
            JsonArray params = new JsonArray().add(projectName);

            portfolioDbClient.queryWithParams(PortfolioSQLQuery.GET_UUID_LABEL_LIST, params, fetch -> {

                if (fetch.succeeded()) {
                    ResultSet resultSet = fetch.result();
                    JsonArray row = resultSet.getResults().get(0);

                    List<String> labelList = ConversionHandler.string2StringList(row.getString(0));
                    List<Integer> uuidList = ConversionHandler.string2IntegerList(row.getString(1));

                    JsonObject reply = ReplyHandler.getOkReply();
                    reply.put(ServerConfig.LABEL_LIST_PARAM, labelList);
                    reply.put(ServerConfig.UUID_LIST_PARAM, uuidList);

                    message.reply(reply);
                } else {
                    message.reply(ReplyHandler.reportDatabaseQueryError(fetch.cause()));
                }
            });
        }
        else
        {
            message.reply(ReplyHandler.reportUserDefinedError(projectNameExistMessage));
        }
    }

    public void getAllProjects(Message<JsonObject> message)
    {
        portfolioDbClient.query(PortfolioSQLQuery.GET_ALL_PROJECTS, fetch -> {
            if (fetch.succeeded()) {

                List<String> projectNameList = fetch.result()
                        .getResults()
                        .stream()
                        .map(json -> json.getString(0))
                        .sorted()
                        .collect(Collectors.toList());

                JsonObject response = ReplyHandler.getOkReply();
                response.put(ServerConfig.CONTENT, projectNameList);

                message.reply(response);
            }
            else {
                message.reply(ReplyHandler.reportDatabaseQueryError(fetch.cause()));
            }
        });
    }

    public static void updateUUIDList(Message<JsonObject> message)
    {
        String projectName = message.body().getString(ServerConfig.PROJECT_NAME_PARAM);
        JsonArray uuidList = message.body().getJsonArray(ServerConfig.UUID_LIST_PARAM);

        if(SelectorHandler.isProjectNameRegistered(projectName))
        {
            portfolioDbClient.queryWithParams(PortfolioSQLQuery.UPDATE_PROJECT, new JsonArray().add(uuidList.toString()).add(projectName), fetch ->{

                if(fetch.succeeded())
                {
                    message.reply(ReplyHandler.getOkReply());
                }
                else {
                    message.reply(ReplyHandler.reportDatabaseQueryError(fetch.cause()));
                }
            });
        }
        else
        {
            message.reply(ReplyHandler.reportUserDefinedError(projectNameExistMessage));
        }

    }

    public static void updateUUIDList(List<Integer> newUUIDList)
    {
        String projectName = SelectorHandler.getProjectNameBuffer();

        portfolioDbClient.queryWithParams(PortfolioSQLQuery.GET_PROJECT_UUID_LIST,  new JsonArray().add(projectName), fetch ->{

            if(fetch.succeeded())
            {
                String UUIDString = fetch.result().getResults().get(0).getString(0);

                List<Integer> UUIDListString = ConversionHandler.string2IntegerList(UUIDString);

                UUIDListString.addAll(newUUIDList);

                JsonArray jsonUpdateBody = new JsonArray().add(UUIDListString.toString()).add(projectName);

                portfolioDbClient.queryWithParams(PortfolioSQLQuery.UPDATE_PROJECT, jsonUpdateBody, reply -> {

                    if(!reply.succeeded())
                    {
                        log.error("Update list of uuids to Portfolio Database failed");
                    }
                });

            }
            else {
                log.error("Retrieving list of uuids from Portfolio Database failed");
            }
        });
    }

    public void configurePortfolioVerticle()
    {
        portfolioDbClient.query(PortfolioSQLQuery.GET_PROJECT_ID_LIST, fetch -> {
            if (fetch.succeeded()) {

                List<Integer> projectIDList = fetch.result()
                        .getResults()
                        .stream()
                        .map(json -> json.getInteger(0))
                        .sorted()
                        .collect(Collectors.toList());

                if(projectIDList.isEmpty())
                {
                    log.info("Project ID List is empty. Initiate generator id from 0");
                    SelectorHandler.setProjectIDGenerator(0);
                }
                else {
                    Integer maxProjectID = Collections.max(projectIDList);
                    SelectorHandler.setProjectIDGenerator(maxProjectID);

                    //set projectIDNameDict and projectNameIDDict in SelectorHandler
                    for (Integer projectID : projectIDList)
                    {
                        JsonArray projectIDJson = new JsonArray().add(projectID);

                        portfolioDbClient.queryWithParams(PortfolioSQLQuery.GET_PROJECT_NAME, projectIDJson, projectNameFetch -> {

                            if (projectNameFetch.succeeded()) {
                                ResultSet resultSet = projectNameFetch.result();

                                if (resultSet.getNumRows() != 0) {

                                    JsonArray row = resultSet.getResults().get(0);

                                    String projectName = row.getString(0);

                                    SelectorHandler.setProjectNameNID(projectName, projectID);

                                    JsonArray thumbnailIDJson = new JsonArray().add(0).add(projectName);

                                    portfolioDbClient.queryWithParams(PortfolioSQLQuery.UPDATE_THUMBNAIL_MAX_INDEX, thumbnailIDJson, thumbnailFetch -> {

                                        if (!thumbnailFetch.succeeded()) {
                                            log.error("Fail in updating thumbnail max in tutorial");
                                        }
                                    });
                                }
                            } else {
                                log.error("Retrieving project name failed");
                            }
                        });
                    }
                }

            }
        });
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
                    connection.execute(PortfolioSQLQuery.CREATE_PORTFOLIO_TABLE, create -> {
                        connection.close();

                        if (create.succeeded()) {

                            //the consumer methods registers an event bus destination handler
                            vertx.eventBus().consumer(PortfolioSQLQuery.QUEUE, this::onMessage);

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
