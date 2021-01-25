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

/**
 * Configurations for files and paths of database
 *
 * @author codenamewei
 */
@Slf4j
public class DatabaseConfig
{
    private final static String ROOT_PATH;

    private final static String LCK_FILE_EXTENSION;

    private final static String PORTFOLIO_DB_NAME;
    private final static String BNDBOX_DB_NAME;
    private final static String SEG_DB_NAME;

    private final static String PORTFOLIO_DB_PATH;
    private final static String BNDBOX_DB_PATH;
    private final static String SEG_DB_PATH;

    private final static String PORTFOLIO_DB_LCKFILE;
    private final static String BNDBOX_DB_LCKFILE;
    private final static String SEG_DB_LCKFILE;

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

        PORTFOLIO_DB_LCKFILE = PORTFOLIO_DB_PATH + LCK_FILE_EXTENSION;
        BNDBOX_DB_LCKFILE = BNDBOX_DB_PATH + LCK_FILE_EXTENSION;
        SEG_DB_LCKFILE = SEG_DB_PATH + LCK_FILE_EXTENSION;
    }

    private static String defineDbPath(String database)
    {
        return ROOT_PATH + File.separator + database + File.separator + database + "db";
    }

    public static String getRootPath() { return ROOT_PATH; }

    public static String getPortfolioDbPath() { return PORTFOLIO_DB_PATH; }

    public static String getBndboxDbPath() { return BNDBOX_DB_PATH; }

    public static String getSegDbPath() { return SEG_DB_PATH; }

    public static String getPortfolioLockFile() { return PORTFOLIO_DB_LCKFILE; }

    public static String getBBLockFile() { return BNDBOX_DB_LCKFILE; }

    public static String getSegLockFile() { return SEG_DB_LCKFILE; }

    public static boolean isDatabaseSetup(boolean unlockDatabase)
    {

        File portfolioLockPath = new File(DatabaseConfig.getPortfolioLockFile());
        File boundingBoxLockPath = new File(DatabaseConfig.getBBLockFile());
        File segmentationLockPath = new File(DatabaseConfig.getSegLockFile());

        if(unlockDatabase)
        {
            if(portfolioLockPath.exists()) portfolioLockPath.delete();

            if(boundingBoxLockPath.exists()) boundingBoxLockPath.delete();

            if(segmentationLockPath.exists()) segmentationLockPath.delete();

        }
        else
        {
            if(portfolioLockPath.exists() || boundingBoxLockPath.exists() || segmentationLockPath.exists())
            {
                log.info("Database is locked. Try with --unlockdb. \n" +
                        "WARNING: This might be hazardous by allowing multiple access to the database.");

                return false;
            }
        }

        return true;
    }
}
