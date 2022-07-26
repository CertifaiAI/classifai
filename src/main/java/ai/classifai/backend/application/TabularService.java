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
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
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

                    return Future.future(promise -> CompositeFuture.all(futures)
                            .onFailure(promise::fail)
                            .onSuccess(promise::complete));
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

    @Override
    public Future<Optional<TabularDTO>> automateTabularLabelling(String projectId, JsonObject preLabellingConditions, String currentUuid, String labellingMode) {
        ProjectLoader loader = projectHandler.getProjectLoader(projectId);
        List<String> uuidList = loader.getUuidListFromDb();

        tabularDataRepository.getAttributes(loader.getProjectName())
                .onComplete(res -> {
                    if (res.succeeded()) {
                        tabularDataRepository.getAttributeTypeMap(loader.getProjectName())
                                .onComplete(result -> {
                                    if (result.succeeded()) {
                                        try {
                                            JsonObject attributesJsonObject = new JsonObject();
                                            for (Map.Entry<String, String> map : result.result().entrySet()) {
                                                attributesJsonObject.put(map.getKey(), map.getValue());
                                            }
                                            initiateAutomaticLabellingForTabular(loader, preLabellingConditions,
                                                    labellingMode, uuidList, attributesJsonObject);
                                        } catch (Exception e) {
                                            log.info("Fail to initiate auto pre-labeling for tabular data. " + e);
                                        }
                                    }

                                    if (result.failed()) {
                                        log.warn("Fail to get " + result.cause().getMessage());
                                    }
                                });
                    }
                    if (res.failed()) {
                        log.warn("Fail to get attributes");
                    }
                });

        return getAnnotationById(loader.getProjectName(), currentUuid);
    }

    private void initiateAutomaticLabellingForTabular(ProjectLoader loader, JsonObject preLabellingConditions,
                                                     String labellingMode, List<String> uuidList, JsonObject attributeMap) throws Exception
    {
        if(preLabellingConditions != null)
        {
            for(String uuid : uuidList)
            {
                for(int i = 0; i < preLabellingConditions.size(); i++)
                {
                    JsonObject tabularDataFromDataBase = getTabularDataById(loader.getProjectName(), uuid);
                    JsonObject condition = preLabellingConditions.getJsonObject(String.valueOf(i));
                    String labelsFromConditionSetting = tabularHandler.parsePreLabellingCondition(condition.getMap(),
                            attributeMap, tabularDataFromDataBase);
                    List<String> listOfLabelsFromDataBase = getListOfLabelsFromDataBase(loader.getProjectId(), uuid);

                    if(labelsFromConditionSetting != null) {
                        // update labels directly if labels from database is null or selected mode is overwrite
                        if(listOfLabelsFromDataBase.size() == 0 || labellingMode.equals("overwrite")) {
                            if(!TabularHandler.isContainInvalidData(getLabelFromDataBase(loader.getProjectId(),uuid))) {
                                updateTabularDataLabelInDatabase(loader.getProjectName(), uuid, labelsFromConditionSetting);
                            }
                        }

                        // update labels on appending the labels
                        else if (labellingMode.equals("append")){
                            JsonArray labelsFromDataBaseJsonArray = getLabelFromDataBase(loader.getProjectId(), uuid);
                            JsonArray labelsFromConditionSettingJsonArray = TabularHandler.parseLabelsJsonStringToJsonArray(labelsFromConditionSetting);
                            String distinctLabels = TabularHandler.processExistedLabelsAndConditionLabels(labelsFromDataBaseJsonArray,
                                    labelsFromConditionSettingJsonArray);
                            updateTabularDataLabelInDatabase(loader.getProjectName(), uuid, distinctLabels);
                        }
                    }
                }
            }
        }
        else
        {
            throw new Exception("Pre-Labeling conditions must be provided in automatic labeling.");
        }
    }

    private JsonObject getTabularDataById(String projectName, String uuid) throws ExecutionException, InterruptedException
    {
        CompletableFuture<JsonObject> future = new CompletableFuture<>();
        getAnnotationById(projectName, uuid)
                .onComplete(res -> {
                    if(res.succeeded()) {
                        log.info("get");
                        toJson(List.of(Objects.requireNonNull(res.result().orElse(null))))
                                .onComplete(response -> {
                                    if (response.succeeded()) {
                                        future.complete(response.result().get(0));
                                    }

                                    if (response.failed()) {
                                        future.completeExceptionally(response.cause());
                                    }
                                });
                    }

                    if (res.failed())
                    {
                        log.info("fail to get annotation by id");
                        future.completeExceptionally(res.cause());
                    }
                });
        return future.get();
    }

    private JsonArray getLabelFromDataBase(String projectId, String uuid) throws ExecutionException, InterruptedException
    {
        CompletableFuture<JsonArray> future = new CompletableFuture<>();
        tabularDataRepository.getLabel(projectId, uuid)
                .onComplete(res -> {
                    if(res.succeeded()) {
                        future.complete(res.result());
                    } else {
                        future.completeExceptionally(res.cause());
                    }
                });

        return future.get();
    }

    private List<String> getListOfLabelsFromDataBase(String projectId, String uuid) throws ExecutionException, InterruptedException
    {
        List<String> listOfLabelsFromDataBase = new ArrayList<>();
        JsonArray labelsFromDataBaseJsonArray = getLabelFromDataBase(projectId, uuid);

        if(labelsFromDataBaseJsonArray.size() == 1)
        {
            JsonObject jsonObject = labelsFromDataBaseJsonArray.getJsonObject(0);
            if(jsonObject.getString("labelName").equals(""))
            {
                return listOfLabelsFromDataBase;
            }
        }

        listOfLabelsFromDataBase = TabularHandler.getLabelList(labelsFromDataBaseJsonArray);
        return listOfLabelsFromDataBase;
    }

    private void updateTabularDataLabelInDatabase(String projectName, String uuid, String labels)
    {
        JsonObject jsonObject = new JsonObject()
                .put("uuid", uuid)
                .put("labels", labels);

        TabularDTO tabularDTO = TabularDTO.builder()
                .uuid(uuid)
                .projectName(projectName)
                .label(jsonObject.toString())
                .build();

        tabularDataRepository.updateAnnotation(tabularDTO)
                .onComplete(res -> {
                    if (res.succeeded()) {
                        log.debug("Update label complete");
                    }

                    if (res.failed()) {
                        log.debug("Update label failed");
                    }
                });
    }
}
