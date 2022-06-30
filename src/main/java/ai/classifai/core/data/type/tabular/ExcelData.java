package ai.classifai.core.data.type.tabular;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

@Slf4j
public class ExcelData {
    @Getter
    private List<String> headerNames = null;

    public List<String[]> readExcelFile(String filePath, String fileExtension) throws IOException {
        List<String[]> rowsData = new ArrayList<>();
        String fileName = new File(filePath).getName();
        FileInputStream fileInputStream = new FileInputStream(filePath);
        Workbook workbook;


        switch(fileExtension){
            case "xlsx" -> workbook = new XSSFWorkbook(fileInputStream);
            case "xls" -> workbook = new HSSFWorkbook(fileInputStream);
            default -> throw new IllegalStateException("Unexpected file extension: " + fileExtension);
        }

        try {
            Sheet sheet = workbook.getSheetAt(0);
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
                            break;
                        case NUMERIC:
                            if (DateUtil.isCellDateFormatted(cell)) {
                                objectArrayList.add(String.valueOf(cell.getDateCellValue()));
                            } else {
                                int value = (int) cell.getNumericCellValue();
                                if (cell.getNumericCellValue() - value == 0.0) {
                                    objectArrayList.add(String.valueOf(value));
                                } else {
                                    objectArrayList.add(String.valueOf(cell.getNumericCellValue()));
                                }
                            }
                            break;
                        case BOOLEAN:
                            objectArrayList.add(String.valueOf(cell.getBooleanCellValue()));
                            break;
                        case ERROR:
                            FormulaError formulaError = FormulaError.forInt(cell.getErrorCellValue());
                            log.error("Error happens when processing " +  fileName + ". Identified formula error: " + formulaError.toString());
                            log.error("Please correct the error found in " + fileName);
                            break;
                    }
                }
                String[] stringArray = objectArrayList.toArray(String[]::new);
                rowsData.add(stringArray);
                objectArrayList.clear();
            }
            fileInputStream.close();

        } catch (Exception e) {
            log.info("Excel file not found");
        }
        return rowsData;
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
}
