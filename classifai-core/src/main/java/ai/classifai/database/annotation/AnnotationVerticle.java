/*
 * Copyright (c) 2020 CertifAI Sdn. Bhd.
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
package ai.classifai.database.annotation;

import ai.classifai.database.VerticleServiceable;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.ProjectHandler;
import ai.classifai.util.collection.ConversionHandler;
import ai.classifai.util.data.ImageHandler;
import ai.classifai.util.message.ReplyHandler;
import ai.classifai.util.type.AnnotationType;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Implementation of Functionalities for each annotation type
 *
 * @author codenamewei
 */
@Slf4j
public abstract class AnnotationVerticle extends AbstractVerticle implements VerticleServiceable, AnnotationServiceable
{
    public void retrieveDataPath(Message<JsonObject> message, @NonNull JDBCClient jdbcClient, @NonNull String query)
    {
        Integer projectID = message.body().getInteger(ParamConfig.getProjectIDParam());
        Integer uuid = message.body().getInteger(ParamConfig.getUUIDParam());

        JsonArray params = new JsonArray().add(uuid).add(projectID);

        jdbcClient.queryWithParams(query, params, fetch -> {
            if(fetch.succeeded())
            {
                ResultSet resultSet = fetch.result();

                if (resultSet.getNumRows() == 0)
                {
                    String projectName = message.body().getString(ParamConfig.getProjectNameParam());
                    String userDefinedMessage = "Failure in data path retrieval for project " + projectName + " with uuid " + uuid;
                    message.reply(ReplyHandler.reportUserDefinedError(userDefinedMessage));
                }
                else
                {
                    JsonObject response = ReplyHandler.getOkReply();

                    JsonArray row = resultSet.getResults().get(0);

                    String imagePath = row.getString(0);

                    response.put(ParamConfig.getImageSourceParam(), ImageHandler.encodeFileToBase64Binary(new File(imagePath)));

                    message.reply(response);

                }
            }
            else {
                message.reply(ReplyHandler.reportDatabaseQueryError(fetch.cause()));
            }
        });

    }


    public void loadValidProjectUUID(Message<JsonObject> message, @NonNull JDBCClient jdbcClient, @NonNull String query)
    {
        Integer projectID  = message.body().getInteger(ParamConfig.getProjectIDParam());
        JsonArray uuidListArray = message.body().getJsonArray(ParamConfig.getUUIDListParam());

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
        ProjectHandler.checkUUIDGeneratorSeedSanity(projectID, Collections.max(oriUUIDList), message.body().getInteger(ParamConfig.getUuidGeneratorParam()));

        for(int i = 0; i < oriUUIDList.size(); ++i)
        {
            final Integer currentLength = i + 1;
            final Integer UUID = oriUUIDList.get(i);
            JsonArray params = new JsonArray().add(projectID).add(UUID);

            jdbcClient.queryWithParams(query, params, fetch -> {

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

    public void deleteProjectUUIDList(Message<JsonObject> message, @NonNull JDBCClient jdbcClient, @NonNull String query)
    {
        Integer projectID =  message.body().getInteger(ParamConfig.getProjectIDParam());

        JsonArray params = new JsonArray().add(projectID);

        jdbcClient.queryWithParams(query, params, fetch -> {

            if(fetch.succeeded())
            {
                message.reply(ReplyHandler.getOkReply());
            }
            else
            {
                log.debug("Failure in deleting uuid list from Annotation Verticle");
                message.reply(ReplyHandler.reportDatabaseQueryError(fetch.cause()));
            }
        });
    }

    public void deleteProjectUUID(Message<JsonObject> message, @NonNull JDBCClient jdbcClient, @NonNull String query)
    {
        Integer projectID =  message.body().getInteger(ParamConfig.getProjectIDParam());

        Integer UUID =  message.body().getInteger(ParamConfig.getUUIDParam());

        JsonArray params = new JsonArray().add(projectID).add(UUID);

        jdbcClient.queryWithParams(query, params, fetch -> {

            if(fetch.succeeded())
            {
                message.reply(ReplyHandler.getOkReply());
            }
            else
            {
                log.debug("Failure in deleting uuid from Annotation Verticle");
                message.reply(ReplyHandler.reportDatabaseQueryError(fetch.cause()));
            }
        });
    }

    public static void updateUUID(@NonNull JDBCClient jdbcClient, @NonNull String query, @NonNull Integer projectID, @NonNull File file, @NonNull Integer UUID, @NonNull Integer currentProcessedLength)
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

        jdbcClient.queryWithParams(query, params, fetch ->
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

    public void updateData(Message<JsonObject> message, @NonNull JDBCClient jdbcClient, @NonNull String query, AnnotationType annotationType)
    {
        JsonObject requestBody = message.body();

        try
        {
            Integer projectID = requestBody.getInteger(ParamConfig.getProjectIDParam());

            String annotationContent = requestBody.getJsonArray(ParamConfig.getAnnotationParam(annotationType)).encode();

            JsonArray params = new JsonArray()
                    .add(annotationContent)
                    .add(requestBody.getInteger(ParamConfig.getImageDepth()))
                    .add(requestBody.getInteger(ParamConfig.getImageXParam()))
                    .add(requestBody.getInteger(ParamConfig.getImageYParam()))
                    .add(requestBody.getDouble(ParamConfig.getImageWParam()))
                    .add(requestBody.getDouble(ParamConfig.getImageHParam()))
                    .add(requestBody.getInteger(ParamConfig.getFileSizeParam()))
                    .add(requestBody.getInteger(ParamConfig.getImageORIWParam()))
                    .add(requestBody.getInteger(ParamConfig.getImageORIHParam()))
                    .add(requestBody.getInteger(ParamConfig.getUUIDParam()))
                    .add(projectID);


            jdbcClient.queryWithParams(query, params, fetch -> {
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
            log.info("AnnotationVerticle: " + message.body().toString());
            String messageInfo = "Error occur when updating data, " + e;
            message.reply(ReplyHandler.reportBadParamError(messageInfo));
        }
    }

    public void retrieveData(Message<JsonObject> message, @NonNull JDBCClient jdbcClient, @NonNull String query, AnnotationType annotationType)
    {

        String projectName =  message.body().getString(ParamConfig.getProjectNameParam());
        Integer projectID =  message.body().getInteger(ParamConfig.getProjectIDParam());
        Integer uuid = message.body().getInteger(ParamConfig.getUUIDParam());

        JsonArray params = new JsonArray().add(uuid).add(projectID);

        jdbcClient.queryWithParams(query, params, fetch -> {

            if(fetch.succeeded())
            {
                ResultSet resultSet = fetch.result();

                if (resultSet.getNumRows() == 0)
                {
                    log.info("Project id: " + params.getInteger(1));

                    String userDefinedMessage = "Data not found when retrieving for project " + projectName + " with uuid " + uuid;
                    message.reply(ReplyHandler.reportUserDefinedError(userDefinedMessage));
                }
                else {
                    JsonArray row = resultSet.getResults().get(0);

                    Integer counter = 0;
                    String dataPath = row.getString(counter++);

                    Map<String, String> imgData = ImageHandler.getThumbNail(dataPath);

                    JsonObject response = ReplyHandler.getOkReply();

                    response.put(ParamConfig.getUUIDParam(), uuid);
                    response.put(ParamConfig.getProjectNameParam(), projectName);

                    response.put(ParamConfig.getImagePathParam(), dataPath);
                    response.put(ParamConfig.getAnnotationParam(annotationType), new JsonArray(row.getString(counter++)));
                    response.put(ParamConfig.getImageDepth(),  Integer.parseInt(imgData.get(ParamConfig.getImageDepth())));
                    response.put(ParamConfig.getImageXParam(), row.getInteger(counter++));
                    response.put(ParamConfig.getImageYParam(), row.getInteger(counter++));
                    response.put(ParamConfig.getImageWParam(), row.getDouble(counter++));
                    response.put(ParamConfig.getImageHParam(), row.getDouble(counter++));
                    response.put(ParamConfig.getFileSizeParam(), row.getInteger(counter));
                    response.put(ParamConfig.getImageORIWParam(), Integer.parseInt(imgData.get(ParamConfig.getImageORIWParam())));
                    response.put(ParamConfig.getImageORIHParam(), Integer.parseInt(imgData.get(ParamConfig.getImageORIHParam())));
                    response.put(ParamConfig.getImageThumbnailParam(), imgData.get(ParamConfig.getBase64Param()));
                    message.reply(response);
                }
            }
            else
            {
                String userDefinedMessage = "Failure in data retrieval for project " + projectName + " with uuid " + uuid;
                message.reply(ReplyHandler.reportUserDefinedError(userDefinedMessage));
            }
        });
    }

}
