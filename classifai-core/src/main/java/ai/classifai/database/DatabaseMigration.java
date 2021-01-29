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
import ai.classifai.database.config.DatabaseConfig;
import ai.classifai.database.config.H2DatabaseConfig;
import ai.classifai.database.config.HsqlDatabaseConfig;
import ai.classifai.database.portfolio.PortfolioDbQuery;
import ai.classifai.util.ArchiveHandler;
import ai.classifai.util.ParamConfig;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.sql.*;

/***
 * Program for database migration from HSQL -> H2
 * Require further generalization for all databases
 *
 * @author YCCertifai
 */
@Slf4j
public class DatabaseMigration {

    public static boolean migrate(){

        //create temporary json file to store data
        String portfolioJson = DatabaseConfig.getRootPath() + File.separator + "portfoliodb.json";
        String bndBoxJson = DatabaseConfig.getRootPath() + File.separator + "bbprojectdb.json";
        String segJson = DatabaseConfig.getRootPath() + File.separator + "segprojectdb.json";

        HsqlDatabaseConfig.deleteLckFile();

        Connection H2PortfolioConnection;
        Connection H2BndboxConnection;
        Connection H2SegConnection;
        Connection HsqlPortfolioConnection;
        Connection HsqlBndboxConnection;
        Connection HsqlSegConnection;

        //Copy HSQL to .archive folder
        ArchiveHandler.copyToArchive(HsqlDatabaseConfig.getPortfolioDirPath());
        ArchiveHandler.copyToArchive(HsqlDatabaseConfig.getBndboxDirPath());
        ArchiveHandler.copyToArchive(HsqlDatabaseConfig.getSegDirPath());

        try
        {
            H2PortfolioConnection = connectDatabase("org.h2.Driver", "jdbc:h2:file:" + H2DatabaseConfig.getPortfolioDbPath(), "admin", "");
            H2BndboxConnection = connectDatabase("org.h2.Driver", "jdbc:h2:file:" + H2DatabaseConfig.getBndboxDbPath(), "admin", "");
            H2SegConnection = connectDatabase("org.h2.Driver", "jdbc:h2:file:" + H2DatabaseConfig.getSegDbPath(), "admin", "");
            HsqlPortfolioConnection = connectDatabase("org.hsqldb.jdbcDriver", "jdbc:hsqldb:file:" + HsqlDatabaseConfig.getPortfolioDbPath(), null,null);
            HsqlBndboxConnection = connectDatabase("org.hsqldb.jdbcDriver", "jdbc:hsqldb:file:" + HsqlDatabaseConfig.getBndboxDbPath(), null,null);
            HsqlSegConnection = connectDatabase("org.hsqldb.jdbcDriver", "jdbc:hsqldb:file:" + HsqlDatabaseConfig.getSegDbPath(), null,null);
        }
        catch(Exception e)
        {
            log.error("Unable to connect to database: " + e);
            deleteExcept(DatabaseConfig.getPortfolioDirPath(), "");
            deleteExcept(DatabaseConfig.getBndboxDirPath(), "");
            deleteExcept(DatabaseConfig.getSegDirPath(), "");
            return false;
        }

        //generate Json file from HSQL
        HSQL2Json(HsqlPortfolioConnection, portfolioJson) ;
        HSQL2Json(HsqlBndboxConnection, bndBoxJson) ;
        HSQL2Json(HsqlSegConnection, segJson) ;

        deleteExcept(DatabaseConfig.getPortfolioDirPath(), H2DatabaseConfig.getPortfolioDbFileName());
        deleteExcept(DatabaseConfig.getBndboxDirPath(), H2DatabaseConfig.getBndboxDbFileName());
        deleteExcept(DatabaseConfig.getSegDirPath(), H2DatabaseConfig.getSegDbFileName());

        try
        {
            HsqlPortfolioConnection.close();
            HsqlBndboxConnection.close();
            HsqlSegConnection.close();
        }
        catch(Exception e)
        {
            log.debug("Unable to close database: " + e);
        }

        //Create H2db tables
        createH2(H2PortfolioConnection, PortfolioDbQuery.createPortfolioTable());
        createH2(H2BndboxConnection, BoundingBoxDbQuery.createProject());
        createH2(H2SegConnection, SegDbQuery.createProject());

        //read Json file to H2
        Json2H2(H2PortfolioConnection, portfolioJson);
        Json2H2(H2BndboxConnection, bndBoxJson);
        Json2H2(H2SegConnection, segJson);

        try
        {
            H2PortfolioConnection.close();
            H2BndboxConnection.close();
            H2SegConnection.close();
        }
        catch(Exception e)
        {
            log.debug("Unable to close database: " + e);
        }

        //Move JSON to .archive folder
        ArchiveHandler.moveToArchive(portfolioJson);
        ArchiveHandler.moveToArchive(bndBoxJson);
        ArchiveHandler.moveToArchive(segJson);

        return true;

    }

    private static void deleteExcept(String folderName, String dbPath){
        File folder = new File(folderName);
        if( folder.isDirectory()){
            for (File file: folder.listFiles()){
                if (file.getName().equals(dbPath)){
                    continue;
                }
                file.delete();
            }
        }
        else{
            log.debug( folderName + " is not a directory");
        }
    }

    private static Connection connectDatabase(String driver, String url, String username, String password) throws ClassNotFoundException, SQLException {
        Class.forName(driver);

        return DriverManager.getConnection(url, username, password);
    }

    private static void createH2(Connection con, String query)  {
        try
        {
            Statement st = con.createStatement();
            st.executeUpdate(query);
            st.close();
        }
        catch(Exception e)
        {
            log.debug("Unable to create table. Please check on query: " + query);
        }
    }

    private static void HSQL2Json(Connection con, String filename){

        try
        {
            JSONArray arr = new JSONArray();
            String read = filename.contains("portfolio")?"select * from portfolio":"select * from project";

            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(read);

            while(rs.next()){
                if( filename.contains("portfolio"))
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

            con.createStatement().executeQuery("SHUTDOWN");

            File file = new File(filename);

            if (!file.exists()) file.createNewFile();
            FileWriter fw = new FileWriter(file);
            fw.write(arr.toString());

            fw.close();
            st.close();

        }catch(Exception e)
        {
            log.debug("Fail to write to JSON" + e);

        }
    }

    private static void Json2H2(Connection con, String filename){
        try
        {
            File file = new File(filename);
            String insert = filename.contains("portfolio")? PortfolioDbQuery.createNewProject(): AnnotationQuery.createData();
            PreparedStatement st = con.prepareStatement(insert);

            InputStream is = new FileInputStream(file);
            JSONTokener tokener = new JSONTokener(is);
            JSONArray arr = new JSONArray(tokener);

            for ( int i = 0; i < arr.length(); i++){

                JSONObject obj = arr.getJSONObject(i);

                if( filename.contains("portfolio"))
                {
                    st.setInt(1, obj.getInt(ParamConfig.getProjectIDParam()));
                    st.setString(2, obj.getString(ParamConfig.getProjectNameParam()));
                    st.setInt(3, obj.getInt(ParamConfig.getAnnotateTypeParam()));
                    st.setString(4, obj.getString(ParamConfig.getLabelListParam()));
                    st.setInt(5, obj.getInt(ParamConfig.getUuidGeneratorParam()));
                    st.setString(6, obj.getString(ParamConfig.getUUIDListParam()));
                    st.setBoolean(7, false);
                    st.setBoolean(8, false);
                    st.setString(9, "");
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

            is.close();
            st.close();
        }
        catch(Exception e)
        {
            log.debug("Fail to write to H2" + e);
        }
    }
}

