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
import ai.classifai.util.DateTime;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.collection.ConversionHandler;
import ai.classifai.util.collection.UUIDGenerator;
import ai.classifai.util.data.FileHandler;
import ai.classifai.util.type.database.RelationalDb;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
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
    private Map<Integer, String> projectIDDict;
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

        //convert all integer id to uuid
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
        createH2(h2ConnDict.get(DbConfig.getPortfolioKey()), PortfolioDbQuery.createPortfolioTable());
        createH2(h2ConnDict.get(DbConfig.getBndBoxKey()), BoundingBoxDbQuery.createProject());
        createH2(h2ConnDict.get(DbConfig.getSegKey()), SegDbQuery.createProject());

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

    private void UUIDConversion()
    {
        projectIDDict = new HashMap<>();
        projectUUIDDict = new HashMap<>();

        Set<String> projectIDSet = new HashSet<>();

        try (Statement st = hsqlConnDict.get(DbConfig.getPortfolioKey()).createStatement())
        {
            String query = PortfolioDbQuery.getAllProjects();

            ResultSet rs = st.executeQuery(query);

            while (rs.next())
            {
                Set<String> uuidSet = new HashSet<>();
                Map<Integer, String> uuidMap = new HashMap<>();

                Integer projectIDInt = rs.getInt(1);
                List<Integer> UUIDIntList = ConversionHandler.string2IntegerList(rs.getString(6));

                String projectID = UUIDGenerator.generateUUID(projectIDSet);

                projectIDDict.put(projectIDInt, projectID);

                projectIDSet.add(projectID);

                for (Integer uuidInt: UUIDIntList)
                {
                    String uuid = UUIDGenerator.generateUUID(uuidSet);

                    uuidMap.put(uuidInt, uuid);

                    uuidSet.add(uuid);
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

                String query = key.equals(DbConfig.getPortfolioKey()) ? PortfolioDbQuery.getAllProjects() : AnnotationQuery.getAllProjects();

                ResultSet rs = st.executeQuery(query);

                if (key.equals(DbConfig.getPortfolioKey()))
                {
                    while (rs.next())
                    {
                        Integer projectID = rs.getInt(1);
                        List<Integer> UUIDIntList = ConversionHandler.string2IntegerList(rs.getString(6));

                        List<String> UUIDList = ConversionHandler.integerList2StringListWithMap(UUIDIntList, projectUUIDDict.get(projectID));

                        arr.put(new JSONObject()
                                .put(ParamConfig.getProjectIDParam(), projectIDDict.get(projectID))
                                .put(ParamConfig.getProjectNameParam(), rs.getString(2))
                                .put(ParamConfig.getAnnotateTypeParam(), rs.getInt(3))
                                .put(ParamConfig.getLabelListParam(), rs.getString(4))
                                .put(ParamConfig.getUUIDListParam(), UUIDList));
                    }
                }
                else
                {
                    while (rs.next())
                    {
                        Integer projectIDInt = rs.getInt(2);
                        Integer uuidInt = rs.getInt(1);

                        arr.put(new JSONObject()
                                .put(ParamConfig.getUUIDParam(), projectUUIDDict.get(projectIDInt).get(uuidInt))
                                .put(ParamConfig.getProjectIDParam(), projectIDDict.get(projectIDInt))
                                .put(ParamConfig.getImagePathParam(), rs.getString(3))
                                .put(ParamConfig.getProjectContentParam(), rs.getString(4))
                                .put(ParamConfig.getImageDepth(), rs.getInt(5))
                                .put(ParamConfig.getImageXParam(), rs.getInt(6))
                                .put(ParamConfig.getImageYParam(), rs.getInt(7))
                                .put(ParamConfig.getImageWParam(), rs.getDouble(8))
                                .put(ParamConfig.getImageHParam(), rs.getDouble(9))
                                .put(ParamConfig.getFileSizeParam(), rs.getInt(10))
                                .put(ParamConfig.getImageORIWParam(), rs.getInt(11))
                                .put(ParamConfig.getImageORIHParam(), rs.getInt(12)));
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
            String query = key.equals(DbConfig.getPortfolioKey()) ? PortfolioDbQuery.createNewProject() : AnnotationQuery.createData();

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

                        st.setString(1, obj.getString(ParamConfig.getProjectIDParam()));
                        st.setString(2, obj.getString(ParamConfig.getProjectNameParam()));
                        st.setInt(3, obj.getInt(ParamConfig.getAnnotateTypeParam()));
                        st.setString(4, obj.getString(ParamConfig.getLabelListParam()));
                        st.setString(5, obj.getJSONArray(ParamConfig.getUUIDListParam()).toString());
                        st.setBoolean(6, false);
                        st.setBoolean(7, false);
                        st.setString(8, DateTime.get()); //changed created date of old projects to current date of migration


                        st.executeUpdate();
                        st.clearParameters();
                    }
                }
                else
                {
                    for(int i = 0; i < arr.length(); ++i)
                    {
                        JSONObject obj = arr.getJSONObject(i);

                        st.setString(1, obj.getString(ParamConfig.getUUIDParam()));
                        st.setString(2, obj.getString(ParamConfig.getProjectIDParam()));
                        st.setString(3, obj.getString(ParamConfig.getImagePathParam()));
                        st.setString(4, obj.getString(ParamConfig.getProjectContentParam()));
                        st.setInt(5, obj.getInt(ParamConfig.getImageDepth()));
                        st.setInt(6, obj.getInt(ParamConfig.getImageXParam()));
                        st.setInt(7, obj.getInt(ParamConfig.getImageYParam()));
                        st.setDouble(8, obj.getDouble(ParamConfig.getImageWParam()));
                        st.setDouble(9, obj.getDouble(ParamConfig.getImageHParam()));
                        st.setInt(10, obj.getInt(ParamConfig.getFileSizeParam()));
                        st.setInt(11, obj.getInt(ParamConfig.getImageORIWParam()));
                        st.setInt(12, obj.getInt(ParamConfig.getImageORIHParam()));

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