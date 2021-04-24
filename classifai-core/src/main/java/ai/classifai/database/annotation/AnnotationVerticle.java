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

import ai.classifai.action.parser.ProjectParser;
import ai.classifai.database.VerticleServiceable;
import ai.classifai.database.portfolio.PortfolioVerticle;
import ai.classifai.database.versioning.Annotation;
import ai.classifai.database.versioning.AnnotationVersion;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.collection.ConversionHandler;
import ai.classifai.util.collection.UuidGenerator;
import ai.classifai.util.data.FileHandler;
import ai.classifai.util.data.ImageHandler;
import ai.classifai.util.message.ReplyHandler;
import ai.classifai.util.project.ProjectHandler;
import ai.classifai.util.type.AnnotationHandler;
import ai.classifai.wasabis3.WasabiImageHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowIterator;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
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
    @Getter protected JDBCPool jdbcPool;

    public void retrieveDataPath(Message<JsonObject> message)
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

                            String dataPath = row.getString(0);

                            ProjectLoader loader = ProjectHandler.getProjectLoader(projectID);

                            if(loader.isCloud())
                            {
                                response.put(ParamConfig.getImgSrcParam(), WasabiImageHandler.encodeFileToBase64Binary(loader.getWasabiProject(), dataPath));
                            }
                            else
                            {
                                File fileImgPath = getDataFullPath(projectID, dataPath);

                                response.put(ParamConfig.getImgSrcParam(), ImageHandler.encodeFileToBase64Binary(fileImgPath));
                            }

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

        return Paths.get(projBasePath, dataSubPath).toFile();
    }

    public static void loadValidProjectUuid(@NonNull String projectId)
    {
        ProjectLoader loader = ProjectHandler.getProjectLoader(projectId);

        List<String> oriUUIDList = loader.getUuidListFromDb();

        loader.setDbOriUUIDSize(oriUUIDList.size());

        for (int i = 0; i < oriUUIDList.size(); ++i)
        {
            final Integer currentLength = i + 1;
            final String UUID = oriUUIDList.get(i);

            Tuple params = Tuple.of(projectId, UUID);

            JDBCPool clientJdbcPool = AnnotationHandler.getJDBCPool(loader);

            clientJdbcPool.preparedQuery(AnnotationQuery.getLoadValidProjectUuid())
                    .execute(params)
                    .onComplete(fetch -> {

                        if (fetch.succeeded())
                        {
                            RowSet<Row> rowSet = fetch.result();

                            if(rowSet.iterator().hasNext())
                            {
                                Row row = rowSet.iterator().next();

                                String dataSubPath = row.getString(0);
                                File dataFullPath = getDataFullPath(projectId, dataSubPath);

                                if (ImageHandler.isImageReadable(dataFullPath))
                                {
                                    loader.pushDBValidUUID(UUID);
                                }
                            }
                        }
                        loader.updateDBLoadingProgress(currentLength);
                    });
        }
    }

    public void loadValidProjectUuid(Message<JsonObject> message)
    {
        String projectId  = message.body().getString(ParamConfig.getProjectIdParam());

        message.replyAndRequest(ReplyHandler.getOkReply());

        loadValidProjectUuid(projectId);
    }


    @Deprecated
    public static void writeUuidToTable(@NonNull ProjectLoader loader, @NonNull File dataFullPath, @NonNull Integer currentLength)
    {
        String dataSubPath = FileHandler.trimPath(loader.getProjectPath(), dataFullPath.getAbsolutePath());

        String uuid = UuidGenerator.generateUuid();

        Annotation annotation = Annotation.builder()
                .projectId(loader.getProjectId())
                .imgPath(dataSubPath)
                .uuid(uuid)
                .annotationDict(ProjectParser.buildAnnotationDict(loader))
                .build();

        loader.getUuidAnnotationDict().put(uuid, annotation);

        JDBCPool clientJdbcPool = AnnotationHandler.getJDBCPool(loader);

        clientJdbcPool.preparedQuery(AnnotationQuery.getCreateData())
                .execute(annotation.getTuple())
                .onComplete(fetch -> {

                    if (fetch.succeeded())
                    {
                        loader.pushFileSysNewUUIDList(uuid);
                    }
                    else
                    {
                        log.error("Push data point with path " + dataFullPath.getAbsolutePath() + " failed: " + fetch.cause().getMessage());
                    }
                    loader.updateLoadingProgress(currentLength);
                });
    }

    @Deprecated
    public static void writeUuidToDb(@NonNull ProjectLoader loader, @NonNull File dataFullPath, @NonNull Integer currentLength)
    {
        String dataSubPath = FileHandler.trimPath(loader.getProjectPath(), dataFullPath.getAbsolutePath());

        String uuid = UuidGenerator.generateUuid();

        Annotation annotation = Annotation.builder()
                .projectId(loader.getProjectId())
                .imgPath(dataSubPath)
                .uuid(uuid)
                .annotationDict(ProjectParser.buildAnnotationDict(loader))
                .build();

        loader.getUuidAnnotationDict().put(uuid, annotation);

        JDBCPool clientJdbcPool = AnnotationHandler.getJDBCPool(loader);

        clientJdbcPool.preparedQuery(AnnotationQuery.getCreateData())
                .execute(annotation.getTuple())
                .onComplete(fetch -> {

                    if (fetch.succeeded())
                    {
                        loader.pushFileSysNewUUIDList(uuid);
                    }
                    else
                    {
                        log.error("Push data point with path " + dataFullPath.getAbsolutePath() + " failed: " + fetch.cause().getMessage());
                    }

                    loader.updateFileSysLoadingProgress(currentLength);
                });
    }

    public static void saveDataPoint(@NonNull ProjectLoader loader, @NonNull String dataPath, @NonNull Integer currentLength)
    {
        String uuid = UuidGenerator.generateUuid();

        Annotation annotation = Annotation.builder()
                .uuid(uuid)
                .projectId(loader.getProjectId())
                .imgPath(dataPath)
                .annotationDict(ProjectParser.buildAnnotationDict(loader))
                .build();
        loader.getUuidAnnotationDict().put(uuid, annotation);

        JDBCPool clientJdbcPool = AnnotationHandler.getJDBCPool(loader);

        clientJdbcPool.preparedQuery(AnnotationQuery.getCreateData())
                .execute(annotation.getTuple())
                .onComplete(fetch -> {

                    if (fetch.succeeded())
                    {
                        loader.pushFileSysNewUUIDList(uuid);
                    }
                    else
                    {
                        log.error("Push data point with path " + dataPath + " failed: " + fetch.cause().getMessage());
                    }

                    loader.updateLoadingProgress(currentLength);
                });
    }

    public static void configProjectLoaderFromDb(@NonNull ProjectLoader loader)
    {
        //export project table relevant
        JDBCPool clientJdbcPool = AnnotationHandler.getJDBCPool(loader);

        Map<String, Annotation> uuidAnnotationDict = loader.getUuidAnnotationDict();

        clientJdbcPool.preparedQuery(AnnotationQuery.getExtractProject())
                .execute(Tuple.of(loader.getProjectId()))
                .onComplete(annotationFetch ->{

                    if (annotationFetch.succeeded())
                    {
                        RowSet<Row> projectRowSet = annotationFetch.result();

                        if(projectRowSet.size() == 0)
                        {
                            log.debug("Extract project annotation retrieve 0 rows. Project not found from project database");
                        }
                        else
                        {
                            RowIterator<Row> rowIterator = projectRowSet.iterator();

                            while(rowIterator.hasNext())
                            {
                                Row row = rowIterator.next();

                                String fullPath = Paths.get(loader.getProjectPath(), row.getString(1)).toString();

                                if(loader.isCloud() || ImageHandler.isImageReadable(new File(fullPath)))
                                {
                                    Map<String, AnnotationVersion> annotationDict = ProjectParser.buildAnnotationDict(row.getString(2));

                                    Annotation annotation = Annotation.builder()
                                            .uuid(row.getString(0))         //uuid
                                            .projectId(loader.getProjectId())    //project_id
                                            .imgPath(row.getString(1))      //img_path
                                            .annotationDict(annotationDict)      //version_list
                                            .imgDepth(row.getInteger(3))    //img_depth
                                            .imgOriW(row.getInteger(4))     //img_ori_w
                                            .imgOriH(row.getInteger(5))     //img_ori_h
                                            .fileSize(row.getInteger(6))    //file_size
                                            .build();

                                    uuidAnnotationDict.put(row.getString(0), annotation);
                                }
                                else
                                {
                                    //remove uuid which is not readable
                                    loader.getSanityUuidList().remove(row.getString(0));
                                }

                            }


                        }
                    }
                });
    }


    private static void writeUuidToDbFromReloadingRootPath(@NonNull ProjectLoader loader, @NonNull String dataSubPath)
    {

        String uuid = UuidGenerator.generateUuid();

        Annotation annotation = Annotation.builder()
                .uuid(uuid)
                .projectId(loader.getProjectId())
                .imgPath(dataSubPath)
                .annotationDict(ProjectParser.buildAnnotationDict(loader))
                .build();

        //put annotation in ProjectLoader
        loader.getUuidAnnotationDict().put(uuid, annotation);

        JDBCPool clientJdbcPool = AnnotationHandler.getJDBCPool(loader);

        clientJdbcPool.preparedQuery(AnnotationQuery.getCreateData())
                .execute(annotation.getTuple())
                .onComplete(fetch -> {

                    if (fetch.succeeded())
                    {
                        loader.uploadUuidFromRootPath(uuid);
                    }
                    else
                    {
                        String dataFullPath = loader.getProjectPath() + dataSubPath;
                        log.error("Push data point with path " + dataFullPath + " failed: " + fetch.cause().getMessage());
                    }
                });
    }

    public static void uploadUuidFromConfigFile(@NonNull Tuple param, @NonNull ProjectLoader loader)
    {
        JDBCPool clientJdbcPool = AnnotationHandler.getJDBCPool(loader);

        clientJdbcPool.preparedQuery(AnnotationQuery.getCreateData())
                .execute(param)
                .onComplete(fetch -> {

                    if (fetch.succeeded())
                    {
                        String childPath = param.getString(2);

                        File currentImagePath = Paths.get(loader.getProjectPath(), childPath).toFile();

                        if(ImageHandler.isImageReadable(currentImagePath))
                        {
                            String uuid = param.getString(0);

                            loader.uploadSanityUuidFromConfigFile(uuid);
                        }
                    }
                    else
                    {
                        log.error("Push data point from config file failed" + fetch.cause().getMessage());
                    }
                });
    }

    public static void createUuidIfNotExist(@NonNull ProjectLoader loader, @NonNull File dataFullPath, @NonNull Integer currentProcessedLength)
    {
        String projectId = loader.getProjectId();

        String dataChildPath = FileHandler.trimPath(loader.getProjectPath(), dataFullPath.getAbsolutePath());

        Tuple params = Tuple.of(dataChildPath, projectId);

        JDBCPool clientJdbcPool = AnnotationHandler.getJDBCPool(loader);

        clientJdbcPool.preparedQuery(AnnotationQuery.getQueryUuid())
                .execute(params)
                .onComplete(fetch -> {

                    RowSet<Row> rowSet = fetch.result();

                    //not exist , create data point
                    if (rowSet.size() == 0)
                    {

                        if(ImageHandler.isImageReadable(dataFullPath))
                        {
                            writeUuidToDbFromReloadingRootPath(loader, dataChildPath);
                        }
                    }
                    else
                    {
                        Row row = rowSet.iterator().next();
                        String uuid = row.getString(0);

                        // if exist remove from Listbuffer to prevent from checking the item again
                        if(!loader.getSanityUuidList().contains(uuid))
                        {
                            loader.uploadUuidFromRootPath(uuid);
                        }

                        loader.getDbListBuffer().remove(uuid);
                    }

                    loader.updateReloadingProgress(currentProcessedLength);
                });


    }

    public void deleteProject(Message<JsonObject> message)
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

    public void deleteSelectionUuidList(Message<JsonObject> message)
    {
        String projectId =  message.body().getString(ParamConfig.getProjectIdParam());
        JsonArray UUIDListJsonArray =  message.body().getJsonArray(ParamConfig.getUuidListParam());

        ProjectLoader loader = ProjectHandler.getProjectLoader(projectId);
        List<String> dbUUIDList = loader.getUuidListFromDb();

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
                            loader.setUuidListFromDb(dbUUIDList);

                            List<String> sanityUUIDList = loader.getSanityUuidList();

                            if (sanityUUIDList.removeAll(deleteUUIDList))
                            {
                                loader.setSanityUuidList(sanityUUIDList);
                            }
                            else
                            {
                                log.info("Error in removing uuid list");
                            }

                            //update Portfolio Verticle
                            PortfolioVerticle.updateFileSystemUuidList(projectId);

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


   public void updateData(Message<JsonObject> message, @NonNull String annotationKey)
   {
        JsonObject requestBody = message.body();

        try
        {
            String projectId = requestBody.getString(ParamConfig.getProjectIdParam());
            String uuid = requestBody.getString(ParamConfig.getUuidParam());

            ProjectLoader loader = ProjectHandler.getProjectLoader(projectId);
            Annotation annotation = loader.getUuidAnnotationDict().get(uuid);

            Integer imgDepth = requestBody.getInteger(ParamConfig.getImgDepth());
            annotation.setImgDepth(imgDepth);

            Integer imgOriW = requestBody.getInteger(ParamConfig.getImgOriWParam());
            annotation.setImgOriW(imgOriW);

            Integer imgOriH = requestBody.getInteger(ParamConfig.getImgOriHParam());
            annotation.setImgOriH(imgOriH);

            Integer fileSize = requestBody.getInteger(ParamConfig.getFileSizeParam());
            annotation.setFileSize(fileSize);

            String currentVersionUuid = loader.getCurrentVersionUuid();

            AnnotationVersion version = annotation.getAnnotationDict().get(currentVersionUuid);

            version.setAnnotation(requestBody.getJsonArray(annotationKey));
            version.setImgX(requestBody.getInteger(ParamConfig.getImgXParam()));
            version.setImgY(requestBody.getInteger(ParamConfig.getImgYParam()));
            version.setImgW(requestBody.getInteger(ParamConfig.getImgWParam()));
            version.setImgH(requestBody.getInteger(ParamConfig.getImgHParam()));

            Tuple params = Tuple.of(annotation.getAnnotationDictDbFormat(),
                                    imgDepth,
                                    imgOriW,
                                    imgOriH,
                                    fileSize,
                                    uuid,
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
            String messageInfo = "Error occur when updating data, " + e;
            message.replyAndRequest(ReplyHandler.reportBadParamError(messageInfo));
        }
    }

    public void queryData(Message<JsonObject> message, @NonNull String annotationKey)
    {
        String projectId =  message.body().getString(ParamConfig.getProjectIdParam());
        String uuid = message.body().getString(ParamConfig.getUuidParam());

        ProjectLoader loader = ProjectHandler.getProjectLoader(projectId);

        Annotation annotation = loader.getUuidAnnotationDict().get(uuid);

        AnnotationVersion version = annotation.getAnnotationDict().get(loader.getCurrentVersionUuid());

        Map<String, String> imgData = new HashMap<>();
        String dataPath = "";

        if(loader.isCloud())
        {
            BufferedImage img = WasabiImageHandler.getThumbNail(loader.getWasabiProject(), annotation.getImgPath());

            //not checking orientation for on cloud version
            imgData = ImageHandler.getThumbNail(img, false, null);
        }
        else
        {
            dataPath = Paths.get(loader.getProjectPath(), annotation.getImgPath()).toString();

            try
            {
                File fileDataPath = new File(dataPath);

                BufferedImage img  = ImageIO.read(fileDataPath);

                imgData = ImageHandler.getThumbNail(img, true, fileDataPath);
            }
            catch(IOException e)
            {
                log.debug("Failure in reading image of path " + dataPath, e);
            }
        }

        JsonObject response = ReplyHandler.getOkReply();

        response.put(ParamConfig.getUuidParam(), uuid);
        response.put(ParamConfig.getProjectNameParam(), loader.getProjectName());

        response.put(ParamConfig.getImgPathParam(), dataPath);
        response.put(annotationKey, version.getAnnotation());
        response.put(ParamConfig.getImgDepth(),  Integer.parseInt(imgData.get(ParamConfig.getImgDepth())));
        response.put(ParamConfig.getImgXParam(), version.getImgX());
        response.put(ParamConfig.getImgYParam(), version.getImgY());
        response.put(ParamConfig.getImgWParam(), version.getImgW());
        response.put(ParamConfig.getImgHParam(), version.getImgH());
        response.put(ParamConfig.getFileSizeParam(), annotation.getFileSize());
        response.put(ParamConfig.getImgOriWParam(), Integer.parseInt(imgData.get(ParamConfig.getImgOriWParam())));
        response.put(ParamConfig.getImgOriHParam(), Integer.parseInt(imgData.get(ParamConfig.getImgOriHParam())));
        response.put(ParamConfig.getImgThumbnailParam(), imgData.get(ParamConfig.getBase64Param()));

        message.replyAndRequest(response);
    }
}