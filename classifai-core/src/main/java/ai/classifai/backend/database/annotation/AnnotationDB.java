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
package ai.classifai.backend.database.annotation;


import ai.classifai.backend.action.parser.ProjectParser;
import ai.classifai.backend.database.DBUtils;
import ai.classifai.backend.database.JDBCPoolHolder;
import ai.classifai.backend.database.portfolio.PortfolioDB;
import ai.classifai.backend.database.versioning.Annotation;
import ai.classifai.core.entities.properties.DataInfoProperties;
import ai.classifai.core.loader.ProjectLoader;
import ai.classifai.core.util.collection.UuidGenerator;
import ai.classifai.core.util.data.FileHandler;
import ai.classifai.core.util.data.ImageHandler;
import ai.classifai.core.util.data.StringHandler;
import ai.classifai.core.util.project.ProjectHandler;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Implementation of Functionalities for each annotation type
 *
 * @author codenamewei
 */
@Slf4j
public class AnnotationDB
{
    private final JDBCPoolHolder holder;
    private final ProjectHandler projectHandler;
    @Setter // Circular Dependency
    private PortfolioDB portfolioDB;

    public AnnotationDB(JDBCPoolHolder holder, ProjectHandler projectHandler, PortfolioDB portfolioDB){
        this.holder = holder;
        this.projectHandler = projectHandler;
        this.portfolioDB = portfolioDB;
    }

    public void runQuery(ProjectLoader loader, String query, Tuple params, Handler<AsyncResult<RowSet<Row>>> handler){
        JDBCPool clientJdbcPool = holder.getJDBCPool(loader);

        clientJdbcPool.preparedQuery(query)
                .execute(params)
                .onComplete(handler);
    }


    public void loadValidProjectUuid(@NonNull String projectId)
    {
        ProjectLoader loader = Objects.requireNonNull(projectHandler.getProjectLoader(projectId));

        List<String> oriUUIDList = loader.getUuidListFromDb();

        loader.setDbOriUUIDSize(oriUUIDList.size());

        for (int i = 0; i < oriUUIDList.size(); ++i)
        {
            final Integer currentLength = i + 1;
            final String UUID = oriUUIDList.get(i);

            Tuple params = Tuple.of(projectId, UUID);

            runQuery(loader, AnnotationQuery.getLoadValidProjectUuid(), params, DBUtils.handleResponse(
                    result -> {
                        if(result.iterator().hasNext())
                        {
                            Row row = result.iterator().next();

                            String dataSubPath = row.getString(0);
                            File dataFullPath = loader.getDataFullPath(dataSubPath);

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

    public void saveDataPoint(@NonNull ProjectLoader loader, @NonNull String dataPath, @NonNull Integer currentLength)
    {
        String uuid = UuidGenerator.generateUuid();

        Annotation annotation = Annotation.builder()
                .uuid(uuid)
                .projectId(loader.getProjectId())
                .imgPath(dataPath)
                .annotationDict(ProjectParser.buildAnnotationDict(loader))
                .build();

        loader.getUuidAnnotationDict().put(uuid, annotation);

        runQuery(loader,AnnotationQuery.getCreateData(), annotation.getTuple(),
                DBUtils.handleEmptyResponse(
                        () -> {
                            loader.pushFileSysNewUUIDList(uuid);
                            loader.updateLoadingProgress(currentLength);
                        },
                        cause -> log.error("Push data point with path " + dataPath + " failed: " + cause)
                ));
    }

    public void configProjectLoaderFromDb(@NonNull ProjectLoader loader)
    {
        runQuery(loader, AnnotationQuery.getExtractProject(), Tuple.of(loader.getProjectId()), DBUtils.handleResponse(
                result -> {
                    if (result.size() == 0) {
                        log.info("Extract project annotation retrieve 0 rows. Project not found from project database");
                    } else {

                        for (Row row : result) {
                            String fullPath = loader.getDataFullPath(row.getString(1)).toString();

                            if (loader.isCloud() || ImageHandler.isImageReadable(new File(fullPath))) {
                                Map<String, DataInfoProperties> annotationDict = ProjectParser.buildAnnotationDict(row.getString(2));

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

                                loader.getUuidAnnotationDict().put(row.getString(0), annotation);
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


    private void writeUuidToDbFromReloadingRootPath(@NonNull ProjectLoader loader, @NonNull String dataSubPath)
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

        runQuery(loader, AnnotationQuery.getCreateData(), annotation.getTuple(), DBUtils.handleResponse(
                result -> {
                    loader.uploadUuidFromRootPath(uuid);
                    portfolioDB.updateFileSystemUuidList(loader.getProjectId());
                },
                cause -> {
                    String dataFullPath = loader.getProjectPath() + dataSubPath;
                    log.error("Push data point with path " + dataFullPath + " failed: " + cause);
                }
        ));
    }

    public void uploadUuidFromConfigFile(@NonNull Tuple param, @NonNull ProjectLoader loader)
    {
        runQuery(loader, AnnotationQuery.getCreateData(), param, DBUtils.handleResponse(
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

    public void createUuidIfNotExist(@NonNull ProjectLoader loader, @NonNull File dataFullPath, @NonNull Integer currentProcessedLength)
    {
        String projectId = loader.getProjectId();

        String dataChildPath = StringHandler.removeFirstSlashes(FileHandler.trimPath(loader.getProjectPath().getAbsolutePath(), dataFullPath.getAbsolutePath()));

        Tuple params = Tuple.of(dataChildPath, projectId);

        runQuery(loader, AnnotationQuery.getQueryUuid(), params, DBUtils.handleResponse(
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
}