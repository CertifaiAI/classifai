package ai.classifai.backend.repository.service;

import ai.classifai.backend.repository.JDBCPoolHolder;
import ai.classifai.backend.repository.database.DBUtils;
import ai.classifai.backend.repository.query.QueryOps;
import ai.classifai.backend.repository.query.TabularAnnotationQuery;
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

//    public Future<JsonObject> getTabularDataByUuid(String projectID, String uuid) {
//        ProjectLoader loader = projectHandler.getProjectLoader(projectID);
//        String projectAttributeQuery = TabularAnnotationQuery.getProjectAttributeQuery();
//        JDBCPool pool = this.holder.getJDBCPool(loader);
//
//        Tuple params = Tuple.of(projectID);
//        Tuple params2 = Tuple.of(uuid, projectID);
//
//        return pool.withConnection(conn ->
//                conn.preparedQuery(projectAttributeQuery)
//                        .execute(params)
//                        .map(res -> {
//                            if(res.size() != 0) {
//                                Row row = res.iterator().next();
//                                TabularAnnotationQuery.createGetSpecificDataPreparedStatement(loader, row.getString(0));
//
//                                return TabularAnnotationQuery.getGetDataQuery();
//                            }
//                            return null;
//                        })
//                        .compose(res -> conn.preparedQuery(res).execute(params2))
//                        .map(res -> {
//                            if (res.size() != 0) {
//                                conn.close();
//                                return res.iterator().next().toJson();
//                            }
//                            conn.close();
//                            return null;
//                        })
//        )
//                .onSuccess(res -> log.info("Retrieve data success"))
//                .onFailure(res -> log.info("Fail to retrieve data from database. " + res.getCause().getMessage()));
//
//    }
//

//
//    public Future<JsonArray> getLabel(String projectId, String uuid) {
//        ProjectLoader loader = projectHandler.getProjectLoader(projectId);
//        TabularAnnotationQuery.createGetLabelPreparedStatement(loader);
//        String query = TabularAnnotationQuery.getGetLabelQuery();
//        Tuple params = Tuple.of(uuid, projectId);
//
//        return runQuery(query, params, this.holder.getJDBCPool(loader)).map(result -> {
//            JsonArray labelsListJsonArray = new JsonArray();
//            if(result.size() != 0) {
//                Row row = result.iterator().next();
//                String labelsJsonString = row.getString(0);
//
//                if(labelsJsonString == null) {
//                    JsonObject emptyLabelJson = new JsonObject().put("labelName", "").put("tagColor", "");
//                    labelsListJsonArray.add(emptyLabelJson);
//                }
//                else {
//                    JSONArray labelsJsonArray = new JSONArray(labelsJsonString);
//                    TabularHandler.processJSONArrayToJsonArray(labelsJsonArray, labelsListJsonArray);
//                }
//                return labelsListJsonArray;
//            }
//            log.info("Fail to query label");
//            return null;
//        });
//    }
//

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
                                        columnNames.addAll(Arrays.asList("fileName","label"));
                                        try {
                                            log.info("Generating csv file...");
                                            csvOutputFileWriter(listOfObjectOfTabularData(res.result()
                                                    , columnNames, isFilterInvalidData), projectId);
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

    private List<Object[]> listOfObjectOfTabularData(List<TabularEntity> tabularEntityList, List<String> extractedColumnNames,
                                                     boolean isFilteredInvalidData)
    {
        List<Object[]> resultArrayList = new ArrayList<>();
        resultArrayList.add(extractedColumnNames.toArray());
        for(TabularEntity tabularEntity : tabularEntityList) {
            List<Object> tempList = new ArrayList<>();
            if(isFilteredInvalidData) {
                if(!checkContainInvalidData(tabularEntity.getLabel())) {
                    getListOfTabularObject(extractedColumnNames, resultArrayList, tabularEntity, tempList);
                }
            } else {
                getListOfTabularObject(extractedColumnNames, resultArrayList, tabularEntity, tempList);
            }
        }
        return resultArrayList;
    }

    private void getListOfTabularObject(List<String> extractedColumnNames, List<Object[]> resultArrayList, TabularEntity tabularEntity, List<Object> tempList) {
        for (String extractedColumnName : extractedColumnNames) {
            String columnName = extractedColumnName.toUpperCase();
//            Object value = row.toJson().getValue(columnName);
            if (columnName.equals("LABEL")) {
                if (value == null) {
                    tempList.add("No Label");
                } else {
                    tempList.add(extractLabelsFromArrayToString(value));
                }
            } else {
                tempList.add(value);
            }
        }
        Object[] resultArray = tempList.toArray(Object[]::new);
        resultArrayList.add(resultArray);
        tempList.clear();
    }

    private Future<JsonObject> convertToJson(TabularEntity tabularEntity) {
        Promise<JsonObject> promise = Promise.promise();
        String projectName = tabularEntity.getProjectName();

        getAttributes(projectName)
                .onComplete(res -> {
                    if (res.succeeded()) {
                        List<String> attributes = res.result()
                                .stream()
                                .map(StringHandler::removeQuotes)
                                .collect(Collectors.toList());

                        getAttributeTypeMap(projectName)
                                .onComplete(result -> {
                                    if (result.succeeded()) {
                                        Map<String, String> attributeTypeMap = result.result();
                                        JsonObject jsonObject = new JsonObject();
                                        jsonObject.put("uuid", tabularEntity.getUuid());
                                        jsonObject.put("project_id", tabularEntity.getProjectId());
                                        jsonObject.put("project_name", tabularEntity.getProjectName());
                                        jsonObject.put("label", tabularEntity.getLabel());
                                        jsonObject.put("file_path", tabularEntity.getFilePath());

                                        List<String> data = Arrays.asList(tabularEntity.getData());
                                        for (int i = 0; i < attributes.size(); i++) {
                                            String type = attributeTypeMap.get(attributes.get(i));
                                            switch (type) {
                                                case "INT" -> jsonObject.put(attributes.get(i), Integer.valueOf(data.get(i)));
                                                case "DECIMAL" -> jsonObject.put(attributes.get(i), Double.valueOf(data.get(i)));
                                                default -> jsonObject.put(attributes.get(i), data.get(i));
                                            }
                                        }

                                        promise.complete(jsonObject);
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

    private boolean checkContainInvalidData(String label){
        if(label != null) {
            JSONArray jsonArray = new JSONArray(label);
            for(int i = 0; i < jsonArray.length(); i++) {
                String labelName = jsonArray.getJSONObject(i).getString("labelName");
                if(labelName.equals("Invalid")) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<JsonObject> lisOfJsonObjectOfTabularData(RowSet<Row> result, List<String> extractedColumnNames, boolean isFilterInvalidData) {
        List<JsonObject> resultList = new ArrayList<>();

        for(Row row : result.value()) {
            JsonObject jsonObject = new JsonObject();
            if(isFilterInvalidData) {
                if(checkContainInvalidData(row)){
                    getTabularJsonObject(extractedColumnNames, resultList, row, jsonObject);
                }
            } else {
                getTabularJsonObject(extractedColumnNames, resultList, row, jsonObject);
            }

        }

        return resultList;
    }

    private void getTabularJsonObject(List<String> extractedColumnNames, List<JsonObject> resultList, Row row, JsonObject jsonObject) {
        for (String extractedColumnName : extractedColumnNames) {
            String columnName = extractedColumnName.toUpperCase();
            Object value = row.toJson().getValue(columnName);
            if (columnName.equals("LABEL")) {
                if (value == null) {
                    jsonObject.put(extractedColumnName,"No Label");
                } else {
                    jsonObject.put(extractedColumnName, extractLabelsFromArrayToString(value));
                }
            } else {
                jsonObject.put(extractedColumnName, value);
            }
        }
        resultList.add(jsonObject);
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
            log.info("Project folder " + projectDirectory.getName() + " is created");
        } else {
            log.info("Project folder " + projectDirectory.getName() + " is exist");
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

    private Future<Void> writeJsonFile(String projectId, boolean isFilterInvalidData) {
        ProjectLoader loader = projectHandler.getProjectLoader(projectId);
        String query = TabularAnnotationQuery.getProjectAttributeQuery();
        Tuple params = Tuple.of(projectId);
        JDBCPool pool = this.holder.getJDBCPool(loader);
        List<String> extractedColumnNames = new ArrayList<>();
        Promise<Void> promise = Promise.promise();

        return pool.withConnection(conn -> {
            conn.preparedQuery(query).execute(params).map(res -> {
                if(res.size() != 0) {
                    Row row = res.iterator().next();
                    String columnNames = row.getString(0) + ",fileName,label";
                    String[] columnNamesArray = columnNames.split(",");
                    extractedColumnNames.addAll(Arrays.asList(columnNamesArray));
                    TabularAnnotationQuery.createGetAllDataPreparedStatement(loader, row.getString(0));

                    return TabularAnnotationQuery.getGetAllDataQuery();
                }
                return null;
            }).compose(res -> conn.preparedQuery(res).execute())
                    .map(result -> {
                        if(result.size() != 0) {
                            return lisOfJsonObjectOfTabularData(result.value(), extractedColumnNames, isFilterInvalidData);
                        }
                        return null;
                    })
                    .onComplete(res -> {
                        if(res.succeeded()) {
                            try {
                                log.info("Generating Json file...");
                                jsonFileWriter(res.result(), projectId);
                                promise.complete();
                            } catch (IOException e) {
                                promise.fail("Error in generating json file");
                            }
                        }

                        if(res.failed()) {
                            promise.fail(res.cause());
                        }
                    });
            return promise.future();
        });
    }

    private void jsonFileWriter(List<JsonObject> result, String projectId) throws IOException {
        ProjectLoader loader = projectHandler.getProjectLoader(projectId);
        String projectPath = loader.getProjectPath().getAbsolutePath();
        File projectDirectory = new File(projectPath);
        String jsonFilePath = loader.getProjectPath() + File.separator + loader.getProjectName() + ".json";
        FileWriter writer = new FileWriter(jsonFilePath);

        if(projectDirectory.mkdirs()) {
            log.info("Project folder " + projectDirectory.getName() + " is created");
        } else {
            log.info("Project folder " + projectDirectory.getName() + " is exist");
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

//    public Future<JsonObject> automateTabularLabelling(String projectId, JsonObject preLabellingConditions,
//                                                       String currentUuid, String labellingMode,PortfolioDB portfolioDB) throws Exception {
//        ProjectLoader loader = projectHandler.getProjectLoader(projectId);
//        List<String> uuidList = loader.getUuidListFromDb();
//        TabularHandler tabularHandler = new TabularHandler();
////        tabularHandler.initiateAutomaticLabellingForTabular(projectId, preLabellingConditions, uuidList, labellingMode,portfolioDB);
//        log.info("initiate");
//        Promise<JsonObject> promise = Promise.promise();
//        log.info(String.valueOf(tabularHandler.checkIsCurrentUuidFinished(currentUuid)));
//        if(tabularHandler.checkIsCurrentUuidFinished(currentUuid)) {
//            getTabularDataByUuid(projectId, currentUuid).onComplete(res -> {
//                if(res.succeeded()) {
//                    promise.complete(res.result());
//                }
//
//                if(res.failed()) {
//                    promise.fail(res.cause());
//                }
//            });
//        }
//        return promise.future();
//    }

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
