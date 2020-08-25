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

package ai.classifai.database.boundingboxdb;

import ai.classifai.database.DatabaseConfig;
import ai.classifai.database.loader.LoaderStatus;
import ai.classifai.database.loader.ProjectLoader;
import ai.classifai.selector.SelectorHandler;
import ai.classifai.server.ParamConfig;
import ai.classifai.util.ConversionHandler;
import ai.classifai.util.image.ImageHandler;
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
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Bounding Box Verticle
 *
 * @author Chiawei Lim
 */
@Slf4j
public class BoundingBoxVerticle extends AbstractVerticle implements BoundingBoxDbServiceable
{
    //connection to database
    private static JDBCClient projectJDBCClient;

    public void onMessage(Message<JsonObject> message) {

        if (!message.headers().contains(ParamConfig.ACTION_KEYWORD))
        {
            log.error("No action header specified for message with headers {} and body {}",
                    message.headers(), message.body().encodePrettily());

            message.fail(ErrorCodes.NO_ACTION_SPECIFIED.ordinal(), "No keyword " + ParamConfig.ACTION_KEYWORD + " specified");
            return;
        }
        String action = message.headers().get(ParamConfig.ACTION_KEYWORD);

        if(action.equals(BoundingBoxDbQuery.RETRIEVE_DATA))
        {
            this.retrieveData(message);
        }
        else if(action.equals(BoundingBoxDbQuery.RETRIEVE_DATA_PATH))
        {
            this.retrieveDataPath(message);
        }
        else if(action.equals(BoundingBoxDbQuery.UPDATE_DATA))
        {
            this.updateData(message);
        }
        else if (action.equals(BoundingBoxDbQuery.LOAD_VALID_PROJECT_UUID))
        {
            this.loadValidProjectUUID(message);
        }
        else
        {
            log.error("Project query error: Action did not found follow up with function");
        }
    }


    public void retrieveDataPath(Message<JsonObject> message)
    {
        String projectName = message.body().getString(ParamConfig.PROJECT_NAME_PARAM);
        Integer uuid = message.body().getInteger(ParamConfig.UUID_PARAM);

        JsonArray params = new JsonArray().add(uuid).add(SelectorHandler.getProjectID(projectName));

        projectJDBCClient.queryWithParams(BoundingBoxDbQuery.RETRIEVE_DATA_PATH, params, fetch -> {

            if(fetch.succeeded())
            {
                ResultSet resultSet = fetch.result();

                if (resultSet.getNumRows() == 0)
                {
                    message.reply(ReplyHandler.reportUserDefinedError("Image Data Path not found"));
                }
                else
                {
                    JsonObject response = ReplyHandler.getOkReply();

                    JsonArray row = resultSet.getResults().get(0);

                    String imagePath = row.getString(0);

                    response.put(ParamConfig.IMAGE_SRC_PARAM, ImageHandler.encodeFileToBase64Binary(new File(imagePath)));

                    message.reply(response);

                }
            }
            else {
                message.reply(ReplyHandler.reportDatabaseQueryError(fetch.cause()));
            }
        });
    }

    public static void updateUUID(List<Integer> uuidList, File file, Integer UUID)
    {
        Map imgMetadata = ImageHandler.getImageMetadata(file);

        if(imgMetadata != null)
        {
            JsonArray params = new JsonArray()
                    .add(UUID) //uuid
                    .add(SelectorHandler.getProjectIDFromBuffer()) //projectid
                    .add(file.getAbsolutePath()) //imgpath
                    .add(new JsonArray().toString()) //new ArrayList<Integer>()
                    .add((Integer)imgMetadata.get("depth")) //imgDepth
                    .add(0) //imgX
                    .add(0) //imgY
                    .add(0) //imgW
                    .add(0) //imgH
                    .add((Integer)imgMetadata.get("width"))
                    .add((Integer)imgMetadata.get("height"));

            projectJDBCClient.queryWithParams(BoundingBoxDbQuery.CREATE_DATA, params, fetch ->
            {
                if(fetch.succeeded())
                {
                    uuidList.add(UUID);
                }
                else
                {
                    log.error("Update metadata in database failed: " + fetch.cause().getMessage());
                }
            });
        }
    }

    /*
    GET http://localhost:{port}/retrievedata/:uuid
    */
    public void retrieveData(Message<JsonObject> message)
    {
        String projectName = message.body().getString(ParamConfig.PROJECT_NAME_PARAM);
        Integer uuid = message.body().getInteger(ParamConfig.UUID_PARAM);

        JsonArray params = new JsonArray().add(uuid).add(SelectorHandler.getProjectID(projectName));

        projectJDBCClient.queryWithParams(BoundingBoxDbQuery.RETRIEVE_DATA, params, fetch -> {

            if(fetch.succeeded())
            {
                ResultSet resultSet = fetch.result();

                if (resultSet.getNumRows() == 0)
                {
                    log.debug("Should not get null");
                    message.reply(ReplyHandler.reportUserDefinedError("Database query to retrieve thumbnail uuid did not found"));
                }
                else
                {
                    JsonArray row = resultSet.getResults().get(0);

                    Integer counter = 0;
                    String dataPath = row.getString(counter++);

                    String thumbnail = ImageHandler.getThumbNail(dataPath);

                    JsonObject response = ReplyHandler.getOkReply();

                    response.put(ParamConfig.UUID_PARAM, uuid);
                    response.put(ParamConfig.PROJECT_NAME_PARAM, projectName);

                    response.put(ParamConfig.IMAGE_PATH_PARAM, dataPath);
                    response.put(ParamConfig.BOUNDING_BOX_PARAM, new JsonArray(row.getString(counter++)));
                    response.put(ParamConfig.IMAGE_DEPTH, row.getInteger(counter++));
                    response.put(ParamConfig.IMAGEX_PARAM, row.getInteger(counter++));
                    response.put(ParamConfig.IMAGEY_PARAM, row.getInteger(counter++));
                    response.put(ParamConfig.IMAGEW_PARAM, row.getDouble(counter++));
                    response.put(ParamConfig.IMAGEH_PARAM, row.getDouble(counter++));
                    response.put(ParamConfig.IMAGEORIW_PARAM, row.getInteger(counter++));
                    response.put(ParamConfig.IMAGEORIH_PARAM, row.getInteger(counter++));
                    response.put(ParamConfig.IMAGE_THUMBNAIL_PARAM, thumbnail);
                    message.reply(response);
                }

            }
            else {
                message.reply(ReplyHandler.reportUserDefinedError("Database query to retrieve thumbnail uuid failed"));
            }
        });
    }


    public void loadValidProjectUUID(Message<JsonObject> message)
    {
        String projectName = message.body().getString(ParamConfig.PROJECT_NAME_PARAM);
        Integer projectID  = SelectorHandler.getProjectID(projectName);

        JsonArray uuidListArray = message.body().getJsonArray(ParamConfig.UUID_LIST_PARAM);
        List<Integer> oriUUIDList = ConversionHandler.jsonArray2IntegerList(uuidListArray);

        ProjectLoader loader = SelectorHandler.getProjectLoader(projectName);

        message.reply(ReplyHandler.getOkReply());

        loader.setLoaderStatus(LoaderStatus.LOADING);
        loader.setTotalUUIDSize(oriUUIDList.size());

        for(int i = 0; i < oriUUIDList.size(); ++i)
        {
            final Integer currentLength = i;
            final Integer UUID = oriUUIDList.get(i);
            JsonArray params = new JsonArray().add(UUID).add(projectID);

            projectJDBCClient.queryWithParams(BoundingBoxDbQuery.RETRIEVE_DATA, params, fetch -> {

                if (fetch.succeeded())
                {
                    ResultSet resultSet = fetch.result();

                    if (resultSet.getNumRows() != 0)
                    {
                        JsonArray row = resultSet.getResults().get(0);

                        String dataPath = row.getString(0);

                        if(ImageHandler.isImageReadable(dataPath)) loader.pushToUUIDSet(UUID);
                    }
                    loader.updateProgress(currentLength + 1);
                }
                else
                {
                    loader.updateProgress(currentLength + 1);
                }
            });
        }
    }


    //PUT http://localhost:{port}/updatedata
    public void updateData(Message<JsonObject> message)
    {
        JsonObject requestBody = message.body();
        
        String projectName = requestBody.getString(ParamConfig.PROJECT_NAME_PARAM);
        String boundingBox = requestBody.getJsonArray(ParamConfig.BOUNDING_BOX_PARAM).encode();

        JsonArray params = new JsonArray()
                .add(boundingBox)
                .add(requestBody.getInteger(ParamConfig.IMAGEX_PARAM))
                .add(requestBody.getInteger(ParamConfig.IMAGEY_PARAM))
                .add(requestBody.getDouble(ParamConfig.IMAGEW_PARAM))
                .add(requestBody.getDouble(ParamConfig.IMAGEH_PARAM))
                .add(requestBody.getInteger(ParamConfig.IMAGEORIW_PARAM))
                .add(requestBody.getInteger(ParamConfig.IMAGEORIH_PARAM))
                .add(requestBody.getInteger(ParamConfig.UUID_PARAM))
                .add(SelectorHandler.getProjectID(projectName));


        projectJDBCClient.queryWithParams(BoundingBoxDbQuery.UPDATE_DATA, params, fetch -> {
            if(fetch.succeeded())
            {
                message.reply(ReplyHandler.getOkReply());
            }
            else {
                message.reply(ReplyHandler.reportDatabaseQueryError(fetch.cause()));
            }
        });
    }

    @Override
    public void stop(Promise<Void> promise) throws Exception
    {
        log.info("Bounding Box Verticle stopping...");

        File lockFile = new File(DatabaseConfig.BNDBOX_DB_LCKFILE);

        if(lockFile.exists()) lockFile.delete();
    }

    //obtain a JDBC client connection,
    //Performs a SQL query to create the pages table unless it already existed
    @Override
    public void start(Promise<Void> promise) throws Exception
    {
        projectJDBCClient = JDBCClient.create(vertx, new JsonObject()
                .put("url", "jdbc:hsqldb:file:" + DatabaseConfig.BNDBOX_DB)
                .put("driver_class", "org.hsqldb.jdbcDriver")
                .put("max_pool_size", 30));


        projectJDBCClient.getConnection(ar -> {
            if (ar.failed()) {

                log.error("Could not open a database connection", ar.cause());
                promise.fail(ar.cause());

            } else {
                SQLConnection connection = ar.result();
                connection.execute(BoundingBoxDbQuery.CREATE_PROJECT, create -> {
                    connection.close();
                    if (create.failed()) {
                        log.error("BoundingBoxVerticle database preparation error", create.cause());
                        promise.fail(create.cause());

                    } else
                    {
                        //the consumer methods registers an event bus destination handler
                        vertx.eventBus().consumer(BoundingBoxDbQuery.QUEUE, this::onMessage);
                        promise.complete();
                    }
                });
            }
        });

    }
}
