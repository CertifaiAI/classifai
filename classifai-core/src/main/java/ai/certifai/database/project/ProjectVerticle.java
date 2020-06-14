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

package ai.certifai.database.project;

import ai.certifai.database.DatabaseConfig;
import ai.certifai.util.ConversionHandler;
import ai.certifai.util.message.ErrorCodes;
import ai.certifai.selector.SelectorHandler;
import ai.certifai.server.ServerConfig;
import ai.certifai.util.ImageUtils;
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
import org.apache.commons.lang3.tuple.Pair;
import org.hsqldb.Server;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


@Slf4j
public class ProjectVerticle extends AbstractVerticle implements ProjectServiceable
{
    //connection to database
    private static JDBCClient projectJDBCClient;

    public void onMessage(Message<JsonObject> message) {

        if (!message.headers().contains(ServerConfig.ACTION_KEYWORD))
        {
            log.error("No action header specified for message with headers {} and body {}",
                    message.headers(), message.body().encodePrettily());

            message.fail(ErrorCodes.NO_ACTION_SPECIFIED.ordinal(), "No keyword " + ServerConfig.ACTION_KEYWORD + " specified");
            return;
        }
        String action = message.headers().get(ServerConfig.ACTION_KEYWORD);

        if(action.equals(ProjectSQLQuery.RETRIEVE_DATA))
        {
            this.retrieveData(message);
        }
        else if(action.equals(ProjectSQLQuery.RETRIEVE_DATA_PATH))
        {
            this.retrieveDataPath(message);
        }
        else if(action.equals(ProjectSQLQuery.UPDATE_DATA))
        {
            this.updateData(message);
        }
        else if (action.equals(ProjectSQLQuery.RECOVER_DATA))
        {
            this.sanityCheckDataPath(message);
        }
        else
        {
            log.error("Project query error: Action did not found follow up with function");
        }
    }


    public void retrieveDataPath(Message<JsonObject> message)
    {
        String projectName = message.body().getString(ServerConfig.PROJECT_NAME_PARAM);
        Integer uuid = message.body().getInteger(ServerConfig.UUID_PARAM);

        JsonArray params = new JsonArray().add(uuid).add(SelectorHandler.getProjectID(projectName));

        projectJDBCClient.queryWithParams(ProjectSQLQuery.RETRIEVE_DATA_PATH, params, fetch -> {
            if(fetch.succeeded())
            {
                ResultSet resultSet = fetch.result();
                JsonObject response = new JsonObject();

                if (resultSet.getNumRows() == 0) {
                    response = null;
                }
                else
                {
                    JsonArray row = resultSet.getResults().get(0);

                    String imagePath = row.getString(0);

                    response.put(ServerConfig.IMAGE_SRC_PARAM, ImageUtils.encodeFileToBase64Binary(new File(imagePath)));

                }
                message.reply(response);
            }
            else {
                message.reply(ReplyHandler.reportDatabaseQueryError(fetch.cause()));
            }
        });

    }

    public static List<Integer> updateUUIDList(List<File> fileHolder, List<Integer> UUIDList)
    {
        if(fileHolder.size() != UUIDList.size())
        {
            log.error("Number of files is not aligned to number of uuid");
            return null;
        }

        Integer currentProjectID = SelectorHandler.getProjectIDFromBuffer();

        List<Integer> indexToRemove = new ArrayList<>();

        for(int i = 0 ; i < fileHolder.size(); ++i)
        {
            SelectorHandler.setCurrentProcessingUUID(i + 1);

            Pair imgMetadata = ImageUtils.getImageSize(fileHolder.get(i));

            if(imgMetadata != null)
            {
                JsonArray params = new JsonArray()
                        .add(UUIDList.get(i)) //uuid
                        .add(currentProjectID) //projectid
                        .add(fileHolder.get(i).getAbsolutePath()) //imgpath
                        .add(new JsonArray().toString()) //new ArrayList<Integer>()
                        .add(0) //imgX
                        .add(0) //imgY
                        .add(0) //imgW
                        .add(0) //imgH
                        .add((Integer)imgMetadata.getLeft())
                        .add((Integer)imgMetadata.getRight());


                projectJDBCClient.queryWithParams(ProjectSQLQuery.CREATE_DATA, params, fetch -> {
                    if(!fetch.succeeded())
                    {
                        log.error("Update metadata in database failed: " + fetch.cause().getMessage());
                    }
                });
            }
            else
            {
                indexToRemove.add(i);
            }
        }

        Integer counter = 0; //counter to remove index remove

        for(Integer item : indexToRemove)
        {
            UUIDList.remove(item - counter);
            ++counter;
        }

        return UUIDList;
    }

    /*
    GET http://localhost:8080/retrievedata/:uuid
    */
    public void retrieveData(Message<JsonObject> message)
    {
        String projectName = message.body().getString(ServerConfig.PROJECT_NAME_PARAM);
        Integer uuid = message.body().getInteger(ServerConfig.UUID_PARAM);

        JsonArray params = new JsonArray().add(uuid).add(SelectorHandler.getProjectID(projectName));

        projectJDBCClient.queryWithParams(ProjectSQLQuery.RETRIEVE_DATA, params, fetch -> {

            if(fetch.succeeded())
            {
                ResultSet resultSet = fetch.result();
                JsonObject response = new JsonObject();

                if (resultSet.getNumRows() == 0) {
                    response = null;
                }
                else
                {
                    JsonArray row = resultSet.getResults().get(0);

                    Integer counter = 0;
                    String imagePath = row.getString(counter++);

                    response.put(ServerConfig.UUID_PARAM, uuid);
                    response.put(ServerConfig.PROJECT_NAME_PARAM, projectName);

                    response.put(ServerConfig.IMAGE_PATH_PARAM, imagePath);
                    response.put(ServerConfig.BOUNDING_BOX_PARAM, new JsonArray(row.getString(counter++)));
                    response.put(ServerConfig.IMAGEX_PARAM, row.getInteger(counter++));
                    response.put(ServerConfig.IMAGEY_PARAM, row.getInteger(counter++));
                    response.put(ServerConfig.IMAGEW_PARAM, row.getDouble(counter++));
                    response.put(ServerConfig.IMAGEH_PARAM, row.getDouble(counter++));
                    response.put(ServerConfig.IMAGEORIW_PARAM, row.getInteger(counter++));
                    response.put(ServerConfig.IMAGEORIH_PARAM, row.getInteger(counter++));
                    response.put(ServerConfig.IMAGE_THUMBNAIL_PARAM, ImageUtils.getThumbNail(imagePath));

                    response.put(ReplyHandler.getMessageKey(), ReplyHandler.getSuccessfulSignal());

                }
                message.reply(response);
            }
            else {
                message.reply(ReplyHandler.reportDatabaseQueryError(fetch.cause()));
            }
        });
    }

    public void sanityCheckDataPath(Message<JsonObject> message)
    {
        JsonObject requestBody = message.body();

        JsonArray params = new JsonArray().add(requestBody.getInteger(ServerConfig.UUID_PARAM))
                                            .add(requestBody.getInteger(ServerConfig.PROJECT_ID_PARAM));

        projectJDBCClient.queryWithParams(ProjectSQLQuery.RETRIEVE_DATA_PATH, params, fetch -> {

            if(fetch.succeeded()) {
                ResultSet resultSet = fetch.result();

                if (resultSet.getNumRows() != 0) {

                    String imagePath = resultSet.getResults().get(0).getString(0);

                    File imageFilePath = new File(imagePath);

                    if (imageFilePath.exists() && (ImageUtils.getImageSize(imageFilePath) != null))
                    {
                        message.reply(ReplyHandler.getOkReply());
                    }
                    else {

                        //this uuid has to be remove
                        projectJDBCClient.queryWithParams(ProjectSQLQuery.DELETE_DATA, params, reply -> {
                            if (!reply.succeeded()) {
                                log.error("Delete uuid failed. This should not happened");
                            }
                        });

                        message.reply(ReplyHandler.getFailedReply());
                    }
                }
                else
                {
                    message.reply(ReplyHandler.getFailedReply());
                }
            }
            else {
                log.error("Request uuid failed. This should not happened");
                message.reply(ReplyHandler.getFailedReply());
            }

        });
    }

    //PUT http://localhost:8080/updatedata
    public void updateData(Message<JsonObject> message)
    {
        JsonObject requestBody = message.body();
        
        String projectName = requestBody.getString(ServerConfig.PROJECT_NAME_PARAM);
        String boundingBox = requestBody.getJsonArray(ServerConfig.BOUNDING_BOX_PARAM).encode();

        JsonArray params = new JsonArray()
                .add(boundingBox)
                .add(requestBody.getInteger(ServerConfig.IMAGEX_PARAM))
                .add(requestBody.getInteger(ServerConfig.IMAGEY_PARAM))
                .add(requestBody.getDouble(ServerConfig.IMAGEW_PARAM))
                .add(requestBody.getDouble(ServerConfig.IMAGEH_PARAM))
                .add(requestBody.getInteger(ServerConfig.IMAGEORIW_PARAM))
                .add(requestBody.getInteger(ServerConfig.IMAGEORIH_PARAM))
                .add(requestBody.getInteger(ServerConfig.UUID_PARAM))
                .add(SelectorHandler.getProjectID(projectName));

        projectJDBCClient.queryWithParams(ProjectSQLQuery.UPDATE_DATA, params, fetch -> {
            if(fetch.succeeded())
            {
                message.reply(ReplyHandler.getOkReply());
            }
            else {
                message.reply(ReplyHandler.reportDatabaseQueryError(fetch.cause()));
            }
        });
    }

    //obtain a JDBC client connection,
    //Performs a SQL query to create the pages table unless it already existed
    @Override
    public void start(Promise<Void> promise) throws Exception
    {
        projectJDBCClient = JDBCClient.create(vertx, new JsonObject()
                .put("url", "jdbc:hsqldb:file:" + DatabaseConfig.PROJECT_DB)
                .put("driver_class", "org.hsqldb.jdbcDriver")
                .put("max_pool_size", 30));

        projectJDBCClient.getConnection(ar -> {
            if (ar.failed()) {
                log.error("Could not open a database connection", ar.cause());
                promise.fail(ar.cause());
            } else {
                SQLConnection connection = ar.result();
                connection.execute(ProjectSQLQuery.CREATE_PROJECT, create -> {
                    connection.close();
                    if (create.failed()) {
                        log.error("Project database preparation error", create.cause());
                        promise.fail(create.cause());

                    } else
                    {
                        log.info("Project database connection success");

                        //the consumer methods registers an event bus destination handler
                        vertx.eventBus().consumer(ProjectSQLQuery.QUEUE, this::onMessage);
                        promise.complete();
                    }
                });
            }
        });
    }
}
