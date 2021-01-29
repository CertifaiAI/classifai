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
package ai.classifai.database.config;

import lombok.extern.slf4j.Slf4j;

import java.io.File;

/**
 * Common configurations for files and paths of database
 *
 * @author codenamewei
 */
@Slf4j
public abstract class DatabaseConfig
{
    private static final String ROOT_PATH;

    private static final String PORTFOLIO_DB_NAME;
    private static final String BNDBOX_DB_NAME;
    private static final String SEG_DB_NAME;

    protected static final String PORTFOLIO_DB_PATH;
    protected static final String BNDBOX_DB_PATH;
    protected static final String SEG_DB_PATH;

    private final static String PORTFOLIO_DIR_PATH;
    private final static String BNDBOX_DIR_PATH;
    private final static String SEG_DIR_PATH;

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

    private static String defineDirPath(String database){
        return ROOT_PATH + File.separator + database;
    }

    private static String defineDbPath(String database)
    {
        return defineDirPath(database) + File.separator + database + "db";
    }

    public static String getRootPath() { return ROOT_PATH; }

    public static String getPortfolioDbPath() { return PORTFOLIO_DB_PATH; }

    public static String getBndboxDbPath() { return BNDBOX_DB_PATH; }

    public static String getSegDbPath() { return SEG_DB_PATH; }

    public static String getPortfolioDirPath() { return PORTFOLIO_DIR_PATH; }

    public static String getBndboxDirPath() { return BNDBOX_DIR_PATH; }

    public static String getSegDirPath() { return SEG_DIR_PATH; }

}
