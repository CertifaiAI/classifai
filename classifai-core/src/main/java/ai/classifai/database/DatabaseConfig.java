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

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Files;

/**
 * Configurations for files and paths of database
 *
 * @author codenamewei
 */
@Slf4j
public class DatabaseConfig
{
    private static final String ROOT_PATH;

    private static final String LCK_FILE_EXTENSION;

    private static final String PORTFOLIO_DB_NAME;
    private static final String BNDBOX_DB_NAME;
    private static final String SEG_DB_NAME;

    private static final String PORTFOLIO_DB_PATH;
    private static final String BNDBOX_DB_PATH;
    private static final String SEG_DB_PATH;

    private static final File PORTFOLIO_DB_LCKPATH;
    private static final File BNDBOX_DB_LCKPATH;
    private static final File SEG_DB_LCKPATH;

    static
    {
        LCK_FILE_EXTENSION = ".lck";

        ROOT_PATH = System.getProperty("user.home") + File.separator + ".classifai";

        PORTFOLIO_DB_NAME = "portfolio";
        BNDBOX_DB_NAME = "bbproject";
        SEG_DB_NAME = "segproject";

        PORTFOLIO_DB_PATH = defineDbPath(PORTFOLIO_DB_NAME);
        BNDBOX_DB_PATH = defineDbPath(BNDBOX_DB_NAME);
        SEG_DB_PATH = defineDbPath(SEG_DB_NAME);

        PORTFOLIO_DB_LCKPATH = new File(PORTFOLIO_DB_PATH + LCK_FILE_EXTENSION);
        BNDBOX_DB_LCKPATH = new File(BNDBOX_DB_PATH + LCK_FILE_EXTENSION);
        SEG_DB_LCKPATH = new File(SEG_DB_PATH + LCK_FILE_EXTENSION);
    }

    private static String defineDbPath(String database)
    {
        return ROOT_PATH + File.separator + database + File.separator + database + "db";
    }

    public static String getRootPath() { return ROOT_PATH; }

    public static String getPortfolioDbPath() { return PORTFOLIO_DB_PATH; }

    public static String getBndboxDbPath() { return BNDBOX_DB_PATH; }

    public static String getSegDbPath() { return SEG_DB_PATH; }

    public static File getPortfolioLockPath() { return PORTFOLIO_DB_LCKPATH; }

    public static File getBndBoxLockPath() { return BNDBOX_DB_LCKPATH; }

    public static File getSegLockPath() { return SEG_DB_LCKPATH; }

    public static boolean isDatabaseSetup(boolean unlockDatabase)
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

    private static boolean isDbReadyForAccess()
    {
        if(PORTFOLIO_DB_LCKPATH.exists() || BNDBOX_DB_LCKPATH.exists() || SEG_DB_LCKPATH.exists())
        {
            log.info("Database is locked. Try with --unlockdb. \n" +
                    "WARNING: This might be hazardous by allowing multiple access to the database.");

            return false;
        }

        return true;
    }

    private static void deleteLckFile()
    {
        try
        {
            if(!Files.deleteIfExists(PORTFOLIO_DB_LCKPATH.toPath()))
            {
                log.debug("Delete portfolio lock file failed from path: " + PORTFOLIO_DB_LCKPATH.getAbsolutePath());
            }

            if(!Files.deleteIfExists(BNDBOX_DB_LCKPATH.toPath()))
            {
                log.debug("Delete boundingbox lock file failed from path: " + BNDBOX_DB_LCKPATH.getAbsolutePath());
            }

            if(!Files.deleteIfExists(SEG_DB_LCKPATH.toPath()))
            {
                log.debug("Delete segmentation lock file failed from path: " + SEG_DB_LCKPATH.getAbsolutePath());
            }
        }
        catch(Exception e) {
            log.debug("Error when delete lock file: ", e);
        }
    }
}
