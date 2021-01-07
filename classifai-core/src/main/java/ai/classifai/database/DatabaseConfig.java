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
    private final static String DB_ROOT_PATH;

    private final static String LCK_FILE_EXTENSION;

    private final static String PORTFOLIO_DB;
    private final static String BNDBOX_DB;
    private final static String SEGMENTATION_DB;

    private final static String PORTFOLIO_DB_LCKFILE;
    private final static String BNDBOX_DB_LCKFILE;
    private final static String SEGMENTATION_DB_LCKFILE;

    static
    {
        LCK_FILE_EXTENSION = ".lck";

        DB_ROOT_PATH = System.getProperty("user.home") + File.separator + ".classifai";

        PORTFOLIO_DB = DB_ROOT_PATH + File.separator +  "portfolio/portfoliodb";
        BNDBOX_DB = DB_ROOT_PATH + File.separator + "bbproject/bbprojectdb";
        SEGMENTATION_DB = DB_ROOT_PATH + File.separator + "segproject/segprojectdb";

        PORTFOLIO_DB_LCKFILE = PORTFOLIO_DB + LCK_FILE_EXTENSION;
        BNDBOX_DB_LCKFILE = BNDBOX_DB + LCK_FILE_EXTENSION;
        SEGMENTATION_DB_LCKFILE = SEGMENTATION_DB + LCK_FILE_EXTENSION;
    }

    public static String getDbRootPath() { return DB_ROOT_PATH; }

    public static String getPortfolioDb() { return PORTFOLIO_DB; }

    public static String getBndboxDb() { return BNDBOX_DB; }

    public static String getSegDb() { return SEGMENTATION_DB; }

    public static String getPortfolioLockFile() { return PORTFOLIO_DB_LCKFILE; }

    public static String getBBLockFile() { return BNDBOX_DB_LCKFILE; }

    public static String getSegLockFile() { return SEGMENTATION_DB_LCKFILE; }
}
