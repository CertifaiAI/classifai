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

import ai.classifai.util.type.database.H2;
import ai.classifai.util.type.database.Hsql;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Db Operations for files and paths of database
 *
 * @author codenamewei
 */
@Slf4j
public class DbConfig
{
    @Getter private static String dbRootPath;

    @Getter private static H2 H2;
    @Getter private static Hsql HSQL;

    @Getter private static Map<String, String> tableFolderPathDict;
    @Getter private static Map<String, String> tableAbsPathDict;

    @Getter private static String portfolioKey;
    @Getter private static String bndBoxKey;
    @Getter private static String segKey;

    @Getter private static List<String> tableKeys;

    private DbConfig()
    {
        throw new IllegalStateException("DbConfig class");
    }

    static
    {
        dbRootPath = System.getProperty("user.home") + File.separator + ".classifai";

        portfolioKey = "portfolio";
        bndBoxKey = "bbproject";
        segKey  = "segproject";

        //add more database tables here if created
        tableKeys = Arrays.asList(new String[]{portfolioKey, bndBoxKey, segKey});

        tableFolderPathDict = new HashMap<>();
        tableAbsPathDict = new HashMap<>();

        for(String name : tableKeys)
        {
            String dbFolderPath = dbRootPath + File.separator + name ;

            tableFolderPathDict.put(name, dbFolderPath);
            tableAbsPathDict.put(name, dbFolderPath + File.separator + name +  "db");
        }

        H2 = new H2();
        HSQL = new Hsql();

        H2.setupDb(tableKeys, tableAbsPathDict);

        HSQL.setupDb(tableKeys, tableAbsPathDict);

    }
}