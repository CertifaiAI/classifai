package ai.classifai.core.data.type.tabular;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import lombok.Getter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class CsvData {
    private final TabularUtils tabularUtils = new TabularUtils();

    @Getter
    private List<String> headerNames = null;

    public List<String[]> readCsvFile(String filePath) throws IOException, CsvException {
        Map<String, List<String>> headerNamesDelimiterMap = tabularUtils.getDelimiterAndHeaderNamesFromCsvFile(filePath);
        char separator = headerNamesDelimiterMap.get("delimiter").get(0).charAt(0);
        headerNames = headerNamesDelimiterMap.get("headerNames");

        CSVParser csvParser = new CSVParserBuilder()
                .withSeparator(separator)
                .build();

        CSVReader csvReader = new CSVReaderBuilder(Files.newBufferedReader(Paths.get(filePath)))
                .withCSVParser(csvParser)
                .build();

        return csvReader.readAll();
    }



}
