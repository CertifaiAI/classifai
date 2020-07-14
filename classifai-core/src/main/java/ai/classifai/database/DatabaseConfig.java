/*
 * Copyright (c) 2020 CertifAI
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

/**
 * Configurations for files and paths of database
 *
 * @author Chiawei Lim
 */
@Slf4j
public class DatabaseConfig
{
    public final static String DB_ROOT_PATH;
    public final static String PORTFOLIO_DB;
    public final static String PROJECT_DB;
    public final static String PORTFOLIO_LCKFILE;
    public final static String PROJECT_LCKFILE;

    static
    {
        DB_ROOT_PATH = System.getProperty("user.home") + "/.classifai";
        PORTFOLIO_DB = DB_ROOT_PATH + "/" +  "portfolio/portfoliodb";
        PROJECT_DB = DB_ROOT_PATH + "/" + "project/projectdb";
        PORTFOLIO_LCKFILE = PORTFOLIO_DB + ".lck";
        PROJECT_LCKFILE = PROJECT_DB + ".lck";
    }
}
