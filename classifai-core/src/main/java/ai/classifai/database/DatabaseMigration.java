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
public class DatabaseMigration {

    private DatabaseMigration(){
        throw new IllegalStateException("Utility class");
    }

    public static boolean migrate(){

        //create temporary json file to store data
        String portfolioJson = DatabaseConfig.getRootPath() + File.separator + "portfoliodb.json";
        String bndBoxJson = DatabaseConfig.getRootPath() + File.separator + "bbprojectdb.json";
        String segJson = DatabaseConfig.getRootPath() + File.separator + "segprojectdb.json";

        DatabaseConfig h2 = new DatabaseConfig(Database.H2);
        DatabaseConfig hsql = new DatabaseConfig(Database.HSQL);

        hsql.deleteLckFile();

        Connection h2PortfolioConnection;
        Connection h2BndboxConnection;
        Connection h2SegConnection;
        Connection hsqlPortfolioConnection;
        Connection hsqlBndboxConnection;
        Connection hsqlSegConnection;

        //Copy HSQL to .archive folder
        ArchiveHandler.copyToArchive(DatabaseConfig.getPortfolioDirPath());
        ArchiveHandler.copyToArchive(DatabaseConfig.getBndboxDirPath());
        ArchiveHandler.copyToArchive(DatabaseConfig.getSegDirPath());

        try
        {
            h2PortfolioConnection = connectDatabase(Database.H2, DatabaseConfig.getPortfolioDbPath());
            h2BndboxConnection = connectDatabase(Database.H2, DatabaseConfig.getBndboxDbPath());
            h2SegConnection = connectDatabase(Database.H2, DatabaseConfig.getSegDbPath());
            hsqlPortfolioConnection = connectDatabase(Database.HSQL, DatabaseConfig.getPortfolioDbPath());
            hsqlBndboxConnection = connectDatabase(Database.HSQL, DatabaseConfig.getBndboxDbPath());
            hsqlSegConnection = connectDatabase(Database.HSQL, DatabaseConfig.getSegDbPath());
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
        hsql2Json(hsqlPortfolioConnection, portfolioJson) ;
        hsql2Json(hsqlBndboxConnection, bndBoxJson) ;
        hsql2Json(hsqlSegConnection, segJson) ;

        deleteExcept(DatabaseConfig.getPortfolioDirPath(), h2.getPortfolioDbFileName());
        deleteExcept(DatabaseConfig.getBndboxDirPath(), h2.getBndboxDbFileName());
        deleteExcept(DatabaseConfig.getSegDirPath(), h2.getSegDbFileName());

        try
        {
            hsqlPortfolioConnection.close();
            hsqlBndboxConnection.close();
            hsqlSegConnection.close();
        }
        catch(Exception e)
        {
            log.debug("Unable to close database: " + e);
        }

        //Create H2db tables
        createH2(h2PortfolioConnection, PortfolioDbQuery.createPortfolioTable());
        createH2(h2BndboxConnection, BoundingBoxDbQuery.createProject());
        createH2(h2SegConnection, SegDbQuery.createProject());

        //read Json file to H2
        Json2H2(h2PortfolioConnection, portfolioJson);
        Json2H2(h2BndboxConnection, bndBoxJson);
        Json2H2(h2SegConnection, segJson);

        try
        {
            h2PortfolioConnection.close();
            h2BndboxConnection.close();
            h2SegConnection.close();
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
                try
                {
                    Files.delete(file.toPath());
                }
                catch (Exception e)
                {
                    log.debug("unable to delete" + file.getName());
                }
            }
        }
        else{
            log.debug( folderName + " is not a directory");
        }
    }

    private static Connection connectDatabase(Database database, String path) throws ClassNotFoundException, SQLException {
        Class.forName(database.getDRIVER());

        return DriverManager.getConnection(database.getURL_HEADER()+ path, database.getUSER(), database.getPASSWORD());
    }

    private static void createH2(Connection con, String query)  {
        Statement st = null;
        try
        {
            st = con.createStatement();
            st.executeUpdate(query);
        }
        catch(Exception e)
        {
            log.debug("Unable to create table. Please check on query: " + query);
        }
        finally
        {
            try
            {
                assert st != null;
                st.close();
            }
            catch (Exception e)
            {
                log.debug( "Statement is null");
            }
        }
    }

    private static boolean isPortfolio(String filename){
        return filename.contains("portfolio");
    }

    private static void hsql2Json(Connection con, String filename){
        Statement st = null;
        try
        {
            JSONArray arr = new JSONArray();
            String read = isPortfolio(filename)?"select * from portfolio":"select * from project";

            st = con.createStatement();
            ResultSet rs = st.executeQuery(read);

            while(rs.next()){
                if( isPortfolio(filename))
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
            shutdownDatabase(con);

            File file = new File(filename);

            if (!file.exists())
            {
                if (! file.createNewFile())
                {
                    log.debug("unable to create file " + file.getName());
                }
                writeJsonToFile(file,arr);
            }

        }
        catch(Exception e)
        {
            log.debug("Fail to write to JSON" + e);

        }
        finally
        {
            try
            {
                st.close();
            }
            catch (Exception e)
            {
                log.debug( "Unable to close statement " + st);
            }
        }
    }

    private static void shutdownDatabase(Connection con){
        Statement shutdownSt = null;
        try
        {
            shutdownSt = con.createStatement();
            shutdownSt.executeQuery("SHUTDOWN");
        }
        catch (Exception e)
        {
            log.debug("Unable to execute query SHUTDOWN");
        }
        finally
        {
            try
            {
                assert shutdownSt != null;
                shutdownSt.close();
            }
            catch(Exception e)
            {
                log.debug("Unable to close shutdownSt");
            }
        }
    }

    private static void writeJsonToFile(File file, JSONArray arr){
        FileWriter fw = null;
        try
        {
            fw = new FileWriter(file);
            fw.write(arr.toString());
        }
        catch (Exception e)
        {
            log.debug( "Unable to write JSON to file " + file.getName());
        }
        finally
        {
            try
            {
                assert fw != null;
                fw.close();
            }
            catch (Exception e)
            {
                log.debug("Unable to close file writer");
            }

        }
    }

    private static void Json2H2(Connection con, String filename){
        InputStream is = null;
        PreparedStatement st = null;
        try
        {
            File file = new File(filename);
            String insert = isPortfolio(filename)? PortfolioDbQuery.createNewProject(): AnnotationQuery.createData();
            st = con.prepareStatement(insert);

            is = new FileInputStream(file);
            JSONTokener tokener = new JSONTokener(is);
            JSONArray arr = new JSONArray(tokener);

            for ( int i = 0; i < arr.length(); i++){

                JSONObject obj = arr.getJSONObject(i);

                if( isPortfolio(filename))
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
        catch(Exception e)
        {
            log.debug("Fail to write to H2" + e);
        }
        finally
        {
            try
            {
                is.close();
            }
            catch (Exception e)
            {
                log.debug( "Unable to close input stream");
            }
            try
            {
                st.close();
            }
            catch (Exception e)
            {
                log.debug( "Unable to close statement");
            }
        }
    }

}

