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
 * Require further generalization for general purpose
 *
 * @author YCCertifai
 */
@Slf4j
public class DatabaseMigration {

    public static void migrate(){

        //create temporary json file to store data
        String portfolioJson = DatabaseConfig.getRootPath() + File.separator + "portfoliodb.json";
        String bndBoxJson = DatabaseConfig.getRootPath() + File.separator + "bbprojectdb.json";
        String segJson = DatabaseConfig.getRootPath() + File.separator + "segprojectdb.json";

        HsqlDatabaseConfig.deleteLckFile();

        //generate Json file from HSQL
        HSQL2Json(HsqlDatabaseConfig.getPortfolioDbPath(),portfolioJson);
        HSQL2Json(HsqlDatabaseConfig.getBndboxDbPath(),bndBoxJson);
        HSQL2Json(HsqlDatabaseConfig.getSegDbPath(),segJson);

        //Move HSQL to .archive folder
        ArchiveHandler.moveToArchive(HsqlDatabaseConfig.getPortfolioDirPath());
        ArchiveHandler.moveToArchive(HsqlDatabaseConfig.getBndboxDirPath());
        ArchiveHandler.moveToArchive(HsqlDatabaseConfig.getSegDirPath());

        //Create H2db
        createH2(H2DatabaseConfig.getPortfolioDbPath(), PortfolioDbQuery.createPortfolioTable());
        createH2(H2DatabaseConfig.getBndboxDbPath(), BoundingBoxDbQuery.createProject());
        createH2(H2DatabaseConfig.getSegDbPath(), SegDbQuery.createProject());

        //read Json file to H2
        Json2H2(H2DatabaseConfig.getPortfolioDbPath(), portfolioJson);
        Json2H2(H2DatabaseConfig.getBndboxDbPath(), bndBoxJson);
        Json2H2(H2DatabaseConfig.getSegDbPath(), segJson);

        //Move JSON to .archive folder
        ArchiveHandler.moveToArchive(portfolioJson);
        ArchiveHandler.moveToArchive(bndBoxJson);
        ArchiveHandler.moveToArchive(segJson);

    }

    private static Connection connectDatabase(String driver, String url, String username, String password) throws Exception {
        Class.forName(driver);

        return DriverManager.getConnection(url, username, password);
    }

    private static void createH2(String databasePath, String query) {
        try
        {
            Connection con = connectDatabase("org.h2.Driver", "jdbc:h2:file:" + databasePath, "admin", "");
            Statement st = con.createStatement();
            st.executeUpdate(query);
            st.close();
            con.close();
        }
        catch(Exception e)
        {
            log.debug("Unable to connect org.h2.Driver\n" + e);
        }
    }

    private static void HSQL2Json(String databasePath, String filename){

        try
        {
            Connection con = connectDatabase("org.hsqldb.jdbcDriver", "jdbc:hsqldb:file:" + databasePath, null,null);
            Statement st = con.createStatement();
            String read = databasePath.contains("portfolio")?"select * from portfolio":"select * from project";
            ResultSet rs = st.executeQuery(read);
            JSONArray arr = new JSONArray();
            while(rs.next()){
                if( databasePath.equals(HsqlDatabaseConfig.getPortfolioDbPath()))
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
            st.close();
            con.close();
            File file = new File(filename);
            if (!file.exists()) file.createNewFile();
            FileWriter fw = new FileWriter(file);
            fw.write(arr.toString());
            fw.close();

        }catch(Exception e)
        {
            log.debug("Unable to connect org.hsqldb.jdbcDriver\n" + e);
        }

    }

    private static void Json2H2(String databasePath, String filename){
        try
        {
            Connection con = connectDatabase("org.h2.Driver", "jdbc:h2:file:" + databasePath, "admin", "");
            String insert = databasePath.equals(H2DatabaseConfig.getPortfolioDbPath())? PortfolioDbQuery.createNewProject(): AnnotationQuery.createData();
            PreparedStatement st = con.prepareStatement(insert);
            File file = new File(filename);
            InputStream is = new FileInputStream(file);
            JSONTokener tokener = new JSONTokener(is);
            JSONArray arr = new JSONArray(tokener);
            for ( int i = 0; i < arr.length(); i++){
                JSONObject obj = arr.getJSONObject(i);
                if( databasePath.equals(H2DatabaseConfig.getPortfolioDbPath()))
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


            st.close();
            con.close();
        }
        catch(Exception e)
        {
            log.debug("Unable to connect org.h2.Driver\n" + e);
        }
    }
}

