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
package ai.classifai.database;

import ai.classifai.database.annotation.AnnotationQuery;
import ai.classifai.database.annotation.bndbox.BoundingBoxDbQuery;
import ai.classifai.database.annotation.seg.SegDbQuery;
import ai.classifai.database.portfolio.PortfolioDbQuery;
import ai.classifai.util.ArchiveHandler;
import ai.classifai.util.DateTime;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.type.Database;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.*;

/***
 * Program for database migration from HSQL -> H2
 * Require further generalization for all databases
 *
 * @author YCCertifai
 */
@Slf4j
public class DbMigration {

    private DbMigration(){
        throw new IllegalStateException("Utility class");
    }

    public static boolean migrate()
    {
        //create temporary json file to store data
        String portfolioJson = DbConfig.getRootPath() + File.separator + "portfoliodb.json";
        String bndBoxJson = DbConfig.getRootPath() + File.separator + "bbprojectdb.json";
        String segJson = DbConfig.getRootPath() + File.separator + "segprojectdb.json";

        DbConfig h2 = new DbConfig(Database.H2);
        DbConfig hsql = new DbConfig(Database.HSQL);

        hsql.deleteLckFile();

        Connection h2PortfolioConn;
        Connection h2BndboxConn;
        Connection h2SegConn;

        Connection hsqlPortfolioConn;
        Connection hsqlBndboxConn;
        Connection hsqlSegConn;

        //Copy HSQL to .archive folder for backup
        ArchiveHandler.copyToArchive(DbConfig.getPortfolioDirPath());
        ArchiveHandler.copyToArchive(DbConfig.getBndboxDirPath());
        ArchiveHandler.copyToArchive(DbConfig.getSegDirPath());

        try
        {
            h2PortfolioConn = connectDb(Database.H2, DbConfig.getPortfolioDbPath());
            h2BndboxConn = connectDb(Database.H2, DbConfig.getBndboxDbPath());
            h2SegConn = connectDb(Database.H2, DbConfig.getSegDbPath());

            hsqlPortfolioConn = connectDb(Database.HSQL, DbConfig.getPortfolioDbPath());
            hsqlBndboxConn = connectDb(Database.HSQL, DbConfig.getBndboxDbPath());
            hsqlSegConn = connectDb(Database.HSQL, DbConfig.getSegDbPath());
        }
        catch (Exception e)
        {
            log.error("Unable to connect to database: " + e);

            deleteExcept(DbConfig.getPortfolioDirPath(), "");
            deleteExcept(DbConfig.getBndboxDirPath(), "");
            deleteExcept(DbConfig.getSegDirPath(), "");

            return false;
        }

        //generate Json file from HSQL
        hsql2Json(hsqlPortfolioConn, portfolioJson) ;
        hsql2Json(hsqlBndboxConn, bndBoxJson) ;
        hsql2Json(hsqlSegConn, segJson) ;

        deleteExcept(DbConfig.getPortfolioDirPath(), h2.getPortfolioDbFileName());
        deleteExcept(DbConfig.getBndboxDirPath(), h2.getBndboxDbFileName());
        deleteExcept(DbConfig.getSegDirPath(), h2.getSegDbFileName());

        try
        {
            hsqlPortfolioConn.close();
            hsqlBndboxConn.close();
            hsqlSegConn.close();
        }
        catch (Exception e)
        {
            log.debug("Unable to close database: " + e);
        }

        //Create H2db tables
        createH2(h2PortfolioConn, PortfolioDbQuery.createPortfolioTable());
        createH2(h2BndboxConn, BoundingBoxDbQuery.createProject());
        createH2(h2SegConn, SegDbQuery.createProject());

        //read Json file to H2
        json2H2(h2PortfolioConn, portfolioJson);
        json2H2(h2BndboxConn, bndBoxJson);
        json2H2(h2SegConn, segJson);

        try
        {
            h2PortfolioConn.close();
            h2BndboxConn.close();
            h2SegConn.close();
        }
        catch (Exception e)
        {
            log.debug("Unable to close database: " + e);
        }

        deleteFile(new File(portfolioJson));
        deleteFile(new File(bndBoxJson));
        deleteFile(new File(segJson));

        return true;
    }

    private static boolean isPortfolio(String filename){
        return filename.contains("portfolio");
    }

    private static void deleteFile(File file)
    {
        try
        {
            Files.delete(file.toPath());
        }
        catch (Exception e)
        {
            log.debug("unable to delete" + file.getName());
        }
    }

    private static void deleteExcept(String folderName, String dbPath)
    {
        File folder = new File(folderName);
        if (folder.isDirectory())
        {
            for (File file: folder.listFiles())
            {
                if (file.getName().equals(dbPath))
                {
                    continue;
                }
                deleteFile(file);
            }
        }
        else
        {
            log.debug(folderName + " is not a directory");
        }
    }

    private static Connection connectDb(Database database, String path) throws ClassNotFoundException, SQLException
    {
        Class.forName(database.getDriver());

        return DriverManager.getConnection(database.getUrlHeader()+ path, database.getUser(), database.getPassword());
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

    private static void shutdownDb(Connection con)
    {
        try (Statement shutdownSt = con.createStatement())
        {

            shutdownSt.executeQuery("SHUTDOWN");
        }
        catch (Exception e)
        {
            log.debug("Unable to execute query SHUTDOWN");
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

    private static void hsql2Json(Connection con, String filename)
    {
        try (Statement st = con.createStatement())
        {
            JSONArray arr = new JSONArray();
            String read = isPortfolio(filename) ? "select * from portfolio" : "select * from project";

            ResultSet rs = st.executeQuery(read);

            while (rs.next())
            {
                if (isPortfolio(filename))
                {
                    arr.put(new JSONObject()
                            .put(ParamConfig.getProjectIDParam(), rs.getInt(1))
                            .put(ParamConfig.getProjectNameParam(), rs.getString(2))
                            .put(ParamConfig.getAnnotateTypeParam(), rs.getInt(3))
                            .put(ParamConfig.getLabelListParam(), rs.getString(4))
                            .put(ParamConfig.getUuidGeneratorParam(), rs.getInt(5))
                            .put(ParamConfig.getUUIDListParam(), rs.getString(6)));
                }
                else
                {
                    arr.put(new JSONObject()
                            .put(ParamConfig.getUUIDParam(), rs.getInt(1))
                            .put(ParamConfig.getProjectIDParam(), rs.getInt(2))
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

            shutdownDb(con);

            File file = new File(filename);

            if (!file.exists())
            {
                if (!file.createNewFile())
                {
                    log.debug("unable to create file " + file.getName());
                }
                writeJsonToFile(file,arr);
            }
        }
        catch (Exception e)
        {
            log.debug("Fail to write to JSON" + e);
        }
    }

    private static void json2H2(Connection con, String filename)
    {
        File file = new File(filename);
        String insert = isPortfolio(filename) ? PortfolioDbQuery.createNewProject() : AnnotationQuery.createData();

        try
        (
                InputStream is = new FileInputStream(file);
                PreparedStatement st = con.prepareStatement(insert)
        )
        {
            JSONTokener tokener = new JSONTokener(is);
            JSONArray arr = new JSONArray(tokener);

            for (int i = 0; i < arr.length(); i++)
            {
                JSONObject obj = arr.getJSONObject(i);

                if (isPortfolio(filename))
                {
                    st.setInt(1, obj.getInt(ParamConfig.getProjectIDParam()));
                    st.setString(2, obj.getString(ParamConfig.getProjectNameParam()));
                    st.setInt(3, obj.getInt(ParamConfig.getAnnotateTypeParam()));
                    st.setString(4, obj.getString(ParamConfig.getLabelListParam()));
                    st.setInt(5, obj.getInt(ParamConfig.getUuidGeneratorParam()));
                    st.setString(6, obj.getString(ParamConfig.getUUIDListParam()));
                    st.setBoolean(7, false);
                    st.setBoolean(8, false);
                    st.setString(9, DateTime.get());
                }
                else
                {
                    st.setInt(1, obj.getInt(ParamConfig.getUUIDParam()));
                    st.setInt(2, obj.getInt(ParamConfig.getProjectIDParam()));
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
                }
                st.executeUpdate();
                st.clearParameters();
            }
        }
        catch (Exception e)
        {
            log.debug("Fail to write to H2" + e);
        }
    }
}

