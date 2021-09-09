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


import ai.classifai.action.DeleteProjectData;
import ai.classifai.action.RenameProjectData;
import ai.classifai.action.parser.ProjectParser;
import ai.classifai.database.DBUtils;
import ai.classifai.database.VerticleServiceable;
import ai.classifai.database.portfolio.PortfolioVerticle;
import ai.classifai.database.versioning.Annotation;
import ai.classifai.database.versioning.AnnotationVersion;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.collection.UuidGenerator;
import ai.classifai.util.data.FileHandler;
import ai.classifai.util.data.ImageHandler;
import ai.classifai.util.data.StringHandler;
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
import io.vertx.sqlclient.Tuple;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
                .onComplete(DBUtils.handleResponse(
                        message,
                        result -> {

                            if (result.size() == 0)
                            {
                                String projectName = message.body().getString(ParamConfig.getProjectNameParam());
                                String userDefinedMessage = "Failure in data path retrieval for project " + projectName + " with uuid " + uuid;
                                message.replyAndRequest(ReplyHandler.reportUserDefinedError(userDefinedMessage));
                            }
                            else
                            {
                                JsonObject response = ReplyHandler.getOkReply();

                                Row row = result.iterator().next();

                                String dataPath = row.getString(0);

                                ProjectLoader loader = Objects.requireNonNull(ProjectHandler.getProjectLoader(projectID));

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
                ));
    }

    public static File getDataFullPath(@NonNull String projectId, @NonNull String dataSubPath)
    {
        ProjectLoader loader = Objects.requireNonNull(ProjectHandler.getProjectLoader(projectId));

        return Paths.get(loader.getProjectPath().getAbsolutePath(), dataSubPath).toFile();

    }

    public static void loadValidProjectUuid(@NonNull String projectId)
    {
        ProjectLoader loader = Objects.requireNonNull(ProjectHandler.getProjectLoader(projectId));

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
                    .onComplete(DBUtils.handleResponse(
                            result -> {
                                if(result.iterator().hasNext())
                                {
                                    Row row = result.iterator().next();

                                    String dataSubPath = row.getString(0);
                                    File dataFullPath = getDataFullPath(projectId, dataSubPath);

                                    if (ImageHandler.isImageReadable(dataFullPath))
                                    {
                                        loader.pushDBValidUUID(UUID);
                                    }
                                }
                                loader.updateDBLoadingProgress(currentLength);
                            },
                            cause -> log.info("Fail to load prject UUID")
                    ));
        }
    }

    public void loadValidProjectUuid(Message<JsonObject> message)
    {
        String projectId  = message.body().getString(ParamConfig.getProjectIdParam());

        message.replyAndRequest(ReplyHandler.getOkReply());

        loadValidProjectUuid(projectId);
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
                .onComplete(DBUtils.handleEmptyResponse(
                        () -> {
                            loader.pushFileSysNewUUIDList(uuid);
                            loader.updateLoadingProgress(currentLength);
                        },
                        cause -> log.error("Push data point with path " + dataPath + " failed: " + cause)
                ));
    }

    public static void configProjectLoaderFromDb(@NonNull ProjectLoader loader)
    {
        //export project table relevant
        JDBCPool clientJdbcPool = AnnotationHandler.getJDBCPool(loader);

        Map<String, Annotation> uuidAnnotationDict = loader.getUuidAnnotationDict();

        clientJdbcPool.preparedQuery(AnnotationQuery.getExtractProject())
                .execute(Tuple.of(loader.getProjectId()))
                .onComplete(DBUtils.handleResponse(
                        result -> {
                            if(result.size() == 0)
                            {
                                log.info("Extract project annotation retrieve 0 rows. Project not found from project database");
                            }
                            else
                            {

                                for (Row row : result) {
                                    String fullPath = Paths.get(loader.getProjectPath().getAbsolutePath(), row.getString(1)).toString();

                                    if (loader.isCloud() || ImageHandler.isImageReadable(new File(fullPath))) {
                                        Map<String, AnnotationVersion> annotationDict = ProjectParser.buildAnnotationDict(new JsonArray(row.getString(2)));

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
                                    } else {
                                        //remove uuid which is not readable
                                        loader.getSanityUuidList().remove(row.getString(0));
                                    }

                                }
                            }
                        },
                        cause -> log.info("Error query for config loader from db")
                ));
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
                .onComplete(DBUtils.handleResponse(
                        result -> {
                            loader.uploadUuidFromRootPath(uuid);
                            PortfolioVerticle.updateFileSystemUuidList(loader.getProjectId());
                        },
                        cause -> {
                            String dataFullPath = loader.getProjectPath() + dataSubPath;
                            log.error("Push data point with path " + dataFullPath + " failed: " + cause);
                        }
                ));
    }

    public static void uploadUuidFromConfigFile(@NonNull Tuple param, @NonNull ProjectLoader loader)
    {
        JDBCPool clientJdbcPool = AnnotationHandler.getJDBCPool(loader);

        clientJdbcPool.preparedQuery(AnnotationQuery.getCreateData())
                .execute(param)
                .onComplete(DBUtils.handleResponse(
                        result ->  {
                            String childPath = param.getString(2);

                            File currentImagePath = Paths.get(loader.getProjectPath().getAbsolutePath(), childPath).toFile();

                            if(ImageHandler.isImageReadable(currentImagePath))
                            {
                                String uuid = param.getString(0);

                                loader.uploadSanityUuidFromConfigFile(uuid);
                            }
                        },
                        cause -> log.error("Push data point from config file failed " + cause)
                ));
    }

    public static void createUuidIfNotExist(@NonNull ProjectLoader loader, @NonNull File dataFullPath, @NonNull Integer currentProcessedLength)
    {
        String projectId = loader.getProjectId();

        String dataChildPath = StringHandler.removeFirstSlashes(FileHandler.trimPath(loader.getProjectPath().getAbsolutePath(), dataFullPath.getAbsolutePath()));

        Tuple params = Tuple.of(dataChildPath, projectId);

        JDBCPool clientJdbcPool = AnnotationHandler.getJDBCPool(loader);

        clientJdbcPool.preparedQuery(AnnotationQuery.getQueryUuid())
                .execute(params)
                .onComplete(DBUtils.handleResponse(
                        result -> {
                            //not exist , create data point
                            if (result.size() == 0)
                            {
                                if(ImageHandler.isImageFileValid(dataFullPath))
                                {
                                    writeUuidToDbFromReloadingRootPath(loader, dataChildPath);
                                }
                            }
                            else
                            {
                                Row row = result.iterator().next();
                                String uuid = row.getString(0);

                                // if exist remove from Listbuffer to prevent from checking the item again
                                if(!loader.getSanityUuidList().contains(uuid))
                                {
                                    loader.uploadUuidFromRootPath(uuid);
                                }

                                loader.getDbListBuffer().remove(uuid);
                            }

                            loader.updateReloadingProgress(currentProcessedLength);
                        },
                        cause -> log.info("Fail to create UUID")
                ));

    }

    public void deleteProject(Message<JsonObject> message)
    {
        String projectID = message.body().getString(ParamConfig.getProjectIdParam());

        Tuple params = Tuple.of(projectID);

        jdbcPool.preparedQuery(AnnotationQuery.getDeleteProject())
                .execute(params)
                .onComplete(DBUtils.handleEmptyResponse(message));
    }

    public void deleteProjectData(Message<JsonObject> message)
    {
        DeleteProjectData.deleteProjectData(jdbcPool, message);
    }

    public void updateData(Message<JsonObject> message, @NonNull String annotationKey)
    {
        JsonObject requestBody = message.body();

        try
        {
            String projectId = requestBody.getString(ParamConfig.getProjectIdParam());
            String uuid = requestBody.getString(ParamConfig.getUuidParam());

            ProjectLoader loader = Objects.requireNonNull(ProjectHandler.getProjectLoader(projectId));
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
                    .onComplete(DBUtils.handleEmptyResponse(message));
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

        ProjectLoader loader = Objects.requireNonNull(ProjectHandler.getProjectLoader(projectId));

        Annotation annotation = loader.getUuidAnnotationDict().get(uuid);

        AnnotationVersion version = annotation.getAnnotationDict().get(loader.getCurrentVersionUuid());

        Map<String, String> imgData = new HashMap<>();
        String dataPath = "";

        if(loader.isCloud())
        {
            try
            {
                BufferedImage img = WasabiImageHandler.getThumbNail(loader.getWasabiProject(), annotation.getImgPath());

                //not checking orientation for on cloud version
                imgData = ImageHandler.getThumbNail(img);
            }
            catch(Exception e)
            {
                log.debug("Unable to write Buffered Image.");
            }

        }
        else
        {
            dataPath = Paths.get(loader.getProjectPath().getAbsolutePath(), annotation.getImgPath()).toString();

            try
            {
                Mat imageMat  = Imgcodecs.imread(dataPath);

                BufferedImage img = ImageHandler.toBufferedImage(imageMat);

                imgData = ImageHandler.getThumbNail(img);
            }
            catch(Exception e)
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

    public void renameProjectData(Message<JsonObject> message)
    {
        RenameProjectData.renameProjectData(jdbcPool, message);
    }
}