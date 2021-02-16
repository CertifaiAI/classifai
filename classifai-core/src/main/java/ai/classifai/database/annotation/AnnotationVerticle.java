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
import ai.classifai.util.collection.UUIDGenerator;
import ai.classifai.util.data.FileHandler;
import ai.classifai.util.data.ImageHandler;
import ai.classifai.util.message.ErrorCodes;
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
    public void retrieveDataPath(Message<JsonObject> message, @NonNull JDBCPool jdbcPool)
    {
        String projectID = message.body().getString(ParamConfig.getProjectIdParam());
        String uuid = message.body().getString(ParamConfig.getUuidParam());

        Tuple params = Tuple.of(uuid, projectID);

        jdbcPool.preparedQuery(AnnotationQuery.getRetrieveDataPath())
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

                            Row row = rowSet.iterator().next();

                            String imgSubPath = row.getString(0);

                            File fileImgPath = getDataFullPath(projectID, imgSubPath);

                            response.put(ParamConfig.getImgSrcParam(), ImageHandler.encodeFileToBase64Binary(fileImgPath));
                            message.replyAndRequest(response);

                        }
                    }
                    else
                    {
                        message.replyAndRequest(ReplyHandler.reportDatabaseQueryError(fetch.cause()));
                    }
                });
    }

    private static File getDataFullPath(@NonNull String projectId, @NonNull String dataSubPath)
    {
        String projBasePath = ProjectHandler.getProjectLoader(projectId).getProjectPath();

        return new File(projBasePath + dataSubPath);
    }

    private static Tuple getNewAnnotation(@NonNull String projectId, @NonNull String dataSubPath, @NonNull String UUID)
    {
        return Tuple.of(UUID,    //uuid
                projectId,                         //project_id
                dataSubPath,                             //img_path
                new JsonArray().toString(),              //new ArrayList<Integer>()
                0,                                       //img_depth
                0,                                       //img_X
                0,                                       //img_Y
                0,                                       //img_W
                0,                                       //img_H
                0,                                       //file_size
                0,                                       //img_ori_w
                0);                                      //img_ori_w
    }


    private static Tuple getNewAnnotation(@NonNull String projectId, @NonNull String dataSubPath)
    {
        return getNewAnnotation(projectId, dataSubPath, UUIDGenerator.generateUUID());
    }

    public void loadValidProjectUUID(Message<JsonObject> message, @NonNull JDBCPool jdbcPool)
    {
        String projectID  = message.body().getString(ParamConfig.getProjectIdParam());

        ProjectLoader loader = ProjectHandler.getProjectLoader(projectID);

        List<String> oriUUIDList = loader.getUuidListFromDatabase();

        message.replyAndRequest(ReplyHandler.getOkReply());

        loader.setDbOriUUIDSize(oriUUIDList.size());

        for (int i = 0; i < oriUUIDList.size(); ++i)
        {
            final Integer currentLength = i + 1;
            final String UUID = oriUUIDList.get(i);

            Tuple params = Tuple.of(projectID, UUID);

            jdbcPool.preparedQuery(AnnotationQuery.getLoadValidProjectUUID())
                    .execute(params)
                    .onComplete(fetch -> {

                        if (fetch.succeeded())
                        {
                            RowSet<Row> rowSet = fetch.result();

                            Row row = rowSet.iterator().next();

                            String dataSubPath = row.getString(0);
                            String dataFullPath = getDataFullPath(projectID, dataSubPath).getAbsolutePath();

                            if (ImageHandler.isImageReadable(dataSubPath))
                            {
                                loader.pushDBValidUUID(UUID);
                            }
                        }
                        loader.updateDBLoadingProgress(currentLength);
                    });
          }
    }

    public static void updateUUID(@NonNull JDBCPool jdbcPool, @NonNull String projectID, @NonNull File file, @NonNull String UUID, @NonNull Integer currentLength)
    {
        ProjectLoader loader = ProjectHandler.getProjectLoader(projectID);

        String dataChildPath = FileHandler.trimPath(loader.getProjectPath(), file.getAbsolutePath());

        Tuple params = AnnotationVerticle.getNewAnnotation(projectID, dataChildPath, UUID);

        jdbcPool.preparedQuery(AnnotationQuery.getCreateData())
                .execute(params)
                .onComplete(fetch -> {

                    if (fetch.succeeded())
                    {
                        loader.pushFileSysNewUUIDList(UUID);
                    }
                    else
                    {
                        log.error("Push data point with path " + file.getAbsolutePath() + " failed: " + fetch.cause().getMessage());
                    }

                    loader.updateFileSysLoadingProgress(currentLength);
                });
    }


    public static void updateUUIDFromReloading(@NonNull JDBCPool jdbcPool, @NonNull String projectId, @NonNull File file)
    {
        ProjectLoader loader = ProjectHandler.getProjectLoader(projectId);

        String dataChildPath = FileHandler.trimPath(loader.getProjectPath(), file.getAbsolutePath());

        String uuid = UUIDGenerator.generateUUID();

        Tuple param = getNewAnnotation(projectId, dataChildPath);


        jdbcPool.preparedQuery(AnnotationQuery.getCreateData())
                .execute(param)
                .onComplete(fetch -> {

                if (fetch.succeeded())
                {
                    loader.getUuidListFromDatabase().add(uuid);
                    loader.getSanityUUIDList().add(uuid);
                }
                else
                {
                    log.error("Push data point with path " + file.getAbsolutePath() + " failed: " + fetch.cause().getMessage());
                }

        });
    }

    public static void createUUIDIfNotExist(@NonNull JDBCPool jdbcPool, @NonNull String projectID, @NonNull File file, @NonNull Integer currentProcessedLength)
    {
        ProjectLoader loader = ProjectHandler.getProjectLoader(projectID);

        Tuple params = Tuple.of(file.getAbsolutePath(), projectID);

        jdbcPool.preparedQuery(AnnotationQuery.getQueryUuid())
                .execute(params)
                .onComplete(fetch -> {
                  
                RowSet<Row> rowSet = fetch.result();

                //not exist , create data point
                if (rowSet.size() == 0)
                {
                    updateUUIDFromReloading(jdbcPool, projectID, file);
                }
                else
                {
                    Row row = rowSet.iterator().next();
                    String uuid = row.getString(0);

                    // if exist remove from Listbuffer to prevent from checking the item again
                    if(!loader.getSanityUUIDList().contains(uuid))
                    {
                        loader.getSanityUUIDList().add(uuid);
                    }

                    loader.getDbListBuffer().remove(uuid);

                }

                loader.updateReloadingProgress(currentProcessedLength);
            });
    }

    public void deleteProject(Message<JsonObject> message, @NonNull JDBCPool jdbcPool)
    {
        String projectID = message.body().getString(ParamConfig.getProjectIdParam());

        Tuple params = Tuple.of(projectID);

        jdbcPool.preparedQuery(AnnotationQuery.getDeleteProject())
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

    public void deleteSelectionUuidList(Message<JsonObject> message, @NonNull JDBCPool jdbcPool)
    {
        String projectId =  message.body().getString(ParamConfig.getProjectIdParam());
        JsonArray UUIDListJsonArray =  message.body().getJsonArray(ParamConfig.getUuidListParam());

        ProjectLoader loader = ProjectHandler.getProjectLoader(projectId);
        List<String> dbUUIDList = loader.getUuidListFromDatabase();

        List<String> deleteUUIDList = ConversionHandler.jsonArray2StringList(UUIDListJsonArray);
        String uuidQueryParam = String.join(",", deleteUUIDList);

        Tuple params = Tuple.of(projectId, uuidQueryParam);

        jdbcPool.preparedQuery(AnnotationQuery.getDeleteSelectionUuidList())
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
                            PortfolioVerticle.updateFileSystemUUIDList(projectId);

                            message.replyAndRequest(ReplyHandler.getOkReply());
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

   public void updateData(Message<JsonObject> message, @NonNull JDBCPool jdbcPool)
   {
        JsonObject requestBody = message.body();

        try
        {
            String projectId = requestBody.getString(ParamConfig.getProjectIdParam());

            String annotationContent = requestBody.getJsonArray(ParamConfig.getAnnotationParam()).encode();

            Tuple params = Tuple.of(annotationContent,
                                    requestBody.getInteger(ParamConfig.getImgDepth()),
                                    requestBody.getInteger(ParamConfig.getImgXParam()),
                                    requestBody.getInteger(ParamConfig.getImgYParam()),
                                    requestBody.getDouble(ParamConfig.getImgWParam()),
                                    requestBody.getDouble(ParamConfig.getImgHParam()),
                                    requestBody.getInteger(ParamConfig.getFileSizeParam()),
                                    requestBody.getInteger(ParamConfig.getImgOriWParam()),
                                    requestBody.getInteger(ParamConfig.getImgOriHParam()),
                                    requestBody.getString(ParamConfig.getUuidParam()),
                                    projectId);

            jdbcPool.preparedQuery(AnnotationQuery.getUpdateData())
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

    public void queryData(Message<JsonObject> message, @NonNull JDBCPool jdbcPool)
    {
        String projectName =  message.body().getString(ParamConfig.getProjectNameParam());
        String projectID =  message.body().getString(ParamConfig.getProjectIdParam());
        String uuid = message.body().getString(ParamConfig.getUuidParam());

        Tuple params = Tuple.of(uuid, projectID);
        
        jdbcPool.preparedQuery(AnnotationQuery.getQueryData())
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
                        Row row = rowSet.iterator().next();


                        Integer counter = 0;
                        String dataChildPath = row.getString(counter++);
                        String dataFullPath = AnnotationVerticle.getDataFullPath(projectID, dataChildPath).getAbsolutePath();

                        Map<String, String> imgData = ImageHandler.getThumbNail(dataChildPath);

                        JsonObject response = ReplyHandler.getOkReply();

                        response.put(ParamConfig.getUuidParam(), uuid);
                        response.put(ParamConfig.getProjectNameParam(), projectName);

                        response.put(ParamConfig.getImgPathParam(), dataFullPath);
                        response.put(ParamConfig.getAnnotationParam(), new JsonArray(row.getString(counter++)));
                        response.put(ParamConfig.getImgDepth(),  Integer.parseInt(imgData.get(ParamConfig.getImgDepth())));
                        response.put(ParamConfig.getImgXParam(), row.getInteger(counter++));
                        response.put(ParamConfig.getImgYParam(), row.getInteger(counter++));
                        response.put(ParamConfig.getImgWParam(), row.getDouble(counter++));
                        response.put(ParamConfig.getImgHParam(), row.getDouble(counter++));
                        response.put(ParamConfig.getFileSizeParam(), row.getInteger(counter++));
                        response.put(ParamConfig.getImgOriWParam(), Integer.parseInt(imgData.get(ParamConfig.getImgOriWParam())));
                        response.put(ParamConfig.getImgOriHParam(), Integer.parseInt(imgData.get(ParamConfig.getImgOriHParam())));
                        response.put(ParamConfig.getImgThumbnailParam(), imgData.get(ParamConfig.getBase64Param()));
                        message.replyAndRequest(response);
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