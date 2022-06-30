package ai.classifai.core.data.type.tabular;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TabularUtils {
    public Map<String, List<String>> getDelimiterAndHeaderNamesFromCsvFile(String filePath) throws IOException {
        String delimiter = null;
        List<String> headerNames = new LinkedList<>();
        Map<String, List<String>> headerNamesDelimiterMap = new HashMap<>();

        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String headerString = reader.readLine();

        if (headerString.contains(",")) {
            delimiter = ",";
            headerNames = Arrays.asList(headerString.split(",", -1));
        }

        else if (headerString.contains(";")) {
            delimiter = ";";
            headerNames = Arrays.asList(headerString.split(";", -1));
        }
        else if (headerString.contains("|")) {
            delimiter = "|";
            headerNames = Arrays.asList(headerString.split("\\|", -1));
        }

        else if (headerString.contains("\t")) {
            delimiter = "\t";
            headerNames = Arrays.asList(headerString.split("\t", -1));
        }

        modifyColumnNameString(headerNames);
        headerNamesDelimiterMap.put("headerNames", headerNames);
        headerNamesDelimiterMap.put("delimiter", Collections.singletonList(delimiter));
        return headerNamesDelimiterMap;
    }

    private void modifyColumnNameString(List<String> headerNames) {
        for(int i = 0; i < headerNames.size(); i++) {
            if(detectSpaceInString(headerNames.get(i)) || detectParenthesisInString(headerNames.get(i))) {
                headerNames.set(i, "\"" + headerNames.get(i) + "\"");
            }
        }
    }

    private Boolean detectSpaceInString(String headerName) {
        Pattern pattern = Pattern.compile("[\\w\\s]+");
        Matcher matcher = pattern.matcher(headerName);
        return matcher.matches();
    }

    private Boolean detectParenthesisInString(String headerName) {
        Pattern pattern = Pattern.compile("\\w+\\s\\(?(\\w+)?(\\s)?(\\p{Sc})?\\)?");
        Matcher matcher = pattern.matcher(headerName);
        return matcher.matches();
    }

    public Boolean isDateObject(String obj) {
        String regex = "(([0-9][0-9]){2})?([0-2]?[0-9])?[/|-][0-1]?[0-9][/|-]([0-9]{2})?[0-9]{2}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(obj);
        return matcher.matches();
    }

    /* Handling missing data:
    * Not handling date type is due to user may set date value to such as none or empty for the date
    * so, stay with string to store in database and do the parse whenX retrieve
    */
    public String typeCheckingPushedData(String data, Integer currentIndex, List<String> headerNames, Map<String, String> headers) {
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

    public String checkAttributeType(String attributeType) {
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

//    public void parseCsvToJson(String filePath) throws IOException {
//        CsvReader reader = CsvParser.reader(new FileReader(filePath));
//
//        JsonFactory jsonFactory = new JsonFactory();
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//
//        Iterator<String[]> iterator = reader.iterator();
//        String[] headers = iterator.next();
//
//        try (JsonGenerator jsonGenerator = jsonFactory.createGenerator(outputStream)) {
//
//            jsonGenerator.writeStartArray();
//
//            while (iterator.hasNext()) {
//                jsonGenerator.writeStartObject();
//                String[] values = iterator.next();
//                int nbCells = Math.min(values.length, headers.length);
//                for(int i = 0; i < nbCells; i++) {
//                    jsonGenerator.writeFieldName(headers[i]);
//                    jsonGenerator.writeString(values[i]);
//                }
//                jsonGenerator.writeEndObject();
//            }
//            jsonGenerator.writeEndArray();
//        }
//
//        return new JsonArray(outputStream.toString());
//    }

}
