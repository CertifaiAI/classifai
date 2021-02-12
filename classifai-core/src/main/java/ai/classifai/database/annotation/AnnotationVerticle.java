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
import ai.classifai.util.type.AnnotationType;
import io.vertx.core.AbstractVerticle;
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
    public void retrieveDataPath(Message<JsonObject> message, @NonNull JDBCPool jdbcPool, @NonNull String query)
    {
        String projectID = message.body().getString(ParamConfig.getProjectIDParam());
        String uuid = message.body().getString(ParamConfig.getUUIDParam());

        Tuple params = Tuple.of(uuid, projectID);

        jdbcPool.preparedQuery(query)
                .execute(params)
                .onComplete(fetch -> {

                    if (fetch.succeeded())
                    {
                        RowSet<Row> rowSet = fetch.result();

                        if (rowSet.size() == 0)
                        {
                            String projectName = message.body().getString(ParamConfig.getProjectNameParam());
                            String userDefinedMessage = "Failure in data path retrieval for project " + projectName + " with uuid " + uuid;
                            message.replyAndRequest(ReplyHandler.reportUserDefinedError(userDefinedMessage));
                        }
                        else
                        {
                            JsonObject response = ReplyHandler.getOkReply();

                            for(Row row : rowSet){
                                String imagePath = row.getString(0);
                                response.put(ParamConfig.getImageSourceParam(), ImageHandler.encodeFileToBase64Binary(new File(imagePath)));
                                message.replyAndRequest(response);
                            }
                        }
                    }
                    else
                    {
                        message.replyAndRequest(ReplyHandler.reportDatabaseQueryError(fetch.cause()));
                    }
                });
    }

    public void loadValidProjectUUID(Message<JsonObject> message, @NonNull JDBCPool jdbcPool, @NonNull String query)
    {
        String projectID  = message.body().getString(ParamConfig.getProjectIDParam());

        ProjectLoader loader = ProjectHandler.getProjectLoader(projectID);

        List<String> oriUUIDList = loader.getUuidListFromDatabase();

        message.replyAndRequest(ReplyHandler.getOkReply());

        loader.setDbOriUUIDSize(oriUUIDList.size());

        for (int i = 0; i < oriUUIDList.size(); ++i)
        {
            final Integer currentLength = i + 1;
            final String UUID = oriUUIDList.get(i);

            Tuple params = Tuple.of(projectID, UUID);

            jdbcPool.preparedQuery(query)
                    .execute(params)
                    .onComplete(fetch -> {

                        if (fetch.succeeded())
                        {
                            RowSet<Row> rowSet = fetch.result();

                            for(Row row : rowSet){
                                String dataPath = row.getString(0);

                                if (ImageHandler.isImageReadable(dataPath))
                                {
                                    loader.pushDBValidUUID(UUID);
                                }
                            }
                        }
                        loader.updateDBLoadingProgress(currentLength);
                    });
        }
    }

    public static void updateUUID(@NonNull JDBCPool jdbcPool, @NonNull String query, @NonNull String projectID, @NonNull File file, @NonNull String UUID, @NonNull Integer currentProcessedLength)
    {
        Tuple params = Tuple.of(
                UUID, //uuid
                projectID, //projectid
                file.getAbsolutePath(), //imgpath
                new JsonArray().toString(), //new ArrayList<Integer>()
                0, //img_depth
                0, //imgX
                0, //imgY
                0, //imgW
                0, //imgH
                0, //file_size
                0,
                0);

        jdbcPool.preparedQuery(query)
                .execute(params)
                .onComplete(fetch -> {
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

    public void deleteProjectUUIDListwithProjectID(Message<JsonObject> message, @NonNull JDBCPool jdbcPool, @NonNull String query)
    {
        String projectID = message.body().getString(ParamConfig.getProjectIDParam());

        Tuple params = Tuple.of(projectID);

        jdbcPool.preparedQuery(query)
                .execute(params)
                .onComplete(fetch -> {
                    if (fetch.succeeded())
                    {
                        message.replyAndRequest(ReplyHandler.getOkReply());
                    }
                    else
                    {
                        log.debug("Failure in deleting uuid list from Annotation Verticle");
                        message.replyAndRequest(ReplyHandler.reportDatabaseQueryError(fetch.cause()));
                    }
                });
    }

    public void deleteProjectUUIDList(Message<JsonObject> message, @NonNull JDBCPool jdbcPool, @NonNull String query)
    {
        String projectID =  message.body().getString(ParamConfig.getProjectIDParam());
        JsonArray UUIDListJsonArray =  message.body().getJsonArray(ParamConfig.getUUIDListParam());

        ProjectLoader loader = ProjectHandler.getProjectLoader(projectID);
        List<String> dbUUIDList = loader.getUuidListFromDatabase();

        List<String> deleteUUIDList = ConversionHandler.jsonArray2StringList(UUIDListJsonArray);
        String uuidQueryParam = String.join(",", deleteUUIDList);

        Tuple params = Tuple.of(projectID, uuidQueryParam);

        jdbcPool.preparedQuery(query)
                .execute(params)
                .onComplete(fetch -> {
                    if (fetch.succeeded())
                    {
                        if (dbUUIDList.removeAll(deleteUUIDList))
                        {
                            loader.setUuidListFromDatabase(dbUUIDList);

                            List<String> sanityUUIDList = loader.getSanityUUIDList();
                            if (sanityUUIDList.removeAll(deleteUUIDList))
                            {
                                loader.setSanityUUIDList(sanityUUIDList);
                            }
                            else
                            {
                                log.info("Error in removing uuid list");
                            }

                            //update Portfolio Verticle
                            PortfolioVerticle.updateFileSystemUUIDList(projectID);

                            message.replyAndRequest(ReplyHandler.getOkReply().put(ParamConfig.getUUIDListParam(), "[]"));
                        }
                        else
                        {
                            message.reply(ReplyHandler.reportUserDefinedError("Failed to remove uuid from Portfolio Verticle. Project not expected to work fine"));
                        }
                    }
                    else
                    {
                        message.replyAndRequest(ReplyHandler.reportDatabaseQueryError(fetch.cause()));
                    }
                });
    }

    public void updateData(Message<JsonObject> message, @NonNull JDBCPool jdbcPool, @NonNull String query, AnnotationType annotationType)
    {
        JsonObject requestBody = message.body();

        try
        {
            String projectID = requestBody.getString(ParamConfig.getProjectIDParam());

            String annotationContent = requestBody.getJsonArray(ParamConfig.getAnnotationParam(annotationType)).encode();

            Tuple params = Tuple.of(
                    annotationContent,
                    requestBody.getInteger(ParamConfig.getImageDepth()),
                    requestBody.getInteger(ParamConfig.getImageXParam()),
                    requestBody.getInteger(ParamConfig.getImageYParam()),
                    requestBody.getDouble(ParamConfig.getImageWParam()),
                    requestBody.getDouble(ParamConfig.getImageHParam()),
                    requestBody.getInteger(ParamConfig.getFileSizeParam()),
                    requestBody.getInteger(ParamConfig.getImageORIWParam()),
                    requestBody.getInteger(ParamConfig.getImageORIHParam()),
                    requestBody.getString(ParamConfig.getUUIDParam()),
                    projectID);

            jdbcPool.preparedQuery(query)
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
        catch (Exception e)
        {
            log.info("AnnotationVerticle: " + message.body().toString());
            String messageInfo = "Error occur when updating data, " + e;
            message.replyAndRequest(ReplyHandler.reportBadParamError(messageInfo));
        }
    }

    public void retrieveData(Message<JsonObject> message, @NonNull JDBCPool jdbcPool, @NonNull String query, AnnotationType annotationType)
    {
        String projectName =  message.body().getString(ParamConfig.getProjectNameParam());
        String projectID =  message.body().getString(ParamConfig.getProjectIDParam());
        String uuid = message.body().getString(ParamConfig.getUUIDParam());

        Tuple params = Tuple.of(uuid,projectID);

        jdbcPool.preparedQuery(query)
                .execute(params)
                .onComplete(fetch -> {

                if (fetch.succeeded())
                {
                    RowSet<Row> rowSet = fetch.result();

                    if (rowSet.size() == 0)
                    {
                        log.info("Project id: " + params.getInteger(1));

                        String userDefinedMessage = "Data not found when retrieving for project " + projectName + " with uuid " + uuid;
                        message.replyAndRequest(ReplyHandler.reportUserDefinedError(userDefinedMessage));
                    }
                    else
                    {
                        for(Row row : rowSet)
                        {
                            String dataPath = row.getString(0);

                            Map<String, String> imgData = ImageHandler.getThumbNail(dataPath);

                            JsonObject response = ReplyHandler.getOkReply();

                            response.put(ParamConfig.getUUIDParam(), uuid);
                            response.put(ParamConfig.getProjectNameParam(), projectName);
                            response.put(ParamConfig.getImagePathParam(), dataPath);
                            response.put(ParamConfig.getAnnotationParam(annotationType), new JsonArray(row.getString(1)));
                            response.put(ParamConfig.getImageDepth(),  Integer.parseInt(imgData.get(ParamConfig.getImageDepth())));
                            response.put(ParamConfig.getImageXParam(), row.getInteger(2));
                            response.put(ParamConfig.getImageYParam(), row.getInteger(3));
                            response.put(ParamConfig.getImageWParam(), row.getDouble(4));
                            response.put(ParamConfig.getImageHParam(), row.getDouble(5));
                            response.put(ParamConfig.getFileSizeParam(), row.getInteger(6));
                            response.put(ParamConfig.getImageORIWParam(), Integer.parseInt(imgData.get(ParamConfig.getImageORIWParam())));
                            response.put(ParamConfig.getImageORIHParam(), Integer.parseInt(imgData.get(ParamConfig.getImageORIHParam())));
                            response.put(ParamConfig.getImageThumbnailParam(), imgData.get(ParamConfig.getBase64Param()));
                            message.replyAndRequest(response);
                        }
                    }
                }
                else
                {
                    String userDefinedMessage = "Failure in data retrieval for project " + projectName + " with uuid " + uuid;
                    message.replyAndRequest(ReplyHandler.reportUserDefinedError(userDefinedMessage));
                }
                });
    }
}