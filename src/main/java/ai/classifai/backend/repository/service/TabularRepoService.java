package ai.classifai.backend.repository.service;

import ai.classifai.backend.repository.JDBCPoolHolder;
import ai.classifai.backend.repository.query.TabularAnnotationQuery;
import ai.classifai.core.data.handler.TabularHandler;
import ai.classifai.core.dto.TabularDTO;
import ai.classifai.core.entity.annotation.TabularEntity;
import ai.classifai.core.loader.ProjectLoader;
import ai.classifai.core.loader.ProjectLoaderStatus;
import ai.classifai.core.service.annotation.AnnotationRepository;
import ai.classifai.frontend.request.ThumbnailProperties;
import io.vertx.core.Future;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Tuple;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class TabularRepoService implements AnnotationRepository<TabularEntity, TabularDTO> {

    private final JDBCPool annotationPool;

    public TabularRepoService(JDBCPoolHolder jdbcPoolHolder) {
        this.annotationPool = jdbcPoolHolder.getAnnotationPool();
    }

    @Override
    public Future<TabularEntity> createAnnotation(@NonNull TabularDTO annotationDTO) throws Exception {
        return null;
    }

    @Override
    public Future<List<TabularEntity>> listAnnotation(@NonNull String projectName) {
        return null;
    }

    @Override
    public Future<Void> updateAnnotation(@NonNull TabularDTO annotationDTO) {
        return null;
    }

    @Override
    public Future<Void> saveFilesMetaData(TabularDTO property) {
        return null;
    }

    @Override
    public Future<Void> deleteData(@NonNull String projectName, @NonNull String uuid) {
        return null;
    }

    @Override
    public Future<Void> createAnnotationProject() {
        return null;
    }

    @Override
    public Future<Void> deleteProjectById(@NonNull String projectId) {
        return null;
    }

    @Override
    public Future<ProjectLoaderStatus> loadAnnotationProject(@NonNull ProjectLoader projectLoader) {
        return null;
    }

    @Override
    public Future<String> renameData(@NonNull ProjectLoader projectLoader, String uuid, String newFileName) {
        return null;
    }

    @Override
    public void configProjectLoaderFromDb(@NonNull ProjectLoader loader) {

    }

//    public void createTabularProjectTable(ProjectLoader loader) {
//        String createProjectAttributeTableQuery = TabularAnnotationQuery.getCreateProjectAttributeTableQuery();
//        String updateProjectAttributeTableQuery = TabularAnnotationQuery.getUpdateProjectAttributeTableQuery();
//        String createProjectTableQuery = TabularAnnotationQuery.getCreateProjectTableQuery();
//
//        Tuple params = Tuple.of(loader.getProjectId(), TabularHandler.getColumnNames(), TabularHandler.getAttributeTypesJsonString());
//
//        annotationPool.getConnection()
//                .compose(conn ->
//                        conn.preparedQuery(createProjectAttributeTableQuery).execute()
//                                .compose(res ->
//                                        conn.preparedQuery(updateProjectAttributeTableQuery)
//                                                .execute(params)
//                                                .onComplete(response -> {
//                                                    if(response.succeeded()) {
//                                                        log.info("Project attribute table created and updated");
//                                                    }
//
//                                                    else if (response.failed()) {
//                                                        log.info("Fail to create project attribute table." + response.cause().getMessage());
//                                                    }
//                                                })
//                                )
//                                .compose(res ->
//                                        conn.preparedQuery(createProjectTableQuery)
//                                                .execute()
//                                                .onComplete(fetch -> {
//                                                    if(fetch.succeeded()) {
//                                                        log.info("Tabular table " + loader.getProjectName() + " created");
//                                                        loader.saveDataToTabularProjectTable();
//                                                    }
//
//                                                    else if(fetch.failed()) {
//                                                        log.info("Fail to create tabular table. " + fetch.cause().getMessage());
//                                                    }
//                                                })
//                                )
//                );
//    }
//
//    public void saveTabularData(@NonNull ProjectLoader loader, String[] rowsData, String filePath, Integer currentIndex) {
//        String query = TabularAnnotationQuery.getCreateDataQuery();
//        String uuid = UuidGenerator.generateUuid();
//        List<Object> tabularDataPoint = createTabularDataPoint(loader, uuid, rowsData, filePath);
//
//        Tuple params = Tuple.from(tabularDataPoint);
//        runQuery(loader,query, params, DBUtils.handleEmptyResponse(
//                () -> {
//                    loader.pushFileSysNewUUIDList(uuid);
//                    loader.updateLoadingProgress(currentIndex);
//                },
//                cause -> log.error("Push tabular data point with file path " + filePath + " failed: " + cause)
//        ));
//    }

    private List<Object> createTabularDataPoint(ProjectLoader loader, String uuid, String[] rowsData, String filePath) {
        List<Object> list = new ArrayList<>();
        String projectID = loader.getProjectId();
        list.add(uuid);
        list.add(projectID);
        list.add(loader.getProjectName());
        list.addAll(Arrays.asList(rowsData));
        list.add(filePath);
        list.add(null);

        return list;
    }

}
