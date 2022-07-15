package ai.classifai.backend.application;

import ai.classifai.backend.repository.database.DBUtils;
import ai.classifai.core.data.handler.TabularHandler;
import ai.classifai.core.dto.ProjectDTO;
import ai.classifai.core.dto.TabularDTO;
import ai.classifai.core.entity.annotation.TabularEntity;
import ai.classifai.core.loader.ProjectHandler;
import ai.classifai.core.loader.ProjectLoader;
import ai.classifai.core.loader.ProjectLoaderStatus;
import ai.classifai.core.properties.tabular.TabularProperties;
import ai.classifai.core.service.annotation.TabularAnnotationService;
import ai.classifai.core.service.annotation.TabularDataRepository;
import ai.classifai.core.service.project.ProjectService;
import ai.classifai.core.status.FileSystemStatus;
import ai.classifai.core.utility.handler.StringHandler;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class TabularService implements TabularAnnotationService<TabularDTO, TabularProperties> {

    private final ProjectService projectService;
    private final TabularHandler tabularHandler = new TabularHandler();
    private final TabularDataRepository<TabularEntity, TabularDTO> tabularDataRepository;
    private final ProjectHandler projectHandler;

    public TabularService(TabularDataRepository<TabularEntity, TabularDTO> tabularDataRepository,
                          ProjectService projectService,
                          ProjectHandler projectHandler)
    {
        this.tabularDataRepository = tabularDataRepository;
        this.projectService = projectService;
        this.projectHandler = projectHandler;
    }

    @Override
    public Future<ProjectLoader> createAnnotationProject(ProjectDTO projectDTO) throws Exception {
        tabularHandler.parseFile(projectDTO);
        List<String[]> dataList = tabularHandler.getData();
        ProjectLoader loader = projectHandler.getProjectLoader(projectDTO.getProjectId());
        loader.resetFileSysProgress(FileSystemStatus.DATABASE_UPDATING);
        loader.setFileSysTotalUUIDSize(dataList.size() - 1);

        return tabularDataRepository.createAnnotationProject()
                .compose(res -> tabularDataRepository.createAndUpdateProjectAttributeTable(loader.getProjectId(),
                        tabularHandler.getColumnNames(), tabularHandler.getAttributeTypesJsonString()))
                .compose(res -> {
                    List<Future> futures = new ArrayList<>();
                    for (int i = 1; i < dataList.size(); i++) {
                        TabularDTO tabularDTO = TabularDTO.builder()
                                .projectId(projectDTO.getProjectId())
                                .projectName(projectDTO.getProjectName())
                                .build();

                        tabularDTO.setFilePath(FilenameUtils.getName(projectDTO.getProjectPath()));
                        tabularDTO.setData(tabularHandler.ensureCorrectTypeInList(dataList.get(i)));
                        loader.pushFileSysNewUUIDList(tabularDTO.getUuid());
                        loader.updateLoadingProgress(i);
                        futures.add(tabularDataRepository.saveFilesMetaData(tabularDTO));
                    }

                    return Future.future(promise -> {
                        CompositeFuture.all(futures)
                                .onFailure(promise::fail)
                                .onSuccess(promise::complete);
                    });
                }).map(result -> loader);
    }

    @Override
    public Future<TabularDTO> createAnnotation(TabularDTO annotationDTO) throws Exception {
        return null;
    }

    @Override
    public Future<List<TabularDTO>> listAnnotations(String projectName) {
        return tabularDataRepository.listAnnotation(projectName)
                .map(res -> res.stream().map(TabularEntity::toDto).collect(Collectors.toList()));
    }

    @Override
    public Future<Optional<TabularDTO>> getAnnotationById(String projectName, String uuid) {
        return listAnnotations(projectName)
                .map(res -> res.stream()
                        .filter(dto -> dto.getUuid().equals(uuid))
                        .findFirst());
    }

    @Override
    public Future<Void> updateAnnotation(@NonNull TabularDTO annotationDTO, @NonNull ProjectLoader projectLoader) {
        return tabularDataRepository.updateAnnotation(annotationDTO)
                .map(DBUtils::toVoid);
    }

    @Override
    public Future<Void> deleteData(@NonNull String projectName, @NonNull String uuid) {
        return null;
    }

    @Override
    public Future<Void> deleteProjectById(@NonNull ProjectDTO projectDTO) {
        return null;
    }

    @Override
    public Future<ProjectLoaderStatus> loadProject(ProjectLoader projectLoader) {
        Promise<ProjectLoaderStatus> promise = Promise.promise();

        tabularDataRepository.loadAnnotationProject(projectLoader)
                .onComplete(res -> {
                    if (res.succeeded()) {
                        if (projectLoader.getIsProjectNew()) {
                            projectService.updateIsNewParam(projectLoader.getProjectId())
                                    .onFailure(promise::fail);
                        }

                        promise.complete(res.result());
                    }

                    if (res.failed()) {
                        promise.fail(res.cause());
                    }
                });

        return promise.future();
    }

    @Override
    public Future<String> renameData(@NonNull ProjectLoader projectLoader, String uuid, String newFileName) {
        return null;
    }

    @Override
    public TabularDTO toDTO(TabularProperties property, @NonNull ProjectLoader loader) {
        return null;
    }

    @Override
    public Future<List<JsonObject>> toJson(List<TabularDTO> tabularDTO) {
        Promise<List<JsonObject>> promise = Promise.promise();
        String projectName = tabularDTO.get(0).getProjectName();

        tabularDataRepository.getAttributes(projectName)
                .onComplete(res -> {
                    if (res.succeeded()) {
                        List<String> attributes = res.result()
                                .stream()
                                .map(StringHandler::removeQuotes)
                                .collect(Collectors.toList());

                        tabularDataRepository.getAttributeTypeMap(projectName)
                                .onComplete(result -> {
                                    if (result.succeeded()) {
                                        List<JsonObject> tabularData = new ArrayList<>();
                                        Map<String, String> attributeTypeMap = result.result();

                                        for (TabularDTO dto : tabularDTO) {
                                            JsonObject jsonObject = new JsonObject();
                                            jsonObject.put("uuid", dto.getUuid());
                                            jsonObject.put("project_id", dto.getProjectId());
                                            jsonObject.put("project_name", dto.getProjectName());
                                            jsonObject.put("label", dto.getLabel());
                                            jsonObject.put("file_path", dto.getFilePath());
                                            List<String> data = Arrays.asList(dto.getData());
                                            for (int i = 0; i < attributes.size(); i++) {
                                                String type = attributeTypeMap.get(attributes.get(i));
                                                switch (type) {
                                                    case "INT" -> jsonObject.put(attributes.get(i), Integer.valueOf(data.get(i)));
                                                    case "DECIMAL" -> jsonObject.put(attributes.get(i), Double.valueOf(data.get(i)));
                                                    default -> jsonObject.put(attributes.get(i), data.get(i));
                                                }
                                            }
                                            tabularData.add(jsonObject);
                                        }
                                        promise.complete(tabularData);
                                    }

                                    if (result.failed()) {
                                        promise.fail(result.cause());
                                    }
                                });
                    }
                    if (res.failed()) {
                        promise.fail(res.cause());
                    }
                });

        return promise.future();
    }

    @Override
    public Future<List<String>> getAllInvalidData(String projectName) {
        return tabularDataRepository.getAllInvalidData(projectName);
    }

    @Override
    public Future<Void> writeFile(String projectId, String fileType, boolean isFilterInvalidData) {
        return tabularDataRepository.writeFile(projectId, fileType, isFilterInvalidData);
    }
}
