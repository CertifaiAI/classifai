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
package ai.classifai.database.annotation;

import ai.classifai.database.VerticleServiceable;
import ai.classifai.database.portfolio.PortfolioVerticle;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.ProjectHandler;
import ai.classifai.util.collection.ConversionHandler;
import ai.classifai.util.data.ImageHandler;
import ai.classifai.util.message.ReplyHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
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
        String projectID = message.body().getString(ParamConfig.getProjectIdParam());
        String uuid = message.body().getString(ParamConfig.getUuidParam());

        JsonArray params = new JsonArray().add(uuid).add(projectID);

            jdbcClient.queryWithParams(query, params, fetch -> {

                if (fetch.succeeded())
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
                        response.put(ParamConfig.getImgSrcParam(), ImageHandler.encodeFileToBase64Binary(new File(imagePath)));
                        message.reply(response);
                    }
                }
                else
                {
                    message.reply(ReplyHandler.reportDatabaseQueryError(fetch.cause()));
                }
            });
    }

    public void loadValidProjectUUID(Message<JsonObject> message, @NonNull JDBCClient jdbcClient, @NonNull String query)
    {
        String projectID  = message.body().getString(ParamConfig.getProjectIdParam());

        ProjectLoader loader = ProjectHandler.getProjectLoader(projectID);

        List<String> oriUUIDList = loader.getUuidListFromDatabase();

        message.reply(ReplyHandler.getOkReply());

        loader.setDbOriUUIDSize(oriUUIDList.size());
        System.out.println("setDbOriUUIDSize: " + oriUUIDList.size());

        for (int i = 0; i < oriUUIDList.size(); ++i)
        {
            final Integer currentLength = i + 1;
            final String UUID = oriUUIDList.get(i);

            JsonArray params = new JsonArray().add(projectID).add(UUID);

            jdbcClient.queryWithParams(query, params, fetch -> {

                if (fetch.succeeded())
                {
                    ResultSet resultSet = fetch.result();
                    JsonArray row = resultSet.getResults().get(0);
                    String dataPath = row.getString(0);

                    if (ImageHandler.isImageReadable(dataPath))
                    {
                        loader.pushDBValidUUID(UUID);
                    }

                    loader.updateDBLoadingProgress(currentLength);
                }

            });
        }
    }

    public static void updateUUID(@NonNull JDBCClient jdbcClient, @NonNull String query, @NonNull String projectID, @NonNull File file, @NonNull String UUID, @NonNull Integer currentProcessedLength)
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

        jdbcClient.queryWithParams(query, params, fetch -> {

            ProjectLoader loader = ProjectHandler.getProjectLoader(projectID);
            if (fetch.succeeded())
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

    public void deleteProjectUUIDListwithProjectID(Message<JsonObject> message, @NonNull JDBCClient jdbcClient, @NonNull String query)
    {
        String projectID = message.body().getString(ParamConfig.getProjectIdParam());

        JsonArray params = new JsonArray().add(projectID);

        jdbcClient.queryWithParams(query, params, fetch -> {

            if (fetch.succeeded())
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

    public void deleteProjectUUIDList(Message<JsonObject> message, @NonNull JDBCClient jdbcClient, @NonNull String query)
    {
        String projectID =  message.body().getString(ParamConfig.getProjectIdParam());
        JsonArray UUIDListJsonArray =  message.body().getJsonArray(ParamConfig.getUuidListParam());

        List<String> oriUUIDList = ConversionHandler.jsonArray2StringList(UUIDListJsonArray);

        List<String> successUUIDList = new ArrayList<>();
        List<String> failedUUIDList = new ArrayList<>();

        ProjectLoader loader = ProjectHandler.getProjectLoader(projectID);
        List<String> dbUUIDList = loader.getUuidListFromDatabase();

        for (String UUID : oriUUIDList)
        {
            if (dbUUIDList.contains(UUID))
            {
                JsonArray params = new JsonArray().add(projectID).add(UUID);

                successUUIDList.add(UUID);

                jdbcClient.queryWithParams(query, params, fetch -> {

                    if (!fetch.succeeded())
                    {
                        log.debug("Failure in deleting uuid " + UUID + " in project " + projectID);
                    }
                });
            }
            else
            {
                failedUUIDList.add(UUID);
            }
        }

        String deleteUUIDListQuery = query + "(" + String.join(",", successUUIDList) + ")";

        JsonArray params = new JsonArray().add(projectID);

        jdbcClient.queryWithParams(deleteUUIDListQuery, params, fetch -> {

            if (!fetch.succeeded())
            {
                log.debug("Failure in deleting uuids in project " + projectID);
            }
        });

        if (dbUUIDList.removeAll(successUUIDList))
        {
            loader.setUuidListFromDatabase(dbUUIDList);

            List<String> sanityUUIDList = loader.getSanityUUIDList();
            if (sanityUUIDList.removeAll(successUUIDList))
            {
                loader.setSanityUUIDList(sanityUUIDList);
            }
            else
            {
                log.info("Error in removing uuid list");
            }

            //update Portfolio Verticle
            PortfolioVerticle.updateFileSystemUUIDList(projectID);

            message.reply(ReplyHandler.getOkReply().put(ParamConfig.getUuidListParam(), failedUUIDList));
        }
        else
        {
            message.reply(ReplyHandler.reportUserDefinedError("Failed to remove uuid from Portfolio Verticle. Project not expected to work fine"));
        }
    }

    public void updateData(Message<JsonObject> message, @NonNull JDBCClient jdbcClient, @NonNull String query)
    {
        JsonObject requestBody = message.body();

        try
        {
            String projectID = requestBody.getString(ParamConfig.getProjectIdParam());

            String annotationContent = requestBody.getJsonArray(ParamConfig.getAnnotationParam()).encode();

            JsonArray params = new JsonArray()
                    .add(annotationContent)
                    .add(requestBody.getInteger(ParamConfig.getImgDepth()))
                    .add(requestBody.getInteger(ParamConfig.getImgXParam()))
                    .add(requestBody.getInteger(ParamConfig.getImgYParam()))
                    .add(requestBody.getDouble(ParamConfig.getImgWParam()))
                    .add(requestBody.getDouble(ParamConfig.getImgHParam()))
                    .add(requestBody.getInteger(ParamConfig.getFileSizeParam()))
                    .add(requestBody.getInteger(ParamConfig.getImgOriWParam()))
                    .add(requestBody.getInteger(ParamConfig.getImgOriHParam()))
                    .add(requestBody.getString(ParamConfig.getUuidParam()))
                    .add(projectID);


            jdbcClient.queryWithParams(query, params, fetch -> {

                if (fetch.succeeded())
                {
                    message.reply(ReplyHandler.getOkReply());
                }
                else
                {
                    message.reply(ReplyHandler.reportDatabaseQueryError(fetch.cause()));
                }
            });
        }
        catch (Exception e)
        {
            log.info("AnnotationVerticle: " + message.body().toString());
            String messageInfo = "Error occur when updating data, " + e;
            message.reply(ReplyHandler.reportBadParamError(messageInfo));
        }
    }

    public void retrieveData(Message<JsonObject> message, @NonNull JDBCClient jdbcClient, @NonNull String query)
    {
        String projectName =  message.body().getString(ParamConfig.getProjectNameParam());
        String projectID =  message.body().getString(ParamConfig.getProjectIdParam());
        String uuid = message.body().getString(ParamConfig.getUuidParam());

        JsonArray params = new JsonArray().add(uuid).add(projectID);

        jdbcClient.queryWithParams(query, params, fetch -> {

            if (fetch.succeeded())
            {
                ResultSet resultSet = fetch.result();

                if (resultSet.getNumRows() == 0)
                {
                    log.info("Project id: " + params.getInteger(1));

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

                    response.put(ParamConfig.getUuidParam(), uuid);
                    response.put(ParamConfig.getProjectNameParam(), projectName);

                    response.put(ParamConfig.getImgPathParam(), dataPath);
                    response.put(ParamConfig.getAnnotationParam(), new JsonArray(row.getString(counter++)));
                    response.put(ParamConfig.getImgDepth(),  Integer.parseInt(imgData.get(ParamConfig.getImgDepth())));
                    response.put(ParamConfig.getImgXParam(), row.getInteger(counter++));
                    response.put(ParamConfig.getImgYParam(), row.getInteger(counter++));
                    response.put(ParamConfig.getImgWParam(), row.getDouble(counter++));
                    response.put(ParamConfig.getImgHParam(), row.getDouble(counter++));
                    response.put(ParamConfig.getFileSizeParam(), row.getInteger(counter));
                    response.put(ParamConfig.getImgOriWParam(), Integer.parseInt(imgData.get(ParamConfig.getImgOriWParam())));
                    response.put(ParamConfig.getImgOriHParam(), Integer.parseInt(imgData.get(ParamConfig.getImgOriHParam())));
                    response.put(ParamConfig.getImgThumbnailParam(), imgData.get(ParamConfig.getBase64Param()));
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