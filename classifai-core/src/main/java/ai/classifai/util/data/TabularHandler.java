package ai.classifai.util.data;

import ai.classifai.database.annotation.AnnotationDB;
import ai.classifai.database.annotation.TabularAnnotationQuery;
import ai.classifai.database.portfolio.PortfolioDB;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.ui.enums.FileSystemStatus;
import ai.classifai.util.project.ProjectHandler;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStrings;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.simpleflatmapper.csv.CsvParser;
import org.simpleflatmapper.lightningcsv.CsvReader;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Tabular data handling
 *
 * @author ken479
 */

@Slf4j
@NoArgsConstructor
public class TabularHandler {
    private static String delimiter;
    private static List<String> headerNames = new LinkedList<>();
    private static Map<String, String> headers = new LinkedHashMap<>();
    private static Integer columnNumbers = 0;
    private static List<String[]> rowsData = new ArrayList<>();
    private static JsonArray jsonArray;
    private static String processingUuid = "";

    private static void checkDelimiter(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String headerString = reader.readLine();

        if (headerString.contains(",")) {
            delimiter = ",";
            headerNames = Arrays.asList(headerString.split(",", -1));
            modifyColumnNameString();
        }

        else if (headerString.contains(";")) {
            delimiter = ";";
            headerNames = Arrays.asList(headerString.split(";", -1));
            modifyColumnNameString();
        }
        else if (headerString.contains("|")) {
            delimiter = "|";
            headerNames = Arrays.asList(headerString.split("\\|", -1));
            modifyColumnNameString();
        }

        else if (headerString.contains("\t")) {
            delimiter = "\t";
            headerNames = Arrays.asList(headerString.split("\t", -1));
            modifyColumnNameString();
        }

    }

    private static void modifyColumnNameString() {
        for(int i = 0; i < headerNames.size(); i++) {
            if(detectSpaceInString(headerNames.get(i)) || detectParenthesisInString(headerNames.get(i))) {
                headerNames.set(i, "\"" + headerNames.get(i) + "\"");
            }
        }
    }

    private static boolean detectSpaceInString(String headerName) {
        Pattern pattern = Pattern.compile("[\\w\\s]+");
        Matcher matcher = pattern.matcher(headerName);
        return matcher.matches();
    }

    private static boolean detectParenthesisInString(String headerName) {
        Pattern pattern = Pattern.compile("\\w+\\s\\(?(\\w+)?(\\s)?(\\p{Sc})?\\)?");
        Matcher matcher = pattern.matcher(headerName);
        return matcher.matches();
    }

    private static Map<String, String> mapHeaderNamesToHeaderTypes(List<String> keys, List<String> values) {
        return IntStream.range(0, keys.size())
                .collect(LinkedHashMap::new, (x, i) -> x.put(keys.get(i), values.get(i)), Map::putAll);
    }

    private static List<String[]> readAllData(String filePath) {
        try(Stream<String> stream = Files.lines(Paths.get(filePath))) {
            return stream.map(x -> x.split(delimiter, -1)).collect(Collectors.toCollection(LinkedList::new));
        } catch (IOException e) {
            log.info("Error in reading csv file");
        }
        return null;
    }

    private static boolean isDateObject(String obj) {
        String regex = "(([0-9][0-9]){2})?([0-2]?[0-9])?[/|-][0-1]?[0-9][/|-]([0-9]{2})?[0-9]{2}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(obj);
        return matcher.matches();
    }

    public static List<Map<String, String>> identifyEachDataType(String[] dataList) {
        List<Map<String, String>> list = new LinkedList<>();

        for (String object : dataList) {
            Map<String, String> map = new HashMap<>();
            if (object != null && isDateObject(object))
            {
                map.put(object, "VARCHAR(100)");
            }

            else if(object != null && !isDateObject(object)) {
                if (NumberUtils.isCreatable(object)) {
                    try {
                        if (Integer.valueOf(object).equals(Integer.parseInt(object))) {
                            map.put(object, "INT");
                        }
                    } catch (NumberFormatException e) {
                        if (Double.valueOf(object).equals(Double.parseDouble(object))) {
                            map.put(object, "DECIMAL");
                        } else {
                            log.info("Number format error. Data is not numeric");
                        }
                    }
                }

                else {
                    map.put(object, "VARCHAR(2000)");
                }
            }

            if(object == null) {
                map.put(null, "VARCHAR(2000)");
            }
            list.add(map);
        }

        return list;
    }

    private static void identifyHeadersTypes(List<String[]> dataFromFile) {
        List<String> headerTypes;
        List<Map<String, String>> listOfMap = new LinkedList<>();

        for(String[] array : dataFromFile) {
            listOfMap.add(extractTypes(identifyEachDataType(array), dataFromFile));
        }

        headerTypes = extractTypesFromLoop(listOfMap);
        headers =  mapHeaderNamesToHeaderTypes(headerNames, headerTypes);
        columnNumbers = headers.size() + 5;
    }

    private static Map<String, String> extractTypes(List<Map<String, String>> mapDataType, List<String[]> dataFromFile) {
        List<String> columnName = Arrays.asList(dataFromFile.get(0));
        List<String> list = new LinkedList<>();
        Map<String, String> map = new LinkedHashMap<>();
        for(Map<String, String> dataTypeMap : mapDataType) {
            for(Map.Entry<String, String> entry : dataTypeMap.entrySet()) {
                list.add(entry.getValue());
            }
        }

        for(int i = 0; i < list.size(); i++) {
            map.put(columnName.get(i), list.get(i));
        }
        return map;
    }

    private static LinkedList<String> extractTypesFromLoop(List<Map<String, String>> listOfMap) {
        Map<String, Map<String, Integer>> countMap = new LinkedHashMap<>();
        Map<String, String> nameMap = new LinkedHashMap<>();

        // name : {value: count, value: count}
        for(Map<String, String> map : listOfMap) {
            for(Map.Entry<String, String> entry : map.entrySet()) {
                Map<String, Integer> temp = new LinkedHashMap<>();
                if(!countMap.containsKey(entry.getKey())) {
                    temp.put(entry.getValue(), 1);
                    countMap.put(entry.getKey(), temp);
                } else {
                    Map<String, Integer> value = countMap.get(entry.getKey());
                    if(value.containsKey(entry.getValue())) {
                        Integer count = value.get(entry.getValue()) + 1;
                        value.put(entry.getValue(), count);
                        countMap.put(entry.getKey(), value);
                    }
                    else if(!value.containsKey(entry.getKey())){
                        value.put(entry.getValue(), 1);
                        countMap.put(entry.getKey(), value);
                    }
                }
            }
        }

        for(Map.Entry<String, Map<String, Integer>> entry : countMap.entrySet()) {
            Map<String, Integer> valueCount = entry.getValue();

            if(valueCount.containsKey("DECIMAL")) {
                nameMap.put(entry.getKey(), "DECIMAL");
            }

            else if(valueCount.containsKey("INT") && !valueCount.containsKey("DECIMAL")) {
                nameMap.put(entry.getKey(), "INT");
            }

            else {
                Map.Entry<String, Integer> map = null;
                for(Map.Entry<String, Integer> entryMap : valueCount.entrySet()){
                    if(map == null || entryMap.getValue().compareTo(map.getValue()) > 0) {
                        map = entryMap;
                    }
                }
                assert map != null;
                nameMap.put(entry.getKey(), map.getKey());
            }

        }
        return new LinkedList<>(nameMap.values());
    }

    /**
     * Parse CSV File
     */
    public void readCsvFile(String filePath, ProjectLoader loader, AnnotationDB annotationDB) throws IOException, CsvException {
        checkDelimiter(filePath);
        char separator = delimiter.charAt(0);

        CSVParser csvParser = new CSVParserBuilder()
                .withSeparator(separator)
                .build();

        CSVReader csvReader = new CSVReaderBuilder(Files.newBufferedReader(Paths.get(filePath)))
                .withCSVParser(csvParser)
                .build();

        rowsData = csvReader.readAll();
        identifyHeadersTypes(rowsData);

        TabularAnnotationQuery.createProjectTablePreparedStatement(headers, loader);
        createTabularProjectTable(loader, annotationDB);
    }

    public static void saveToTabularProjectTable(@NonNull AnnotationDB annotationDB, @NonNull ProjectLoader loader)
    {
        TabularAnnotationQuery.createDataPreparedStatement(loader, columnNumbers);
        File tabularFilePath = loader.getTabularFilePath();
        loader.resetFileSysProgress(FileSystemStatus.DATABASE_UPDATING);
        loader.setFileSysTotalUUIDSize(rowsData.size() - 1);

        for (int i = 1; i < rowsData.size(); i++) {
            String dataSubPath = FilenameUtils.getName(tabularFilePath.toString());
            annotationDB.saveTabularData(loader, ensureCorrectTypeInList(rowsData.get(i)), dataSubPath, i);
        }

    }

    /* Handling missing data:
    *Not handling date type is due to user may set date value to such as none or empty for the date
    * so, stay with string to store in database and do the parse whenX retrieve
    */

    private static String typeCheckingPushedData(String data, Integer currentIndex) {
        String currentName = headerNames.get(currentIndex);
        String type = checkAttributeType(headers.get(currentName));
        String currentData = data;
        // Missing value handling
        switch (type) {
            case "Integer" -> {
                try {
                    Integer.valueOf(data).equals(Integer.parseInt(data));
                } catch (Exception ignore) {
                    currentData = "0";
                }
            }

            case "Double" -> {
                try {
                    Double.valueOf(data).equals(Double.parseDouble(data));
                } catch (Exception ignore) {
                    currentData = "0.0";
                }
            }

            case "String" -> {
                if(data.isEmpty() || data.isBlank()) {
                    currentData = "Empty data";
                }
            }
        }

        return currentData;
    }

    private static String[] ensureCorrectTypeInList(String[] rowsData) {
        String[] data = new String[rowsData.length];
        for(int i = 0; i < rowsData.length; i++) {
            data[i] = typeCheckingPushedData(rowsData[i], i);
        }
        return data;
    }

    private static void createTabularProjectTable(ProjectLoader loader, AnnotationDB annotationDB) {
        annotationDB.createTabularProjectTable(loader);
    }

    /**
     * Parse Excel File
     */
    public void readExcelFile(String filePath, ProjectLoader loader, AnnotationDB annotationDB, String fileExtension) throws IOException {
        String fileName = new File(filePath).getName();
        FileInputStream fileInputStream = new FileInputStream(filePath);
        Workbook workbook;
        Map<String, String> headersTypeMap = new LinkedHashMap<>();

        switch(fileExtension){
            case "xlsx" -> workbook = new XSSFWorkbook(fileInputStream);
            case "xls" -> workbook = new HSSFWorkbook(fileInputStream);
            default -> throw new IllegalStateException("Unexpected file extension: " + fileExtension);
        }

        try {
            Sheet sheet = workbook.getSheetAt(0);

            // to get a list of headers
            int first = sheet.getFirstRowNum();
            Row headersRow = sheet.getRow(first);
            for (int index = headersRow.getFirstCellNum(); index < headersRow.getLastCellNum(); index++) {
                headerNames.add(headersRow.getCell(index).getStringCellValue());
            }

            int firstRow = sheet.getFirstRowNum();
            int lastRow = sheet.getLastRowNum();
            List<String> objectArrayList = new ArrayList<>();

            for (int i = firstRow + 1; i <= lastRow; i++) {
                Row row = sheet.getRow(i);
                for (int j = row.getFirstCellNum(); j < row.getLastCellNum(); j++) {
                    Cell cell = row.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    CellType cellType;

                    if(cell.getCellType() == CellType.FORMULA) {
                        cellType = cell.getCachedFormulaResultType();
                    } else {
                        cellType = cell.getCellType();
                    }

                    switch (cellType) {
                        case STRING:
                        case BLANK:
                            objectArrayList.add(String.valueOf(cell.getStringCellValue()));
                            headersTypeMap.put(headerNames.get(j), "VARCHAR(2000)");
                            break;
                        case NUMERIC:
                            if (DateUtil.isCellDateFormatted(cell)) {
                                objectArrayList.add(String.valueOf(cell.getDateCellValue()));
                                headersTypeMap.put(headerNames.get(j), "DATE");
                            } else {
                                int value = (int) cell.getNumericCellValue();
                                if (cell.getNumericCellValue() - value == 0.0) {
                                    objectArrayList.add(String.valueOf(value));
                                    headersTypeMap.put(headerNames.get(j), "INT");
                                } else {
                                    objectArrayList.add(String.valueOf(cell.getNumericCellValue()));
                                    headersTypeMap.put(headerNames.get(j), "DECIMAL");
                                }
                            }
                            break;
                        case BOOLEAN:
                            objectArrayList.add(String.valueOf(cell.getBooleanCellValue()));
                            headersTypeMap.put(headerNames.get(j), "VARCHAR(5)");
                            break;
                        case ERROR:
                            FormulaError formulaError = FormulaError.forInt(cell.getErrorCellValue());
                            log.error("Error happens when processing " +  fileName + ". Identified formula error: " + formulaError.toString());
                            log.error("Please correct the error found in " + fileName);
                            return;
//                            objectArrayList.add((formulaError == null) ? null : formulaError.toString());
//                            headersTypeMap.put(headerNames.get(j), "VARCHAR(20)");
                    }
                }
                String[] stringArray = objectArrayList.toArray(String[]::new);
                rowsData.add(stringArray);
                objectArrayList.clear();
            }
            headers.putAll(headersTypeMap);
            fileInputStream.close();

        } catch (Exception e) {
            log.info("Excel file not found");
        }

        columnNumbers = headers.size() + 5;
        TabularAnnotationQuery.createProjectTablePreparedStatement(headers, loader);
        createTabularProjectTable(loader, annotationDB);
    }

    private static void printData(List<String[]> rowsData) {
        for (String[] s : rowsData) {
            log.info(Arrays.toString(s));
        }
    }

    private void printCellValue(Cell cell) {
        CellType cellType = cell.getCellType().equals(CellType.FORMULA)
                ? cell.getCachedFormulaResultType() : cell.getCellType();
        if (cellType.equals(CellType.STRING)) {
            log.info(cell.getStringCellValue() + "\t\t");
        }
        else if (cellType.equals(CellType.NUMERIC)) {
            if (DateUtil.isCellDateFormatted(cell)) {
                log.info(cell.getDateCellValue() + "\t\t");
            } else {
                log.info(cell.getNumericCellValue() + "\t\t");
            }
        }
        else if (cellType.equals(CellType.BOOLEAN)) {
            log.info(cell.getBooleanCellValue() + "\t\t");
        }
    }

    /**
     * Parse Large Excel File
     */
    public void readLargeExcelFile(String filePath, ProjectLoader loader, AnnotationDB annotationDB) throws IOException,
            OpenXML4JException, SAXException, ParserConfigurationException {
        OPCPackage opcPackage = OPCPackage.open(new File(filePath));
        XSSFReader xssfReader = new XSSFReader(opcPackage);
        SharedStrings sharedStringsTable = xssfReader.getSharedStringsTable();

        XMLReader parser = fetchSheetParser(sharedStringsTable);
        InputStream sheet1 = xssfReader.getSheet("rId1");
        InputSource sheetSource = new InputSource(sheet1);
        parser.parse(sheetSource);
        sheet1.close();

    }

    public static XMLReader fetchSheetParser(SharedStrings sst) throws SAXException, ParserConfigurationException {
        XMLReader parser = XMLHelper.newXMLReader();
        ContentHandler handler = new SheetHandler(sst);
        parser.setContentHandler(handler);
        return parser;
    }

    private static class SheetHandler extends DefaultHandler {
        private final SharedStrings sst;
        private String lastContents;
        private boolean nextIsString;

        private SheetHandler(SharedStrings sst) {
            this.sst = sst;
        }

        public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
            // c => cell
            if(name.equals("c")) {
                // Print the cell reference
                System.out.print(attributes.getValue("r") + " - ");
                // Figure out if the value is an index in the SST
                String cellType = attributes.getValue("t");
                nextIsString = cellType != null && cellType.equals("s");
            }
            // Clear contents cache
            lastContents = "";
        }

        public void endElement(String uri, String localName, String name) throws SAXException {
            // Process the last contents as required.
            // Do now, as characters() may be called more than once
            if(nextIsString) {
                int idx = Integer.parseInt(lastContents);
                lastContents = sst.getItemAt(idx).getString();
                nextIsString = false;
            }
            // v => contents of a cell
            // Output after we've seen the string contents
            if(name.equals("v")) {
                System.out.println(lastContents);
            }
        }

        public void characters(char[] ch, int start, int length) {
            lastContents += new String(ch, start, length);
        }
    }

    public void parseCsvToJson(String filePath) throws IOException {
        CsvReader reader = CsvParser.reader(new FileReader(filePath));

        JsonFactory jsonFactory = new JsonFactory();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Iterator<String[]> iterator = reader.iterator();
        String[] headers = iterator.next();

        try (JsonGenerator jsonGenerator = jsonFactory.createGenerator(outputStream)) {

            jsonGenerator.writeStartArray();

            while (iterator.hasNext()) {
                jsonGenerator.writeStartObject();
                String[] values = iterator.next();
                int nbCells = Math.min(values.length, headers.length);
                for(int i = 0; i < nbCells; i++) {
                    jsonGenerator.writeFieldName(headers[i]);
                    jsonGenerator.writeString(values[i]);
                }
                jsonGenerator.writeEndObject();
            }
            jsonGenerator.writeEndArray();
        }

        jsonArray = new JsonArray(outputStream.toString());
    }

    public static JsonArray getTabularData() {
        return jsonArray;
    }

    public static String getColumnNames() {
        return StringUtils.join(headerNames, ',');
    }

    public static String getAttributeTypesJsonString() {
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

    /**
     * Automated Labelling
     *
     */
//    public void initiateAutomaticLabellingForTabular(String projectId, JsonObject preLabellingConditions, List<String> uuidList, String labellingMode,
//                                                     PortfolioDB portfolioDB) throws Exception {
    public void initiateAutomaticLabellingForTabular(String projectId, JsonObject preLabellingConditions, String labellingMode,
                                                     PortfolioDB portfolioDB, ProjectHandler projectHandler) throws Exception {
        JsonObject attributeTypeMap = getAttributeTypeFromDataBase(projectId, portfolioDB);
        ProjectLoader loader = projectHandler.getProjectLoader(projectId);
        List<String> uuidList = loader.getUuidListFromDb();
        if(preLabellingConditions != null)
        {
            for(String uuid : uuidList)
            {
                for(int i = 0; i < preLabellingConditions.size(); i++)
                {
                    JsonObject tabularDataFromDataBase = getTabularDataFromDataBase(projectId, uuid, portfolioDB);
                    JsonObject condition = preLabellingConditions.getJsonObject(String.valueOf(i));
                    String labelsFromConditionSetting = parsePreLabellingCondition(condition.getMap(), attributeTypeMap,tabularDataFromDataBase);
                    List<String> listOfLabelsFromDataBase = getListOfLabelsFromDataBase(projectId, uuid, portfolioDB);

                    if(labelsFromConditionSetting != null) {
                        // update labels directly if labels from database is null or selected mode is overwrite
                        if(listOfLabelsFromDataBase.size() == 0 || labellingMode.equals("overwrite")) {
                            if(!isContainInvalidData(getLabelFromDataBase(projectId,uuid, portfolioDB))) {
                                updateTabularDataLabelInDatabase(projectId, uuid, labelsFromConditionSetting, portfolioDB);
                            }
                        }

                        // update labels on appending the labels
                        else if (labellingMode.equals("append")){
                            JsonArray labelsFromDataBaseJsonArray = getLabelFromDataBase(projectId, uuid, portfolioDB);
                            JsonArray labelsFromConditionSettingJsonArray = parseLabelsJsonStringToJsonArray(labelsFromConditionSetting);
                            String distinctLabels = processExistedLabelsAndConditionLabels(labelsFromDataBaseJsonArray,
                                    labelsFromConditionSettingJsonArray);
                            updateTabularDataLabelInDatabase(projectId, uuid, distinctLabels, portfolioDB);
                        }
                    }
                }
//                processingUuid = uuid;
            }
//            processingUuid = "";
        }
    }

    public Boolean checkIsCurrentUuidFinished(String currentUuid) {
        return currentUuid.equals(processingUuid);
    }

    private JsonArray parseLabelsJsonStringToJsonArray(String labelsJsonString) {
        JsonArray labelsListJsonArray = new JsonArray();
        JSONArray labelsJsonArray = new JSONArray(labelsJsonString);

        processJSONArrayToJsonArray(labelsJsonArray, labelsListJsonArray);
        return labelsListJsonArray;
    }

    public static void processJSONArrayToJsonArray(JSONArray jsonArray, JsonArray vertxJsonArray) {
        for(int i = 0; i < jsonArray.length(); i++) {
            JsonObject jsonObject = new JsonObject()
                    .put("labelName", jsonArray.getJSONObject(i).get("labelName"))
                    .put("tagColor", jsonArray.getJSONObject(i).get("tagColor"));
            vertxJsonArray.add(jsonObject);
        }
    }

    private List<String> getListOfLabelsFromConditionSetting(String labelsFromConditionSetting)
    {
        JsonArray labelsFromConditionSettingJsonArray = new JsonArray(labelsFromConditionSetting);
        return getLabelList(labelsFromConditionSettingJsonArray);
    }

    private List<String> getListOfLabelsFromDataBase(String projectId, String uuid, PortfolioDB portfolioDB)
            throws ExecutionException, InterruptedException
    {
        List<String> listOfLabelsFromDataBase = new ArrayList<>();
        JsonArray labelsFromDataBaseJsonArray = getLabelFromDataBase(projectId, uuid, portfolioDB);

        if(labelsFromDataBaseJsonArray.size() == 1)
        {
            JsonObject jsonObject = labelsFromDataBaseJsonArray.getJsonObject(0);
            if(jsonObject.getString("labelName").equals(""))
            {
                return listOfLabelsFromDataBase;
            }
        }

        listOfLabelsFromDataBase = getLabelList(labelsFromDataBaseJsonArray);
        return listOfLabelsFromDataBase;
    }

    private List<String> getLabelList(JsonArray labelsJsonArray)
    {
        List<String> labelList = new ArrayList<>();
        for(int i = 0; i < labelsJsonArray.size(); i++) {
            JsonObject jsonObject = labelsJsonArray.getJsonObject(i);
            labelList.add(jsonObject.getString("labelName"));
        }
        return labelList;
    }

    private String processExistedLabelsAndConditionLabels(JsonArray labelsFromDataBaseJsonArray, JsonArray labelsFromConditionSettingJsonArray)
    {
        List<String> listOfLabelsFromDataBase = getLabelList(labelsFromDataBaseJsonArray);
        List<String> listOfLabelsFromConditionSetting = getLabelList(labelsFromConditionSettingJsonArray);
        String labelsListJsonString;
        JsonArray filteredArray = new JsonArray();
        boolean containInvalidData = isContainInvalidData(labelsFromDataBaseJsonArray);

        if(!containInvalidData) {
            List<String> filteredLabels = listOfLabelsFromConditionSetting
                    .stream()
                    .filter(label -> !listOfLabelsFromDataBase.contains(label))
                    .collect(Collectors.toList());

            for(int i = 0; i < labelsFromConditionSettingJsonArray.size(); i++){
                String label = labelsFromConditionSettingJsonArray.getJsonObject(i).getString("labelName");
                if(filteredLabels.contains(label)) {
                    filteredArray.add(labelsFromConditionSettingJsonArray.getJsonObject(i));
                }
            }
            labelsListJsonString = labelsFromDataBaseJsonArray.addAll(filteredArray).encode();
        } else {
            labelsListJsonString = labelsFromDataBaseJsonArray.encode();
        }

        return labelsListJsonString;
    }

    private boolean isContainInvalidData(JsonArray labelsFromDataBaseJsonArray) {
        for(int i = 0; i < labelsFromDataBaseJsonArray.size(); i++){
            String label = labelsFromDataBaseJsonArray.getJsonObject(i).getString("labelName");
            if(label.equals("Invalid")) {
                return true;
            }
        }
        return false;
    }

    private String parsePreLabellingCondition(Map<String, Object> conditionMap, JsonObject attributeTypeMap,
                                              JsonObject tabularDataFromDataBase) throws Exception {
        String labels = null;
        ObjectMapper mapper = new ObjectMapper();

        for(String key : conditionMap.keySet())
        {
            if(key.equals("threshold"))
            {
                String thresholdConditionJsonString = mapper.writeValueAsString(conditionMap.get(key));
                JsonObject thresholdConditionJsonObject = new JsonObject(thresholdConditionJsonString);
                labels = processThresholdCondition(thresholdConditionJsonObject, attributeTypeMap, tabularDataFromDataBase);
            }

            else if (key.equals("range"))
            {
                String rangeConditionJsonString = mapper.writeValueAsString(conditionMap.get(key));
                JsonObject rangeConditionJsonObject = new JsonObject(rangeConditionJsonString);
                labels = processRangeCondition(rangeConditionJsonObject, attributeTypeMap, tabularDataFromDataBase);
            }
        }

        return labels;
    }

    private String processThresholdCondition(JsonObject thresholdCondition, JsonObject attributeTypeMap,
                                             JsonObject tabularDataFromDataBase) throws Exception {
        String attributeName = thresholdCondition.getString("attribute");
        String attributeType = attributeTypeMap.getString(attributeName);
        Object attributeValue = tabularDataFromDataBase.getValue(attributeName);

        String conditionOperator = thresholdCondition.getString("operator");
        Object conditionValue = thresholdCondition.getValue("value");
        String labels = thresholdCondition.getJsonArray("label").toString();
        String dateFormat = thresholdCondition.getString("dateFormat");

        String type;
        if(attributeName.equals("Date")) {
            type = "Date";
        } else {
            type = checkAttributeType(attributeType);
        }

        boolean matchCondition = checkTrueNessOfCondition(attributeValue, type, "threshold",
                Collections.singletonList(conditionValue), Collections.singletonList(conditionOperator), dateFormat);
        return matchCondition ? labels : null;
    }

    private String processRangeCondition(JsonObject rangeCondition, JsonObject attributeTypeMap,
                                         JsonObject tabularDataFromDataBase) throws Exception {
        String attributeName = rangeCondition.getString("attribute");
        String attributeType = attributeTypeMap.getString(attributeName);
        Object attributeValue = tabularDataFromDataBase.getValue(attributeName);

        String lowerOperator = rangeCondition.getString("lowerOperator");
        String upperOperator = rangeCondition.getString("upperOperator");
        Object lowerLimit = rangeCondition.getValue("lowerLimit");
        Object upperLimit = rangeCondition.getValue("upperLimit");
        String labels = rangeCondition.getJsonArray("label").toString();
        String dateFormat = rangeCondition.getString("dateFormat");
        boolean matchCondition = checkTrueNessOfCondition(attributeValue, checkAttributeType(attributeType), "range",
                Arrays.asList(lowerLimit, upperLimit), Arrays.asList(lowerOperator, upperOperator), dateFormat);
        return matchCondition ? labels : null;
    }

    private JsonObject getTabularDataFromDataBase(String projectId, String uuid, PortfolioDB portfolioDB)
            throws ExecutionException, InterruptedException {
        CompletableFuture<JsonObject> future = new CompletableFuture<>();
        portfolioDB.getTabularDataByUuid(projectId, uuid)
                .onComplete(res -> {
                    if(res.succeeded()) {
                        future.complete(res.result());
                    } else {
                        future.completeExceptionally(res.cause());
                    }
                });

        return future.get();
    }

    private static void updateTabularDataLabelInDatabase(String projectId, String uuid, String labels, PortfolioDB portfolioDB) {
        JsonObject jsonObject = new JsonObject()
                .put("uuid", uuid)
                .put("labels", labels);
        portfolioDB.updateLabel(projectId, jsonObject);
    }

    private JsonObject getAttributeTypeFromDataBase(String projectId, PortfolioDB portfolioDB)
            throws ExecutionException, InterruptedException {
        CompletableFuture<JsonObject> future = new CompletableFuture<>();
        portfolioDB.getAttributeTypeMap(projectId)
                .onComplete(res -> {
                    if(res.succeeded()) {
                        future.complete(res.result());
                    } else {
                        future.completeExceptionally(res.cause());
                    }
                });

        return future.get();
    }

    private JsonArray getLabelFromDataBase(String projectId, String uuid, PortfolioDB portfolioDB)
            throws ExecutionException, InterruptedException {
        CompletableFuture<JsonArray> future = new CompletableFuture<>();
        portfolioDB.getLabel(projectId, uuid)
                .onComplete(res -> {
                    if(res.succeeded()) {
                        future.complete(res.result());
                    } else {
                        future.completeExceptionally(res.cause());
                    }
                });

        return future.get();
    }

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
