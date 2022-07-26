package ai.classifai.backend.repository.service;

import ai.classifai.backend.repository.JDBCPoolHolder;
import ai.classifai.backend.repository.database.DBUtils;
import ai.classifai.backend.repository.query.QueryOps;
import ai.classifai.backend.repository.query.TabularAnnotationQuery;
import ai.classifai.core.data.handler.TabularHandler;
import ai.classifai.core.dto.TabularDTO;
import ai.classifai.core.entity.annotation.TabularEntity;
import ai.classifai.core.enumeration.AnnotationType;
import ai.classifai.core.loader.ProjectHandler;
import ai.classifai.core.loader.ProjectLoader;
import ai.classifai.core.loader.ProjectLoaderStatus;
import ai.classifai.core.service.annotation.TabularDataRepository;
import ai.classifai.core.utility.handler.StringHandler;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class TabularRepoService implements TabularDataRepository<TabularEntity, TabularDTO> {
    private final JDBCPool annotationPool;
    private final QueryOps queryOps = new QueryOps();
    private final ProjectHandler projectHandler;

    public TabularRepoService(JDBCPoolHolder jdbcPoolHolder, ProjectHandler projectHandler) {
        this.annotationPool = jdbcPoolHolder.getAnnotationPool();
        this.projectHandler = projectHandler;
    }

    @Override
    public Future<TabularEntity> createAnnotation(@NonNull TabularDTO annotationDTO) throws Exception {
        return null;
    }

    @Override
    public Future<List<TabularEntity>> listAnnotation(@NonNull String projectName) {
        Promise<List<TabularEntity>> promise = Promise.promise();

        retrieveGetAllDataQuery(projectName)
                .compose(res -> queryOps.runQuery(res, annotationPool))
                .onComplete(response -> {
                    if (response.succeeded()) {
                        getAttributes(projectName).onComplete(
                                res -> {
                                    if (res.succeeded()) {
                                        if (response.result().size() != 0) {
                                            List<TabularEntity> list = new ArrayList<>();
                                            List<String> attributes = res.result()
                                                    .stream()
                                                    .map(StringHandler::removeQuotes)
                                                    .collect(Collectors.toList());

                                            for (Row row : response.result()) {
                                                String[] data = attributes.stream()
                                                        .map(attr -> row.getValue(attr.strip().toUpperCase(Locale.ROOT)).toString())
                                                        .toArray(String[]::new);

                                                TabularEntity tabularEntity = TabularEntity.builder()
                                                        .uuid(row.getString("UUID"))
                                                        .projectId(row.getString("PROJECT_ID"))
                                                        .projectName(row.getString("PROJECT_NAME"))
                                                        .filePath(row.getString("FILE_PATH"))
                                                        .data(data)
                                                        .label(row.getString("LABEL"))
                                                        .build();

                                                list.add(tabularEntity);
                                            }
                                            promise.complete(list);
                                        }
                                    }

                                    if (res.failed()) {
                                        log.info(res.cause().getMessage());
                                    }
                                });
                    }

                    if (response.failed()) {
                        promise.fail("Fail to get tabular data. " + response.cause());
                    }
                });

        return promise.future();
    }

    private Future<String> retrieveGetAllDataQuery(String projectName) {
        ProjectLoader loader = projectHandler.getProjectLoader(projectName, AnnotationType.TABULAR);
        Tuple param = Tuple.of(loader.getProjectId());
        return queryOps.runQuery(TabularAnnotationQuery.getProjectAttributeQuery(), param, annotationPool)
                .map(res -> {
                    if(res.iterator().hasNext()) {
                        Row row = res.iterator().next();
                        TabularAnnotationQuery.createGetAllDataPreparedStatement(projectName, row.getString(0));
                        return TabularAnnotationQuery.getGetAllDataQuery();
                    }
                    return null;
                });
    }

    @Override
    public Future<List<String>> getAttributes(String projectName) {
        ProjectLoader loader = projectHandler.getProjectLoader(projectName, AnnotationType.TABULAR);
        Tuple param = Tuple.of(loader.getProjectId());
        return queryOps.runQuery(TabularAnnotationQuery.getProjectAttributeQuery(), param, annotationPool)
                .map(res -> {
                    List<String> list = new ArrayList<>();
                    if (res.iterator().hasNext()) {
                        for (Row row : res) {
                            String attributes = row.getString(0);
                            list = List.of(attributes.split(","));
                        }
                        return list;
                    }
                    return null;
                });
    }

    @Override
    public Future<Map<String,String>> getAttributeTypeMap(String projectName) {
        ProjectLoader loader = projectHandler.getProjectLoader(projectName, AnnotationType.TABULAR);
        Tuple param = Tuple.of(loader.getProjectId());
        return queryOps.runQuery(TabularAnnotationQuery.getAttributeTypeMapQuery(), param, annotationPool)
                .map(res -> {
                    if (res.size() != 0) {
                        Map<String,String> attributeTypeMap = new LinkedHashMap<>();
                        if (res.iterator().hasNext()) {
                            Row row = res.iterator().next();
                            String attributeMapString = StringUtils.substringBetween(row.getString(0), "{", "}");
                            String[] keyValuePairs = attributeMapString.split(",");
                            for (int i = 0; i < keyValuePairs.length; i++) {
                                String[] entry = keyValuePairs[i].split(":");
                                attributeTypeMap.put(StringHandler.removeQuotes(entry[0]), StringHandler.removeQuotes(entry[1]));
                            }
                        }
                        return attributeTypeMap;
                    }
                    return null;
                });
    }

    @Override
    public Future<Void> updateAnnotation(@NonNull TabularDTO annotationDTO) {
        Promise<Void> promise = Promise.promise();
        ProjectLoader loader = projectHandler.getProjectLoader(annotationDTO.getProjectName(), AnnotationType.TABULAR);
        TabularAnnotationQuery.createUpdateDataPreparedStatement(annotationDTO.getProjectName());
        Tuple params = Tuple.of(annotationDTO.getLabel(), annotationDTO.getUuid(), loader.getProjectId());

        queryOps.runQuery(TabularAnnotationQuery.getUpdateDataQuery(), params, annotationPool)
                .onComplete(res -> {
                    if (res.succeeded()) {
                        promise.complete();
                    }

                    if (res.failed()) {
                        promise.fail(res.cause());
                    }
                });

        return promise.future();
    }

    @Override
    public Future<Void> createAndUpdateProjectAttributeTable(String projectId, String columnNames, String attributeTypesJson) {
        String createProjectAttributeTableQuery = TabularAnnotationQuery.getCreateProjectAttributeTableQuery();
        String updateProjectAttributeTableQuery = TabularAnnotationQuery.getUpdateProjectAttributeTableQuery();
        Tuple params = Tuple.of(projectId, columnNames, attributeTypesJson);

        return queryOps.runQuery(createProjectAttributeTableQuery, annotationPool)
                .onComplete(result -> {
                    if(result.succeeded()) {
                        log.info("Tabular project attribute table created");
                    }

                    if (result.failed()) {
                        log.info("Fail to create tabular project attribute table. " + result.cause().getMessage());
                    }
                })
                .compose(res -> queryOps.runQuery(updateProjectAttributeTableQuery, params, annotationPool).onComplete(response -> {
                    if (response.succeeded()) {
                        log.info("Tabular project attribute table updated");
                    }

                    if (response.failed()) {
                        log.info("Fail to update tabular project attribute table. " + response.cause().getMessage());
                    }
                })).map(DBUtils::toVoid);
    }

    @Override
    public Future<Void> saveFilesMetaData(TabularDTO tabularDTO) {
        Tuple params = Tuple.from(createTabularDataPoint(tabularDTO));
        return queryOps.runQuery(TabularAnnotationQuery.getCreateDataQuery(), params, annotationPool)
                .map(DBUtils::toVoid);
    }

    private List<Object> createTabularDataPoint(TabularDTO tabularDTO) {
        List<Object> list = new ArrayList<>();
        list.add(tabularDTO.getUuid());
        list.add(tabularDTO.getProjectId());
        list.add(tabularDTO.getProjectName());
        list.addAll(Arrays.asList(tabularDTO.getData()));
        list.add(tabularDTO.getFilePath());
        list.add(tabularDTO.getLabel());

        return list;
    }

    @Override
    public Future<Void> deleteData(@NonNull String projectName, @NonNull String uuid) {
        return null;
    }

    @Override
    public Future<Void> createAnnotationProject() {
        Promise<Void> promise = Promise.promise();
        queryOps.runQuery(TabularAnnotationQuery.getCreateProjectTableQuery(), annotationPool)
                .onComplete(res -> {
                    if (res.succeeded()) {
                        log.info("Tabular project table created");
                        promise.complete();
                    }

                    if (res.failed()) {
                        log.info("Fail to create tabular project table.");
                        promise.fail(res.cause());
                    }
                });
        return promise.future();
    }

    @Override
    public Future<Void> deleteProjectById(@NonNull String projectId) {
        return null;
    }

    @Override
    public Future<ProjectLoaderStatus> loadAnnotationProject(@NonNull ProjectLoader projectLoader) {
        Promise<ProjectLoaderStatus> promise = Promise.promise();

        ProjectHandler.checkProjectLoaderStatus(projectLoader)
                .onComplete(res -> {
                    if (res.succeeded()) {
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
    public void configProjectLoaderFromDb(@NonNull ProjectLoader loader) {

    }

    @Override
    public Future<JsonArray> getLabel(String projectId, String uuid) {
        ProjectLoader loader = projectHandler.getProjectLoader(projectId);
        TabularAnnotationQuery.createGetLabelPreparedStatement(loader.getProjectName());
        String query = TabularAnnotationQuery.getGetLabelQuery();
        Tuple params = Tuple.of(uuid, projectId);

        return queryOps.runQuery(query, params, annotationPool).map(result -> {
            JsonArray labelsListJsonArray = new JsonArray();
            if(result.size() != 0) {
                Row row = result.iterator().next();
                String labelsJsonString = row.getString(0);

                if(labelsJsonString == null) {
                    JsonObject emptyLabelJson = new JsonObject().put("labelName", "").put("tagColor", "");
                    labelsListJsonArray.add(emptyLabelJson);
                }
                else {
                    JSONArray labelsJsonArray = new JSONArray(labelsJsonString);
                    TabularHandler.processJSONArrayToJsonArray(labelsJsonArray, labelsListJsonArray);
                }
                return labelsListJsonArray;
            }
            log.info("Fail to query label");
            return null;
        });
    }

    @Override
    public Future<Void> writeFile(String projectId, String format, boolean isFilterInvalidData) {
        switch(format) {
            case "csv" -> {
                return writeCsvFile(projectId, isFilterInvalidData);
            }
            case "json" -> {
                return writeJsonFile(projectId, isFilterInvalidData);
            }
        }
        return null;
    }

    public Future<Void> writeCsvFile(String projectId, boolean isFilterInvalidData) {
        Promise<Void> promise = Promise.promise();
        ProjectLoader loader = projectHandler.getProjectLoader(projectId);
        String projectName = loader.getProjectName();

        listAnnotation(projectName)
                .onComplete(res -> {
                    if(res.succeeded()) {
                        getAttributes(projectName)
                                .onComplete(result -> {
                                    if (result.succeeded()) {
                                        List<String> columnNames = result.result()
                                                .stream()
                                                .map(StringHandler::removeQuotes)
                                                .collect(Collectors.toList());
                                        columnNames.addAll(Collections.singletonList("label"));
                                        try {
                                            log.info("Generating csv file...");
                                            List<Object[]> objectsList = getListOfTabularDataObject(res.result(), columnNames, isFilterInvalidData);
                                            csvOutputFileWriter(objectsList, projectId);
                                            promise.complete();
                                        } catch (IOException e) {
                                            promise.fail("Error in generating csv file");
                                        }
                                    }

                                    if (result.failed()) {
                                        promise.fail(result.cause());
                                    }
                                });
                    }

                    if(res.failed()) {
                        promise.fail(res.cause());
                    }
                });

        return promise.future();
    }

    private static String extractLabelsFromArrayToString(Object value) {
        JSONArray labelsJsonArray = new JSONArray(value.toString());
        List<String> labelList = new ArrayList<>();
        for(int i = 0; i < labelsJsonArray.length(); i++) {
            labelList.add((String) labelsJsonArray.getJSONObject(i).get("labelName"));
        }
        String labelListString = StringUtils.join(labelList, " ");
        List<String> list = new ArrayList<>();
        list.add(labelListString);
        return list.toString();
    }

    private List<Object[]> getListOfTabularDataObject(List<TabularEntity> tabularEntityList, List<String> extractedColumnNames,
                                                      boolean isFilterInvalidData)
    {
        List<Object[]> resultArrayList = new ArrayList<>();
        resultArrayList.add(extractedColumnNames.toArray());
        for (TabularEntity tabularEntity : tabularEntityList) {
            Map<String, Object> objectMap = mapColumnNameToValue(tabularEntity, extractedColumnNames);
            getListOfTabularObject(objectMap, resultArrayList, extractedColumnNames, isFilterInvalidData);
        }
        return resultArrayList;
    }

    private Map<String, Object> mapColumnNameToValue(TabularEntity tabularEntity, List<String> columnNames) {
        Map<String, Object> map = new LinkedHashMap<>();
        List<Object> objects = new ArrayList<>(Arrays.asList(tabularEntity.getData()));
        objects.add(tabularEntity.getLabel());
        for (int i = 0; i < columnNames.size(); i++) {
            map.put(columnNames.get(i), objects.get(i));
        }
        return map;
    }

    private void getListOfTabularObject(Map<String, Object> objectMap, List<Object[]> resultArrayList,
                                        List<String> extractedColumnNames, boolean isFilterInvalidData)
    {
        List<Object> tempList = new ArrayList<>();
        for (String columnName : extractedColumnNames)
        {
            if (columnName.equals("label"))
            {
                if (objectMap.get(columnName) == null)
                {
                    tempList.add("[No Label]");
                }

                else
                {
                    String label = extractLabelsFromArrayToString(objectMap.get(columnName));
                    String content = StringUtils.substringBetween(label, "[", "]");
                    if (isFilterInvalidData && content.equals("Invalid")) return;
                    tempList.add(label);
                }
            }

            else
            {
                tempList.add(objectMap.get(columnName));
            }
        }
        Object[] resultArray = tempList.toArray(Object[]::new);
        resultArrayList.add(resultArray);
        tempList.clear();
    }

    private void csvOutputFileWriter(List<Object[]> retrievedDataList, String projectId) throws IOException {
        ProjectLoader loader = projectHandler.getProjectLoader(projectId);
        String projectPath = loader.getProjectPath().getAbsolutePath();
        File projectDirectory = new File(projectPath);
        String tempFilePath = loader.getProjectPath() + File.separator + loader.getProjectName() + ".text";
        String csvFilePath = loader.getProjectPath() + File.separator + loader.getProjectName() + ".csv";
        File csvFile = new File(csvFilePath);
        File tempFile = new File(tempFilePath);
        BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile));

        if (projectDirectory.mkdirs()) {
            log.debug("Project folder " + projectDirectory.getName() + " is created");
        } else {
            log.debug("Project folder " + projectDirectory.getName() + " is exist");
        }

        for (Object[] objArr : retrievedDataList) {
            for (int i = 0; i < objArr.length; i++) {
                if (i != objArr.length - 1) {
                    writer.write(objArr[i].toString());
                    writer.write(",");
                } else {
                    writer.write(objArr[i].toString());
                }
            }
            writer.newLine();
        }
        writer.close();
        tempFile.renameTo(csvFile);

        if (csvFile.exists()) {
            log.info(csvFile.getName() + " is generated in project folder");
        } else {
            log.info("Fail to generate csv file for project " + loader.getProjectName());
        }
    }

    private List<JsonObject> getListOfTabularDataJsonObject(List<TabularEntity> tabularEntityList, List<String> extractedColumnNames, boolean isFilterInvalidData) {
        List<JsonObject> resultList = new ArrayList<>();

        for(TabularEntity tabularEntity : tabularEntityList) {
            Map<String, Object> objectMap = mapColumnNameToValue(tabularEntity, extractedColumnNames);
            getTabularJsonObject(objectMap, resultList, extractedColumnNames, isFilterInvalidData);
        }

        return resultList;
    }

    private void getTabularJsonObject(Map<String, Object> objectMap, List<JsonObject> resultList,
                                      List<String> extractedColumnNames, boolean isFilterInvalidData)
    {
        JsonObject jsonObject = new JsonObject();
        for (String columnName : extractedColumnNames)
        {
            if (columnName.equals("label"))
            {
                if (objectMap.get(columnName) == null)
                {
                    jsonObject.put(columnName,"[No Label]");
                }

                else
                {
                    String label = extractLabelsFromArrayToString(objectMap.get(columnName));
                    String content = StringUtils.substringBetween(label, "[", "]");
                    if (isFilterInvalidData && content.equals("Invalid")) return;
                    jsonObject.put(columnName, extractLabelsFromArrayToString(objectMap.get(columnName)));
                }
            }

            else
            {
                jsonObject.put(columnName, objectMap.get(columnName));
            }
        }
        resultList.add(jsonObject);
    }

    private Future<Void> writeJsonFile(String projectId, boolean isFilterInvalidData) {
        Promise<Void> promise = Promise.promise();
        ProjectLoader loader = projectHandler.getProjectLoader(projectId);
        String projectName = loader.getProjectName();

        listAnnotation(projectName)
                .onComplete(res -> {
                    if(res.succeeded()) {
                        getAttributes(projectName)
                                .onComplete(result -> {
                                    if (result.succeeded()) {
                                        List<String> columnNames = result.result()
                                                .stream()
                                                .map(StringHandler::removeQuotes)
                                                .collect(Collectors.toList());
                                        columnNames.addAll(Collections.singletonList("label"));
                                        try {
                                            log.info("Generating json file...");
                                            List<JsonObject> jsonObjectList = getListOfTabularDataJsonObject(res.result(), columnNames, isFilterInvalidData);
                                            jsonFileWriter(jsonObjectList, projectId);
                                            promise.complete();
                                        } catch (IOException e) {
                                            promise.fail("Error in generating json file");
                                        }
                                    }

                                    if (result.failed()) {
                                        promise.fail(result.cause());
                                    }
                                });
                    }

                    if(res.failed()) {
                        promise.fail(res.cause());
                    }
                });

        return promise.future();
    }

    private void jsonFileWriter(List<JsonObject> result, String projectId) throws IOException {
        ProjectLoader loader = projectHandler.getProjectLoader(projectId);
        String projectPath = loader.getProjectPath().getAbsolutePath();
        File projectDirectory = new File(projectPath);
        String jsonFilePath = loader.getProjectPath() + File.separator + loader.getProjectName() + ".json";
        FileWriter writer = new FileWriter(jsonFilePath);

        if(projectDirectory.mkdirs()) {
            log.debug("Project folder " + projectDirectory.getName() + " is created");
        } else {
            log.debug("Project folder " + projectDirectory.getName() + " is exist");
        }

        try {
            writer.write(result.toString());
            log.info(loader.getProjectName() + ".json is generated in project folder");
        }
        catch (IOException e) {
            log.info("Fail to generate json file for project " + loader.getProjectName());
        }
        finally {
            writer.flush();
            writer.close();
        }
    }

    @Override
    public Future<List<String>> getAllInvalidData(String projectName) {
        Promise<List<String>> promise = Promise.promise();
        listAnnotation(projectName).onComplete(res -> {
            if (res.succeeded()) {
                if (res.result().size() != 0) {
                    List<String> listOfInvalidUUID = new ArrayList<>();
                    for(TabularEntity tabularEntity : res.result()) {
                        boolean invalidLabels = false;
                        String label = tabularEntity.getLabel();
                        if(label != null) {
                            JSONArray labelsJsonArray = new JSONArray(label);
                            for(int i = 0; i < labelsJsonArray.length(); i++) {
                                JSONObject object = labelsJsonArray.getJSONObject(i);
                                if(object.getString("labelName").equals("Invalid")) {
                                    invalidLabels = true;
                                }
                            }
                            if(invalidLabels) {
                                listOfInvalidUUID.add(tabularEntity.getUuid());
                            }
                        }
                    }
                    promise.complete(listOfInvalidUUID);
                }
            }
            if (res.failed()) {
                promise.fail(res.cause());
            }
        });
        return promise.future();
    }

}
