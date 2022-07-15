package ai.classifai.core.data.handler;

import ai.classifai.backend.repository.query.TabularAnnotationQuery;
import ai.classifai.core.data.type.tabular.CsvData;
import ai.classifai.core.data.type.tabular.ExcelData;
import ai.classifai.core.data.type.tabular.TabularQueryGen;
import ai.classifai.core.data.type.tabular.TabularUtils;
import ai.classifai.core.dto.ProjectDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.exceptions.CsvException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tabular data handling
 *
 * @author ken479
 */

@Slf4j
@NoArgsConstructor
public class TabularHandler {
    private final TabularQueryGen tabularQueryGen = new TabularQueryGen();
    private final TabularUtils tabularUtils = new TabularUtils();
    private List<String> headerNames;
    private Map<String, String> headers;
    @Getter
    private List<String[]> data;

    public void parseFile(ProjectDTO projectDTO) throws IOException, CsvException {
        String filePath = projectDTO.getProjectFilePath();
        String fileType = FilenameUtils.getExtension(filePath);

        if (fileType.equals("csv"))
        {
            CsvData csvData = new CsvData();
            data = csvData.readCsvFile(filePath);
            headerNames = csvData.getHeaderNames();
        }

        else if (fileType.equals("xlsx"))
        {
            ExcelData excelData = new ExcelData();
            data = excelData.readExcelFile(filePath, fileType);
            headerNames = excelData.getHeaderNames();
        }

        else if (fileType.equals("xls"))
        {
            ExcelData excelData = new ExcelData();
            data = excelData.readExcelFile(filePath, fileType);
            headerNames = excelData.getHeaderNames();
        }

        else
        {
            throw new IllegalArgumentException("File type not supported. Only support csv, xlsx and xls.");
        }

        headers = tabularQueryGen.extractHeaders(data, headerNames);
        int columnNumbers = headers.size() + 5;
        TabularAnnotationQuery.createProjectTablePreparedStatement(headers, projectDTO.getProjectName());
        TabularAnnotationQuery.createDataPreparedStatement(projectDTO.getProjectName(), columnNumbers);
    }

    public String[] ensureCorrectTypeInList(String[] rowsData) {
        String[] data = new String[rowsData.length];
        for(int i = 0; i < rowsData.length; i++) {
            data[i] = tabularUtils.typeCheckingPushedData(rowsData[i], i, headerNames, headers);
        }
        return data;
    }

    public String getColumnNames() {
        return String.join(", ", headerNames.toArray(new String[0]));
    }

    public String getAttributeTypesJsonString() {
        Map<String, String> map = new LinkedHashMap<>();

        for(Map.Entry<String, String> entry : headers.entrySet()) {
            map.put(entry.getKey().replace("\"", ""), entry.getValue());
        }

        String jsonString = "";
        ObjectMapper mapper = new ObjectMapper();

        try {
            jsonString = mapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            log.info("Error happened in json processing for attribute types map");
        }

        return jsonString;
    }
//
//    /**
//     * Automated Labelling
//     *
//     */
////    public void initiateAutomaticLabellingForTabular(String projectId, JsonObject preLabellingConditions, List<String> uuidList, String labellingMode,
////                                                     PortfolioDB portfolioDB) throws Exception {
//    public void initiateAutomaticLabellingForTabular(String projectId, JsonObject preLabellingConditions, String labellingMode,
//                                                     PortfolioDB portfolioDB, ProjectHandler projectHandler) throws Exception {
//        JsonObject attributeTypeMap = getAttributeTypeFromDataBase(projectId, portfolioDB);
//        ProjectLoader loader = projectHandler.getProjectLoader(projectId);
//        List<String> uuidList = loader.getUuidListFromDb();
//        if(preLabellingConditions != null)
//        {
//            for(String uuid : uuidList)
//            {
//                for(int i = 0; i < preLabellingConditions.size(); i++)
//                {
//                    JsonObject tabularDataFromDataBase = getTabularDataFromDataBase(projectId, uuid, portfolioDB);
//                    JsonObject condition = preLabellingConditions.getJsonObject(String.valueOf(i));
//                    String labelsFromConditionSetting = parsePreLabellingCondition(condition.getMap(), attributeTypeMap,tabularDataFromDataBase);
//                    List<String> listOfLabelsFromDataBase = getListOfLabelsFromDataBase(projectId, uuid, portfolioDB);
//
//                    if(labelsFromConditionSetting != null) {
//                        // update labels directly if labels from database is null or selected mode is overwrite
//                        if(listOfLabelsFromDataBase.size() == 0 || labellingMode.equals("overwrite")) {
//                            if(!isContainInvalidData(getLabelFromDataBase(projectId,uuid, portfolioDB))) {
//                                updateTabularDataLabelInDatabase(projectId, uuid, labelsFromConditionSetting, portfolioDB);
//                            }
//                        }
//
//                        // update labels on appending the labels
//                        else if (labellingMode.equals("append")){
//                            JsonArray labelsFromDataBaseJsonArray = getLabelFromDataBase(projectId, uuid, portfolioDB);
//                            JsonArray labelsFromConditionSettingJsonArray = parseLabelsJsonStringToJsonArray(labelsFromConditionSetting);
//                            String distinctLabels = processExistedLabelsAndConditionLabels(labelsFromDataBaseJsonArray,
//                                    labelsFromConditionSettingJsonArray);
//                            updateTabularDataLabelInDatabase(projectId, uuid, distinctLabels, portfolioDB);
//                        }
//                    }
//                }
////                processingUuid = uuid;
//            }
////            processingUuid = "";
//        }
//    }
//
//    public Boolean checkIsCurrentUuidFinished(String currentUuid) {
//        return currentUuid.equals(processingUuid);
//    }
//
//    private JsonArray parseLabelsJsonStringToJsonArray(String labelsJsonString) {
//        JsonArray labelsListJsonArray = new JsonArray();
//        JSONArray labelsJsonArray = new JSONArray(labelsJsonString);
//
//        processJSONArrayToJsonArray(labelsJsonArray, labelsListJsonArray);
//        return labelsListJsonArray;
//    }
//
//    public static void processJSONArrayToJsonArray(JSONArray jsonArray, JsonArray vertxJsonArray) {
//        for(int i = 0; i < jsonArray.length(); i++) {
//            JsonObject jsonObject = new JsonObject()
//                    .put("labelName", jsonArray.getJSONObject(i).get("labelName"))
//                    .put("tagColor", jsonArray.getJSONObject(i).get("tagColor"));
//            vertxJsonArray.add(jsonObject);
//        }
//    }
//
//    private List<String> getListOfLabelsFromConditionSetting(String labelsFromConditionSetting)
//    {
//        JsonArray labelsFromConditionSettingJsonArray = new JsonArray(labelsFromConditionSetting);
//        return getLabelList(labelsFromConditionSettingJsonArray);
//    }
//
//    private List<String> getListOfLabelsFromDataBase(String projectId, String uuid, PortfolioDB portfolioDB)
//            throws ExecutionException, InterruptedException
//    {
//        List<String> listOfLabelsFromDataBase = new ArrayList<>();
//        JsonArray labelsFromDataBaseJsonArray = getLabelFromDataBase(projectId, uuid, portfolioDB);
//
//        if(labelsFromDataBaseJsonArray.size() == 1)
//        {
//            JsonObject jsonObject = labelsFromDataBaseJsonArray.getJsonObject(0);
//            if(jsonObject.getString("labelName").equals(""))
//            {
//                return listOfLabelsFromDataBase;
//            }
//        }
//
//        listOfLabelsFromDataBase = getLabelList(labelsFromDataBaseJsonArray);
//        return listOfLabelsFromDataBase;
//    }
//
//    private List<String> getLabelList(JsonArray labelsJsonArray)
//    {
//        List<String> labelList = new ArrayList<>();
//        for(int i = 0; i < labelsJsonArray.size(); i++) {
//            JsonObject jsonObject = labelsJsonArray.getJsonObject(i);
//            labelList.add(jsonObject.getString("labelName"));
//        }
//        return labelList;
//    }
//
//    private String processExistedLabelsAndConditionLabels(JsonArray labelsFromDataBaseJsonArray, JsonArray labelsFromConditionSettingJsonArray)
//    {
//        List<String> listOfLabelsFromDataBase = getLabelList(labelsFromDataBaseJsonArray);
//        List<String> listOfLabelsFromConditionSetting = getLabelList(labelsFromConditionSettingJsonArray);
//        String labelsListJsonString;
//        JsonArray filteredArray = new JsonArray();
//        boolean containInvalidData = isContainInvalidData(labelsFromDataBaseJsonArray);
//
//        if(!containInvalidData) {
//            List<String> filteredLabels = listOfLabelsFromConditionSetting
//                    .stream()
//                    .filter(label -> !listOfLabelsFromDataBase.contains(label))
//                    .collect(Collectors.toList());
//
//            for(int i = 0; i < labelsFromConditionSettingJsonArray.size(); i++){
//                String label = labelsFromConditionSettingJsonArray.getJsonObject(i).getString("labelName");
//                if(filteredLabels.contains(label)) {
//                    filteredArray.add(labelsFromConditionSettingJsonArray.getJsonObject(i));
//                }
//            }
//            labelsListJsonString = labelsFromDataBaseJsonArray.addAll(filteredArray).encode();
//        } else {
//            labelsListJsonString = labelsFromDataBaseJsonArray.encode();
//        }
//
//        return labelsListJsonString;
//    }
//
//    private boolean isContainInvalidData(JsonArray labelsFromDataBaseJsonArray) {
//        for(int i = 0; i < labelsFromDataBaseJsonArray.size(); i++){
//            String label = labelsFromDataBaseJsonArray.getJsonObject(i).getString("labelName");
//            if(label.equals("Invalid")) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    private String parsePreLabellingCondition(Map<String, Object> conditionMap, JsonObject attributeTypeMap,
//                                              JsonObject tabularDataFromDataBase) throws Exception {
//        String labels = null;
//        ObjectMapper mapper = new ObjectMapper();
//
//        for(String key : conditionMap.keySet())
//        {
//            if(key.equals("threshold"))
//            {
//                String thresholdConditionJsonString = mapper.writeValueAsString(conditionMap.get(key));
//                JsonObject thresholdConditionJsonObject = new JsonObject(thresholdConditionJsonString);
//                labels = processThresholdCondition(thresholdConditionJsonObject, attributeTypeMap, tabularDataFromDataBase);
//            }
//
//            else if (key.equals("range"))
//            {
//                String rangeConditionJsonString = mapper.writeValueAsString(conditionMap.get(key));
//                JsonObject rangeConditionJsonObject = new JsonObject(rangeConditionJsonString);
//                labels = processRangeCondition(rangeConditionJsonObject, attributeTypeMap, tabularDataFromDataBase);
//            }
//        }
//
//        return labels;
//    }
//
//    private String processThresholdCondition(JsonObject thresholdCondition, JsonObject attributeTypeMap,
//                                             JsonObject tabularDataFromDataBase) throws Exception {
//        String attributeName = thresholdCondition.getString("attribute");
//        String attributeType = attributeTypeMap.getString(attributeName);
//        Object attributeValue = tabularDataFromDataBase.getValue(attributeName);
//
//        String conditionOperator = thresholdCondition.getString("operator");
//        Object conditionValue = thresholdCondition.getValue("value");
//        String labels = thresholdCondition.getJsonArray("label").toString();
//        String dateFormat = thresholdCondition.getString("dateFormat");
//
//        String type;
//        if(attributeName.equals("Date")) {
//            type = "Date";
//        } else {
//            type = checkAttributeType(attributeType);
//        }
//
//        boolean matchCondition = checkTrueNessOfCondition(attributeValue, type, "threshold",
//                Collections.singletonList(conditionValue), Collections.singletonList(conditionOperator), dateFormat);
//        return matchCondition ? labels : null;
//    }
//
//    private String processRangeCondition(JsonObject rangeCondition, JsonObject attributeTypeMap,
//                                         JsonObject tabularDataFromDataBase) throws Exception {
//        String attributeName = rangeCondition.getString("attribute");
//        String attributeType = attributeTypeMap.getString(attributeName);
//        Object attributeValue = tabularDataFromDataBase.getValue(attributeName);
//
//        String lowerOperator = rangeCondition.getString("lowerOperator");
//        String upperOperator = rangeCondition.getString("upperOperator");
//        Object lowerLimit = rangeCondition.getValue("lowerLimit");
//        Object upperLimit = rangeCondition.getValue("upperLimit");
//        String labels = rangeCondition.getJsonArray("label").toString();
//        String dateFormat = rangeCondition.getString("dateFormat");
//        boolean matchCondition = checkTrueNessOfCondition(attributeValue, checkAttributeType(attributeType), "range",
//                Arrays.asList(lowerLimit, upperLimit), Arrays.asList(lowerOperator, upperOperator), dateFormat);
//        return matchCondition ? labels : null;
//    }
//
//    private JsonObject getTabularDataFromDataBase(String projectId, String uuid, PortfolioDB portfolioDB)
//            throws ExecutionException, InterruptedException {
//        CompletableFuture<JsonObject> future = new CompletableFuture<>();
//        portfolioDB.getTabularDataByUuid(projectId, uuid)
//                .onComplete(res -> {
//                    if(res.succeeded()) {
//                        future.complete(res.result());
//                    } else {
//                        future.completeExceptionally(res.cause());
//                    }
//                });
//
//        return future.get();
//    }
//
//    private static void updateTabularDataLabelInDatabase(String projectId, String uuid, String labels, PortfolioDB portfolioDB) {
//        JsonObject jsonObject = new JsonObject()
//                .put("uuid", uuid)
//                .put("labels", labels);
//        portfolioDB.updateLabel(projectId, jsonObject);
//    }
//
//    private JsonObject getAttributeTypeFromDataBase(String projectId, PortfolioDB portfolioDB)
//            throws ExecutionException, InterruptedException {
//        CompletableFuture<JsonObject> future = new CompletableFuture<>();
//        portfolioDB.getAttributeTypeMap(projectId)
//                .onComplete(res -> {
//                    if(res.succeeded()) {
//                        future.complete(res.result());
//                    } else {
//                        future.completeExceptionally(res.cause());
//                    }
//                });
//
//        return future.get();
//    }
//
//    private JsonArray getLabelFromDataBase(String projectId, String uuid, PortfolioDB portfolioDB)
//            throws ExecutionException, InterruptedException {
//        CompletableFuture<JsonArray> future = new CompletableFuture<>();
//        portfolioDB.getLabel(projectId, uuid)
//                .onComplete(res -> {
//                    if(res.succeeded()) {
//                        future.complete(res.result());
//                    } else {
//                        future.completeExceptionally(res.cause());
//                    }
//                });
//
//        return future.get();
//    }

    public static String checkAttributeType(String attributeType) {
        String type = "";
        if(attributeType.contains("VARCHAR")) {
            return "String";
        }
        switch(attributeType) {
            case "INT" -> type = "Integer";
            case "DECIMAL" -> type = "Double";
            case "DATE" -> type = "Date";
        }
        return type;
    }

    private static String determineDatePattern(String date) {
        String[] regexDateFormat = {
                "([0-9][0-9]){2}/[0-1]?[0-9]/[0-3]?[0-9]" ,
                "[0-1]?[0-9]/[0-3]?[0-9]/([0-9][0-9]){2}",
                "[0-3]?[0-9]/[0-1]?[0-9]/([0-9][0-9]){2}",
                "[0-1]?[0-9]/[0-3]?[0-9]/([0-9][0-9])?[0-9]{2}",
                "[0-3]?[0-9]/[0-1]?[0-9]/([0-9][0-9])?[0-9]{2}",
        };
        String dateFormat = "";
        String matchFormat = "";

        for(String regex : regexDateFormat) {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(date);
            if(matcher.matches()) {
                matchFormat = regex;
            }
        }

        switch(matchFormat) {
            case "([0-9][0-9]){2}/[0-1]?[0-9]/[0-3]?[0-9]" -> dateFormat = "yyyy-MM-dd";
            case "[0-1]?[0-9]/[0-3]?[0-9]/([0-9][0-9]){2}" -> dateFormat = "MM-dd-yyyy";
            case "[0-3]?[0-9]/[0-1]?[0-9]/([0-9][0-9]){2}" -> dateFormat = "dd-MM-yyyy";
            case "[0-1]?[0-9]/[0-3]?[0-9]/([0-9][0-9])?[0-9]{2}" -> dateFormat = "MM-dd-yy";
            case "[0-3]?[0-9]/[0-1]?[0-9]/([0-9][0-9])?[0-9]{2}" -> dateFormat = "dd-MM-yy";

        }
        return dateFormat;
    }

    private static String convertDateFormat(String date) {
        String regex = "(([0-9][0-9]){2})?([0-2]?[0-9])?[/.-][0-3]?[0-9][/.-]([0-9]{2})?[0-9]{2}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(date);

        if(matcher.matches()) {
            String dateFormatInDataBase = matcher.group();
            String[] stringArray = StringUtils.split(dateFormatInDataBase, "/.-");
            return String.join("/", stringArray);
        }

        return null;
    }

    private <T> boolean checkIsLessThan(T obj1, T obj2) {
        if (obj1.getClass() == Double.class && obj2.getClass() == Double.class) {
            return (Double) obj1 < (Double) obj2;
        }

        if(obj1.getClass() == Integer.class && obj2.getClass() == Integer.class) {
            return (Integer) obj1 < (Integer) obj2;
        }

        if(obj1.getClass() == Date.class && obj2.getClass() == Date.class) {
            return ((Date) obj1).before((Date) obj2);
        }

        return false;
    }

    private <T> boolean checkEquality(T obj1, T obj2) {
        if (obj1.getClass() == Double.class && obj2.getClass() == Double.class) {
            return obj1.equals(obj2);
        }

        if(obj1.getClass() == Integer.class && obj2.getClass() == Integer.class) {
            return obj1.equals(obj2);
        }

        if(obj1.getClass() == String.class && obj2.getClass() == String.class) {
            return ((String) obj1).equalsIgnoreCase((String) obj2);
        }

        if(obj1.getClass() == Date.class && obj2.getClass() == Date.class) {
            return obj1.equals(obj2);
        }

        return false;
    }

    private <T> boolean checkIsMoreThan(T obj1, T obj2) {
        if (obj1.getClass() == Double.class && obj2.getClass() == Double.class) {
            return (Double) obj1 > (Double) obj2;
        }

        if(obj1.getClass() == Integer.class && obj2.getClass() == Integer.class) {
            return (Integer) obj1 > (Integer) obj2;
        }

        if(obj1.getClass() == Date.class && obj2.getClass() == Date.class) {
            return ((Date) obj1).after((Date) obj2);
        }

        return false;
    }

    private <T> boolean compareThresholdCondition(String operator, T sourceValue, T conditionValue) {
        boolean condition = false;

        switch (operator) {
            case "less than" -> condition = checkIsLessThan(sourceValue, conditionValue);
            case "less than or equal to" -> condition = checkIsLessThan(sourceValue, conditionValue) || checkEquality(sourceValue, conditionValue);
            case "equal to" -> condition = checkEquality(sourceValue, conditionValue);
            case "not equal to" -> condition = !checkEquality(sourceValue, conditionValue);
            case "more than" -> condition = checkIsMoreThan(sourceValue, conditionValue);
            case "more than or equal to" -> condition = checkIsMoreThan(sourceValue, conditionValue) || checkEquality(sourceValue, conditionValue);
        }

        return condition;
    }

    private <T> boolean compareRangeCondition(String lowerOperator, String upperOperator, T sourceValue,
                                              T lowerLimit, T upperLimit) {
        boolean condition = false;

        if(lowerOperator.equals("more than") && upperOperator.equals("less than")) {
            condition = checkIsMoreThan(sourceValue, lowerLimit) && checkIsLessThan(sourceValue, upperLimit);
        }

        if(lowerOperator.equals("more than or equal to") && upperOperator.equals("less than")) {
            condition = (checkIsMoreThan(sourceValue, lowerLimit) || checkEquality(sourceValue,lowerLimit))
                    && checkIsLessThan(sourceValue, upperLimit);
        }

        if(lowerOperator.equals("more than") && upperOperator.equals("less than or equal to")) {
            condition = checkIsMoreThan(sourceValue, lowerLimit) && (checkIsLessThan(sourceValue, upperLimit)
                    || checkEquality(sourceValue, upperLimit));
        }

        if(lowerOperator.equals("more than or equal to") && upperOperator.equals("less than or equal to")) {
            condition = (checkIsMoreThan(sourceValue, lowerLimit) || checkEquality(sourceValue, lowerLimit))
                    && (checkIsLessThan(sourceValue, upperLimit) || checkEquality(sourceValue, upperLimit));
        }

        return condition;
    }

    private boolean checkTrueNessOfCondition(Object attributeValue, String attributeType, String conditionType,
                                             List<Object> conditionValueList, List<String> operatorList, String dateFormat) throws Exception {
        boolean check = false;

        if(conditionType.equals("threshold")) {
            String operator = operatorList.get(0);
            switch(attributeType) {
                case "Integer", "Double" -> check = compareThresholdCondition(operator, attributeValue,
                        conditionValueList.get(0));

                case "String" -> check = checkEquality(attributeValue, conditionValueList.get(0));

                case "Date" -> {
                    String dateFromDataBase = convertDateFormat(attributeValue.toString());
                    SimpleDateFormat format = new SimpleDateFormat(dateFormat);
                    detectConsistencyOfDateFormat(dateFromDataBase, dateFormat);
                    Date sourceDate = format.parse(dateFromDataBase);
                    Date conditionDate = format.parse(conditionValueList.get(0).toString());
                    check = compareThresholdCondition(operator, sourceDate, conditionDate);
                }
            }
        }

        if(conditionType.equals("range")) {
            String lowerOperator = operatorList.get(0);
            String upperOperator = operatorList.get(1);

            switch(attributeType) {
                case "Integer", "Double" -> check = compareRangeCondition(lowerOperator, upperOperator, attributeValue,
                        conditionValueList.get(0), conditionValueList.get(1));

                case "Date" -> {
                    SimpleDateFormat format = new SimpleDateFormat(dateFormat);
                    String dateFromDataBase = convertDateFormat(attributeValue.toString());
                    Date sourceDate = format.parse(dateFromDataBase);
                    Date lowerLimitDate = format.parse(conditionValueList.get(0).toString());
                    Date upperLimitDate = format.parse(conditionValueList.get(1).toString());
                    check = compareRangeCondition(lowerOperator, upperOperator, sourceDate, lowerLimitDate, upperLimitDate);
                }
            }
        }

        return check;
    }

    private static void detectConsistencyOfDateFormat(String sourceDate, String dateFormat) throws Exception {
        String regexString = "";
        switch (dateFormat) {
            case "yyyy/MM/dd":
                regexString = "([0-9][0-9]){2}/[0-1]?[0-9]/[0-3]?[0-9]";
                break;
            case "dd/MM/yyyy":
                regexString = "[0-3]?[0-9]/[0-1]?[0-9]/([0-9][0-9])?[0-9]{2}";
                break;
            case "MM/dd/yyyy":
                regexString = "[0-1]?[0-9]/[0-3]?[0-9]/([0-9][0-9])?[0-9]{2}";
                break;
        }

        Pattern pattern = Pattern.compile(regexString);
        Matcher matcher = pattern.matcher(sourceDate);

        if(!matcher.matches()) {
            throw new Exception("Date format error. The date format is not tally with the source date.");
        }
    }
}
