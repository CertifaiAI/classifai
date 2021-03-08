/*
 * Copyright (c) 2020-2021 CertifAI Sdn. Bhd.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.classifai.database.migration;

import ai.classifai.database.DbConfig;
import ai.classifai.database.annotation.AnnotationQuery;
import ai.classifai.database.annotation.bndbox.BoundingBoxDbQuery;
import ai.classifai.database.annotation.seg.SegDbQuery;
import ai.classifai.database.portfolio.PortfolioDbQuery;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.collection.ConversionHandler;
import ai.classifai.util.collection.UuidGenerator;
import ai.classifai.util.data.FileHandler;
import ai.classifai.util.datetime.DateTime;
import ai.classifai.util.type.database.RelationalDb;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.sql.*;
import java.util.*;

/***
 * Program for database migration from HSQL -> H2
 * Require further generalization for all databases
 *
 * @author YCCertifai
 */
@Slf4j
@NoArgsConstructor
public class DbMigration
{
    private Map<String, String> tempJsonDict;
    private Map<String, Connection> hsqlConnDict;
    private Map<String, Connection> h2ConnDict;

    //Migration from v1 -> v2, replacing project id  & uuid from int -> string(uuidv4)

    //Key: v1 project id
    //Value: v2 project id
    private Map<Integer, String> projectIDDict;

    //Key: v1 project id
    //Value: Map<Integer v1 uuid, String v2 uuid>
    private Map<Integer, Map<Integer, String>> projectUUIDDict;

    public boolean migrate()
    {
        //create temporary json file to store data
        createTempJson();

        //hsql lock file to be removed to open up for migration
        if(!DbConfig.getHsql().removeLckIfExist())
        {
            log.info("Remove lock file of hsql database failed. Migration aborted");
            return false;
        }

        //Copy HSQL to .archive folder for backup
        copyToArchive();

        if(!createConnection())
        {
            log.debug("Migration aborted due to failed connection");

            return false;
        }

        //create map for conversion from integer id to uuid
        UUIDConversion();

        //generate Json file from HSQL
        hsql2Json();

        //close hsql connection
        closeConnection(new ArrayList<>(hsqlConnDict.values()));

        ///delete hsql lingering files
        for(String key : DbConfig.getTableKeys())
        {
            String tableFolderPath = DbConfig.getTableFolderPathDict().get(key);
            File tableFilePath = DbConfig.getH2().getTableAbsPathDict().get(key);

            selectiveDelete(tableFolderPath, tableFilePath.getAbsolutePath());
        }

        //Create h2 tables
        createH2(h2ConnDict.get(DbConfig.getPortfolioKey()), PortfolioDbQuery.getCreatePortfolioTable());
        createH2(h2ConnDict.get(DbConfig.getBndBoxKey()), BoundingBoxDbQuery.getCreateProject());
        createH2(h2ConnDict.get(DbConfig.getSegKey()), SegDbQuery.getCreateProject());

        //read Json file to H2
        json2H2();

        //close h2 connections
        closeConnection(new ArrayList<>(h2ConnDict.values()));

        //delete intermediate json files
        for(String key : DbConfig.getTableKeys())
        {
            FileHandler.deleteFile(new File(tempJsonDict.get(key)));
        }

        return true;
    }

    private void createTempJson()
    {
        tempJsonDict = new HashMap<>();

        for(String table : DbConfig.getTableKeys())
        {
            String tempJson = DbConfig.getDbRootPath() + File.separator + table + ".json";

            tempJsonDict.put(table, tempJson);
        }
    }

    private boolean createConnection()
    {
        hsqlConnDict = new HashMap<>();
        h2ConnDict = new HashMap<>();

        for(String key : DbConfig.getTableKeys())
        {
            try
            {
                Connection hsqlConn = connectDb(DbConfig.getTableAbsPathDict().get(key), DbConfig.getHsql());
                Connection h2Conn = connectDb(DbConfig.getTableAbsPathDict().get(key), DbConfig.getH2());

                hsqlConnDict.put(key, hsqlConn);
                h2ConnDict.put(key, h2Conn);

            }
            catch (Exception e)
            {
                log.error("Unable to create connection by failing to connect to database: " + e);

                return false;
            }
        }
        return true;
    }


    private static Connection connectDb(String tableAbsPath, RelationalDb db) throws ClassNotFoundException, SQLException
    {
        Class.forName(db.getDriver());

        return DriverManager.getConnection(db.getUrlHeader() + tableAbsPath, db.getUser(), db.getPassword());
    }

    private void copyToArchive()
    {
        for(String path : DbConfig.getTableFolderPathDict().values())
        {
            ArchiveHandler.copyToArchive(path);
        }
    }

    private static void createH2(Connection con, String query)
    {
        try (Statement st = con.createStatement())
        {
            st.executeUpdate(query);
        }
        catch (Exception e)
        {
            log.debug("Unable to create table. Please check on query: " + query);
        }
    }

    private static void writeJsonToFile(File file, JSONArray arr)
    {
        try (FileWriter fw = new FileWriter(file))
        {
            fw.write(arr.toString());
        }
        catch (Exception e)
        {
            log.debug( "Unable to write JSON to file " + file.getName());
        }
    }

    private static List<String> integerList2StringListWithMap(List<Integer> subsetList, Map<Integer,String> fullListMap)
    {
        List<String> convertedList = new ArrayList<>();

        for (Integer myInt : subsetList)
        {
            convertedList.add(fullListMap.get(myInt));
        }

        return convertedList;
    }

    private void UUIDConversion()
    {
        projectIDDict = new HashMap<>();
        projectUUIDDict = new HashMap<>();

        Set<String> projectIDSet = new HashSet<>();

        try (Statement st = hsqlConnDict.get(DbConfig.getPortfolioKey()).createStatement())
        {
            String query = PortfolioDbQuery.getRetrieveAllProjects();

            ResultSet rs = st.executeQuery(query);

            while (rs.next())
            {
                Map<Integer, String> uuidMap = new HashMap<>();

                Integer projectIDInt = rs.getInt(1);
                List<Integer> UUIDIntList = ConversionHandler.string2IntegerList(rs.getString(6));

                String projectID = UuidGenerator.generateUuid();

                projectIDDict.put(projectIDInt, projectID);

                projectIDSet.add(projectID);

                for (Integer uuidInt: UUIDIntList)
                {
                    String uuid = UuidGenerator.generateUuid();

                    uuidMap.put(uuidInt, uuid);

                }

                projectUUIDDict.put(projectIDInt, uuidMap);
            }
        }
        catch (Exception e)
        {
            log.debug("Unable to convert to UUID " + e);
        }
    }

    private void hsql2Json()
    {
        for (Map.Entry<String, Connection> entry : hsqlConnDict.entrySet())
        {
            String key = entry.getKey();

            Connection con = entry.getValue();

            try (Statement st = con.createStatement())
            {
                JSONArray arr = new JSONArray();

                String query = key.equals(DbConfig.getPortfolioKey()) ? PortfolioDbQuery.getRetrieveAllProjects() : AnnotationQuery.getRetrieveAllProjects();

                ResultSet rs = st.executeQuery(query);

                if (key.equals(DbConfig.getPortfolioKey()))
                {
                    while (rs.next())
                    {
                        Integer projectID = rs.getInt(1);
                        List<Integer> UUIDIntList = ConversionHandler.string2IntegerList(rs.getString(6));

                        List<String> UUIDList = integerList2StringListWithMap(UUIDIntList, projectUUIDDict.get(projectID));

                        arr.put(new JSONObject()
                                .put(ParamConfig.getProjectIdParam(), projectIDDict.get(projectID))
                                .put(ParamConfig.getProjectNameParam(), rs.getString(2))
                                .put(ParamConfig.getAnnotationTypeParam(), rs.getInt(3))
                                .put(ParamConfig.getLabelListParam(), rs.getString(4))
                                .put(ParamConfig.getUuidListParam(), UUIDList));
                    }
                }
                else
                {
                    while (rs.next())
                    {
                        Integer projectIDInt = rs.getInt(2);
                        Integer uuidInt = rs.getInt(1);

                        arr.put(new JSONObject()
                                .put(ParamConfig.getUuidParam(), projectUUIDDict.get(projectIDInt).get(uuidInt))
                                .put(ParamConfig.getProjectIdParam(), projectIDDict.get(projectIDInt))
                                .put(ParamConfig.getImgPathParam(), rs.getString(3))
                                .put(ParamConfig.getProjectContentParam(), rs.getString(4))
                                .put(ParamConfig.getImgDepth(), rs.getInt(5))
                                .put(ParamConfig.getImgXParam(), rs.getInt(6))
                                .put(ParamConfig.getImgYParam(), rs.getInt(7))
                                .put(ParamConfig.getImgWParam(), rs.getDouble(8))
                                .put(ParamConfig.getImgHParam(), rs.getDouble(9))
                                .put(ParamConfig.getFileSizeParam(), rs.getInt(10))
                                .put(ParamConfig.getImgOriWParam(), rs.getInt(11))
                                .put(ParamConfig.getImgOriHParam(), rs.getInt(12)));
                    }
                }

                File file = new File(tempJsonDict.get(key));

                if (!file.exists() && !file.createNewFile())
                {
                    log.debug("Unable to create file " + file.getName());
                }

                writeJsonToFile(file, arr);
            }
            catch (Exception e)
            {
                log.debug("Fail to write to JSON: " + e);
            }
        }

    }

    private void json2H2()
    {
        for (Map.Entry<String, Connection> entry : h2ConnDict.entrySet())
        {
            String key = entry.getKey();

            Connection con = entry.getValue();

            File file = new File(tempJsonDict.get(key));
            String query = key.equals(DbConfig.getPortfolioKey()) ? PortfolioDbQuery.getCreateNewProject() : AnnotationQuery.getCreateData();

            try
            (
                InputStream is = new FileInputStream(file);
                PreparedStatement st = con.prepareStatement(query)
            )
            {
                JSONTokener tokener = new JSONTokener(is);
                JSONArray arr = new JSONArray(tokener);

                if(key.equals(DbConfig.getPortfolioKey()))
                {
                    for(int i = 0; i < arr.length(); ++i)
                    {
                        JSONObject obj = arr.getJSONObject(i);

                        st.setString(1, obj.getString(ParamConfig.getProjectIdParam()));
                        st.setString(2, obj.getString(ParamConfig.getProjectNameParam()));
                        st.setInt(3, obj.getInt(ParamConfig.getAnnotationTypeParam()));
                        st.setString(4, obj.getString(ParamConfig.getLabelListParam()));
                        st.setString(5, obj.getJSONArray(ParamConfig.getUuidListParam()).toString());
                        st.setBoolean(6, false);
                        st.setBoolean(7, false);
                        st.setString(8, new DateTime().toString()); //changed created date of old projects to current date of migration


                        st.executeUpdate();
                        st.clearParameters();
                    }
                }
                else
                {
                    for(int i = 0; i < arr.length(); ++i)
                    {
                        JSONObject obj = arr.getJSONObject(i);

                        st.setString(1, obj.getString(ParamConfig.getUuidParam()));
                        st.setString(2, obj.getString(ParamConfig.getProjectIdParam()));
                        st.setString(3, obj.getString(ParamConfig.getImgPathParam()));
                        st.setString(4, obj.getString(ParamConfig.getProjectContentParam()));
                        st.setInt(5, obj.getInt(ParamConfig.getImgDepth()));
                        st.setInt(6, obj.getInt(ParamConfig.getImgXParam()));
                        st.setInt(7, obj.getInt(ParamConfig.getImgYParam()));
                        st.setDouble(8, obj.getDouble(ParamConfig.getImgWParam()));
                        st.setDouble(9, obj.getDouble(ParamConfig.getImgHParam()));
                        st.setInt(10, obj.getInt(ParamConfig.getFileSizeParam()));
                        st.setInt(11, obj.getInt(ParamConfig.getImgOriWParam()));
                        st.setInt(12, obj.getInt(ParamConfig.getImgOriHParam()));

                        st.executeUpdate();
                        st.clearParameters();
                    }
                }

            }
            catch (Exception e)
            {
                log.debug("Fail to write to H2: " + e);
            }
        }
    }


    private static void selectiveDelete(String folderName, String pathOmitted)
    {
        File folder = new File(folderName);

        if (folder.isDirectory())
        {
            for (File file: folder.listFiles())
            {
                if (!file.getAbsolutePath().equals(pathOmitted))
                {
                    FileHandler.deleteFile(file);
                }
            }
        }
        else
        {
            log.debug(folderName + " is not a directory");
        }
    }

    private static void closeConnection(List<Connection> connection)
    {
        for(Connection conn : connection)
        {
            try {
                conn.close();
            }
            catch(Exception e)
            {
                log.debug("Close connection failed with: " + e);
            }
        }
    }
}