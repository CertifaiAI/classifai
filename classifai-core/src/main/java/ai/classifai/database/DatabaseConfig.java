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

import ai.classifai.util.type.Database;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * configurations for files and paths of database
 *
 * @author codenamewei
 */
@Slf4j
public class DatabaseConfig {
    private static final String ROOT_PATH;

    private static final String PORTFOLIO_DB_NAME;
    private static final String BNDBOX_DB_NAME;
    private static final String SEG_DB_NAME;

    private static final String PORTFOLIO_DB_PATH;
    private static final String BNDBOX_DB_PATH;
    private static final String SEG_DB_PATH;

    private static final String PORTFOLIO_DIR_PATH;
    private static final String BNDBOX_DIR_PATH;
    private static final String SEG_DIR_PATH;

    private final File portfolioDbFile;
    private final File bndboxDbFile;
    private final File segDbFile;

    private final File portfolioDbLockPath;
    private final File bndboxDbLockPath;
    private final File segDbLockPath;

    static {
        ROOT_PATH = System.getProperty("user.home") + File.separator + ".classifai";

        PORTFOLIO_DB_NAME = "portfolio";
        BNDBOX_DB_NAME = "bbproject";
        SEG_DB_NAME = "segproject";

        PORTFOLIO_DIR_PATH = defineDirPath(PORTFOLIO_DB_NAME);
        BNDBOX_DIR_PATH = defineDirPath(BNDBOX_DB_NAME);
        SEG_DIR_PATH = defineDirPath(SEG_DB_NAME);

        PORTFOLIO_DB_PATH = defineDbPath(PORTFOLIO_DB_NAME);
        BNDBOX_DB_PATH = defineDbPath(BNDBOX_DB_NAME);
        SEG_DB_PATH = defineDbPath(SEG_DB_NAME);
    }

    public DatabaseConfig(Database database){
        String lckFileExtension = database.getLckFileExtension();
        String dbFileExtension = database.getDbFileExtension();

        portfolioDbLockPath = new File(PORTFOLIO_DB_PATH + lckFileExtension);
        bndboxDbLockPath = new File(BNDBOX_DB_PATH + lckFileExtension);
        segDbLockPath = new File(SEG_DB_PATH + lckFileExtension);

        portfolioDbFile = new File(PORTFOLIO_DB_PATH + dbFileExtension);
        bndboxDbFile = new File(BNDBOX_DB_PATH + dbFileExtension);
        segDbFile = new File(SEG_DB_PATH + dbFileExtension);
    }

    private static String defineDirPath(String database) {
        return ROOT_PATH + File.separator + database;
    }

    private static String defineDbPath(String database) {
        return defineDirPath(database) + File.separator + database + "db";
    }

    public static String getRootPath() {
        return ROOT_PATH;
    }

    public static String getPortfolioDbPath() {
        return PORTFOLIO_DB_PATH;
    }

    public static String getBndboxDbPath() {
        return BNDBOX_DB_PATH;
    }

    public static String getSegDbPath() {
        return SEG_DB_PATH;
    }

    public static String getPortfolioDirPath() {
        return PORTFOLIO_DIR_PATH;
    }

    public static String getBndboxDirPath() {
        return BNDBOX_DIR_PATH;
    }

    public static String getSegDirPath() {
        return SEG_DIR_PATH;
    }

    public File getPortfolioLockPath() { return portfolioDbLockPath; }

    public File getBndBoxLockPath() { return bndboxDbLockPath; }

    public File getSegLockPath() { return segDbLockPath; }

    public String getPortfolioDbFileName(){ return portfolioDbFile.getName(); }

    public String getBndboxDbFileName(){ return bndboxDbFile.getName(); }

    public String getSegDbFileName(){ return segDbFile.getName(); }

    private boolean deleteIfExists(File file){
        Path path = file.toPath();
        if (file.exists()){
            try
            {
                Files.delete(path);
            }
            catch(Exception e)
            {
                log.debug(String.valueOf(e));
                return false;
            }
        }

        return true;
    }

    public void deleteLckFile()
    {
        try
        {
            if(! deleteIfExists(portfolioDbLockPath))
            {
                log.debug("Delete portfolio lock file failed from path: " + portfolioDbLockPath.getAbsolutePath());
            }

            if(! deleteIfExists(bndboxDbLockPath))
            {
                log.debug("Delete boundingbox lock file failed from path: " + bndboxDbLockPath.getAbsolutePath());
            }

            if(! deleteIfExists(segDbLockPath))
            {
                log.debug("Delete segmentation lock file failed from path: " + segDbLockPath.getAbsolutePath());
            }
        }
        catch(Exception e) {
            log.debug("Error when delete lock file: ", e);
        }
    }

    public boolean isDatabaseExist()
    {
        return (portfolioDbFile.exists() && bndboxDbFile.exists() && segDbFile.exists());
    }

    public boolean isDatabaseSetup(boolean unlockDatabase)
    {
        if(unlockDatabase)
        {
            deleteLckFile();
        }
        else
        {
            return isDbReadyForAccess();
        }

        return true;
    }

    private boolean isDbReadyForAccess()
    {
        if(portfolioDbLockPath.exists() || bndboxDbLockPath.exists() || segDbLockPath.exists())
        {
            log.info("Database is locked. Try with --unlockdb. \n" +
                    "WARNING: This might be hazardous by allowing multiple access to the database.");

            return false;
        }

        return true;
    }

}
