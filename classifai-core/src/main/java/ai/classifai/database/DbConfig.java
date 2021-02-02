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
public class DbConfig {
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

    private final File PORTFOLIO_DB_FILE;
    private final File BNDBOX_DB_FILE;
    private final File SEG_DB_FILE;

    private final File PORTFOLIO_DB_LOCK_PATH;
    private final File BNDBOX_DB_LOCK_PATH;
    private final File SEG_DB_LOCK_PATH;

    static
    {
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

    public DbConfig(Database database)
    {
        String lckFileExtension = database.getLckFileExtension();
        String dbFileExtension = database.getDbFileExtension();

        PORTFOLIO_DB_LOCK_PATH = defineLockPath(PORTFOLIO_DB_PATH, lckFileExtension);
        BNDBOX_DB_LOCK_PATH = defineLockPath(BNDBOX_DB_PATH, lckFileExtension);
        SEG_DB_LOCK_PATH = defineLockPath(SEG_DB_PATH, lckFileExtension);

        PORTFOLIO_DB_FILE = new File(PORTFOLIO_DB_PATH + dbFileExtension);
        BNDBOX_DB_FILE = new File(BNDBOX_DB_PATH + dbFileExtension);
        SEG_DB_FILE = new File(SEG_DB_PATH + dbFileExtension);
    }

    public static String getRootPath() { return ROOT_PATH; }

    public static String getPortfolioDbPath() { return PORTFOLIO_DB_PATH; }

    public static String getBndboxDbPath() { return BNDBOX_DB_PATH; }

    public static String getSegDbPath() { return SEG_DB_PATH; }

    public static String getPortfolioDirPath() { return PORTFOLIO_DIR_PATH; }

    public static String getBndboxDirPath() { return BNDBOX_DIR_PATH; }

    public static String getSegDirPath() { return SEG_DIR_PATH; }

    public String getPortfolioDbFileName() { return PORTFOLIO_DB_FILE.getName(); }

    public String getBndboxDbFileName() { return BNDBOX_DB_FILE.getName(); }

    public String getSegDbFileName() { return SEG_DB_FILE.getName(); }

    private static String defineDirPath(String database)
    {
        return ROOT_PATH + File.separator + database;
    }

    private File defineLockPath(String dbPath, String lckFileExtension)
    {
        if (lckFileExtension == null)
        {
            return null;
        }
        return new File( dbPath + lckFileExtension);
    }

    private static String defineDbPath(String database)
    {
        return defineDirPath(database) + File.separator + database + "db";
    }

    public boolean isDatabaseExist()
    {
        return (PORTFOLIO_DB_FILE.exists() && BNDBOX_DB_FILE.exists() && SEG_DB_FILE.exists());
    }

    private boolean deleteIfExists(File file)
    {
        Path path = file.toPath();
        if (file.exists()){
            try
            {
                Files.delete(path);
            }
            catch (Exception e)
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
            if (!deleteIfExists(PORTFOLIO_DB_LOCK_PATH))
            {
                log.debug("Delete portfolio lock file failed from path: " + PORTFOLIO_DB_LOCK_PATH.getAbsolutePath());
            }

            if (!deleteIfExists(BNDBOX_DB_LOCK_PATH))
            {
                log.debug("Delete boundingbox lock file failed from path: " + BNDBOX_DB_LOCK_PATH.getAbsolutePath());
            }

            if (!deleteIfExists(SEG_DB_LOCK_PATH))
            {
                log.debug("Delete segmentation lock file failed from path: " + SEG_DB_LOCK_PATH.getAbsolutePath());
            }
        }
        catch (Exception e)
        {
            log.debug("Error when delete lock file: ", e);
        }
    }
}
