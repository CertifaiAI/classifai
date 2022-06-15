package ai.classifai.backend.repository.query;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Create Custom Query for Each Tabular Project Table
 *
 * @author ken479
 */
@Slf4j
public class TabularAnnotationQuery
{
    @Getter private static final String createProjectAttributeTableQuery;
    @Getter private static final String updateProjectAttributeTableQuery;
    @Getter private static final String projectAttributeQuery;
    @Getter private static final String attributeTypeMapQuery;
    @Getter private static final String getDeleteProjectAttributeQuery;
    @Getter private static String createProjectTableQuery;
    @Getter private static String getAllDataQuery;
    @Getter private static String getDataQuery;
    @Getter private static String createDataQuery;
    @Getter private static String updateDataQuery;
    @Getter private static String deleteProjectQuery;
    @Getter private static String getLabelQuery;
    @Getter private static String changeProjectTableNameQuery;

    static {
        createProjectAttributeTableQuery = "CREATE TABLE IF NOT EXISTS ProjectAttributeTable(project_id UUID, attributes CLOB, attributeTypeMap CLOB)";
        updateProjectAttributeTableQuery = "INSERT INTO ProjectAttributeTable VALUES(?, ?, ?)";
        projectAttributeQuery = "SELECT attributes FROM ProjectAttributeTable WHERE project_id = ?";
        attributeTypeMapQuery = "SELECT attributeTypeMap FROM ProjectAttributeTable WHERE project_id = ?";
        getDeleteProjectAttributeQuery = "DELETE FROM ProjectAttributeTable WHERE project_id = ?";
    }

    public static void createProjectTablePreparedStatement(Map<String, String> headers, String projectName) {
        List<String> attributes = new ArrayList<>();
//        String projectName = projectName;

        String columnNames = "uuid UUID, project_id UUID, project_name VARCHAR(100), ";

        for (String headerName : headers.keySet()) {
            attributes.add(headerName + " " + headers.get(headerName));
        }

        String attributesString = String.join(", ", attributes);
        columnNames += attributesString;
        columnNames += ", fileName VARCHAR(2000), label CLOB DEFAULT NULL, PRIMARY KEY(uuid, project_id)";

        createProjectTableQuery = "CREATE TABLE IF NOT EXISTS " + projectName + " (" + columnNames + ")";
    }

    public static void createGetAllDataPreparedStatement(String projectName, String columnNames) {
        getAllDataQuery = "SELECT uuid, project_name, " + columnNames + " , fileName, label FROM " + projectName;
    }

    public static void createDataPreparedStatement(String projectName, Integer columnNumbers) {
        List<String> attributes = new ArrayList<>();
        createDataQuery = "INSERT INTO " + projectName + " VALUES(";
        for (int i = 0; i < columnNumbers; i++) {
            attributes.add("? ");
        }
        String attributesString = String.join(",", attributes);
        createDataQuery += attributesString;
        createDataQuery = createDataQuery.strip();
        createDataQuery += ")";
    }

    public static void createUpdateDataPreparedStatement(String projectName) {
        updateDataQuery = "UPDATE " + projectName + " SET label = ? WHERE uuid = ? AND project_id = ?";
    }

    public static void createGetSpecificDataPreparedStatement(String projectName, String columnNames) {
        getDataQuery = "SELECT uuid, project_name," + columnNames + " , fileName, label FROM " + projectName + " WHERE uuid = ? AND project_id = ?";
    }

    public static void createGetLabelPreparedStatement(String projectName) {
        getLabelQuery = "SELECT label FROM " + projectName + " WHERE uuid = ? AND project_id = ?";
    }

    public static void createDeleteProjectPreparedStatement(String projectName) {
        deleteProjectQuery = "DROP TABLE " + projectName;
    }

    public static void createChangeProjectTableNamePreparedStatement(String projectName, String newProjectName) {
        changeProjectTableNameQuery = "ALTER TABLE " + projectName + " RENAME TO " + newProjectName;
    }

}
