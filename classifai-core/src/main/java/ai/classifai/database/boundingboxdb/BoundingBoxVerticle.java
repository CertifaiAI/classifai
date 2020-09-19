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
import ai.classifai.database.loader.ProjectLoader;
import ai.classifai.server.ParamConfig;
import ai.classifai.util.ConversionHandler;
import ai.classifai.util.ProjectHandler;
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
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Collections;
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
            log.error("Bounding Box Verticle query error. Action did not have an assigned function for handling.");
        }
    }


    public void retrieveDataPath(Message<JsonObject> message)
    {
        Integer projectID = message.body().getInteger(ParamConfig.PROJECT_ID_PARAM);
        Integer uuid = message.body().getInteger(ParamConfig.UUID_PARAM);

        JsonArray params = new JsonArray().add(uuid).add(projectID);

        projectJDBCClient.queryWithParams(BoundingBoxDbQuery.RETRIEVE_DATA_PATH, params, fetch -> {

            if(fetch.succeeded())
            {
                ResultSet resultSet = fetch.result();

                if (resultSet.getNumRows() == 0)
                {
                    message.reply(ReplyHandler.reportUserDefinedError("Image data path not found"));
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

    public static void  updateUUID(@NonNull Integer projectID, @NonNull File file, @NonNull Integer UUID, @NonNull Integer currentProcessedLength)
    {
        JsonArray params = new JsonArray()
                .add(UUID) //uuid
                .add(projectID) //projectid
                .add(file.getAbsolutePath()) //imgpath
                .add(new JsonArray().toString()) //new ArrayList<Integer>()
                .add(0) //img_depth
                .add(0) //imgX
                .add(0) //imgY
                .add(0) //imgW
                .add(0) //imgH
                .add(0) //file_size
                .add(0)
                .add(0);

        projectJDBCClient.queryWithParams(BoundingBoxDbQuery.CREATE_DATA, params, fetch ->
        {
            ProjectLoader loader = ProjectHandler.getProjectLoader(projectID);
            if(fetch.succeeded())
            {
                loader.pushFileSysNewUUIDList(UUID);
            }
            else
            {
                log.error("Push data point with path " + file.getAbsolutePath() + " failed: " + fetch.cause().getMessage());
            }

            loader.updateFileSysLoadingProgress(currentProcessedLength);
        });

    }


    /*
    GET http://localhost:{port}/retrievedata/:uuid
    */
    public void retrieveData(Message<JsonObject> message)
    {
        String projectName =  message.body().getString(ParamConfig.PROJECT_NAME_PARAM);
        Integer projectID =  message.body().getInteger(ParamConfig.PROJECT_ID_PARAM);
        Integer uuid = message.body().getInteger(ParamConfig.UUID_PARAM);

        JsonArray params = new JsonArray().add(uuid).add(projectID);

        projectJDBCClient.queryWithParams(BoundingBoxDbQuery.RETRIEVE_DATA, params, fetch -> {

            if(fetch.succeeded())
            {
                ResultSet resultSet = fetch.result();

                if (resultSet.getNumRows() == 0)
                {
                    String userDefinedMessage = "Data not found when retrieving for project " + projectName + " with uuid " + uuid;

                    message.reply(ReplyHandler.reportUserDefinedError(userDefinedMessage));
                }
                else
                {
                    JsonArray row = resultSet.getResults().get(0);

                    Integer counter = 0;
                    String dataPath = row.getString(counter++);

                    Map<String, String> imgData = ImageHandler.getThumbNail(dataPath);

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
                    response.put(ParamConfig.FILE_SIZE_PARAM, row.getInteger(counter++));
                    response.put(ParamConfig.IMAGEORIW_PARAM, Integer.parseInt(imgData.get(ParamConfig.IMAGEORIW_PARAM)));
                    response.put(ParamConfig.IMAGEORIH_PARAM, Integer.parseInt(imgData.get(ParamConfig.IMAGEORIH_PARAM)));
                    response.put(ParamConfig.IMAGE_THUMBNAIL_PARAM, imgData.get(ParamConfig.BASE64_PARAM));
                    message.reply(response);
                }

            }
            else {
                String userDefinedMessage = "Failure in data retrieval for project " + projectName + " with uuid " + uuid;
                message.reply(ReplyHandler.reportUserDefinedError(userDefinedMessage));
            }
        });
    }


    public void loadValidProjectUUID(Message<JsonObject> message)
    {
        Integer projectID  = message.body().getInteger(ParamConfig.PROJECT_ID_PARAM);
        JsonArray uuidListArray = message.body().getJsonArray(ParamConfig.UUID_LIST_PARAM);

        List<Integer> oriUUIDList = ConversionHandler.jsonArray2IntegerList(uuidListArray);

        ProjectLoader loader = ProjectHandler.getProjectLoader(projectID);

        message.reply(ReplyHandler.getOkReply());

        if(oriUUIDList.isEmpty())
        {
            loader.updateDBLoadingProgress(1);  // in order for loading process to be 100%
            return;
        }

        loader.setDbOriUUIDSize(oriUUIDList.size());

        //sanity check on seed and write on database if needed
        ProjectHandler.checkUUIDGeneratorSeedSanity(projectID, Collections.max(oriUUIDList), message.body().getInteger(ParamConfig.UUID_GENERATOR_PARAM));

        for(int i = 0; i < oriUUIDList.size(); ++i)
        {
            final Integer currentLength = i + 1;
            final Integer UUID = oriUUIDList.get(i);
            JsonArray params = new JsonArray().add(UUID).add(projectID);

            projectJDBCClient.queryWithParams(BoundingBoxDbQuery.RETRIEVE_DATA_PATH, params, fetch -> {

                if (fetch.succeeded()) {
                    ResultSet resultSet = fetch.result();

                    if (resultSet.getNumRows() != 0) {
                        JsonArray row = resultSet.getResults().get(0);

                        String dataPath = row.getString(0);

                        if (ImageHandler.isImageReadable(dataPath)) loader.pushDBValidUUID(UUID);
                    }
                }

                loader.updateDBLoadingProgress(currentLength);
            });
        }

    }


    //PUT http://localhost:{port}/updatedata
    public void updateData(Message<JsonObject> message)
    {
        JsonObject requestBody = message.body();

        try
        {
            Integer projectID = requestBody.getInteger(ParamConfig.PROJECT_ID_PARAM);

            String boundingBox = requestBody.getJsonArray(ParamConfig.BOUNDING_BOX_PARAM).encode();

            JsonArray params = new JsonArray()
                    .add(boundingBox)
                    .add(requestBody.getInteger(ParamConfig.IMAGE_DEPTH))
                    .add(requestBody.getInteger(ParamConfig.IMAGEX_PARAM))
                    .add(requestBody.getInteger(ParamConfig.IMAGEY_PARAM))
                    .add(requestBody.getDouble(ParamConfig.IMAGEW_PARAM))
                    .add(requestBody.getDouble(ParamConfig.IMAGEH_PARAM))
                    .add(requestBody.getInteger(ParamConfig.FILE_SIZE_PARAM))
                    .add(requestBody.getInteger(ParamConfig.IMAGEORIW_PARAM))
                    .add(requestBody.getInteger(ParamConfig.IMAGEORIH_PARAM))
                    .add(requestBody.getInteger(ParamConfig.UUID_PARAM))
                    .add(projectID);


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
        catch(Exception e)
        {
            log.info("BoundingBoxVerticle: " + message.body().toString());
            String messageInfo = "Error occur when updating data, " + e;
            message.reply(ReplyHandler.reportBadParamError(messageInfo));
        }
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
                log.error("Could not open a database connection for Bounding Box Verticle", ar.cause());
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
