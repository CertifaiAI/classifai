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

import ai.classifai.database.migration.DbMigration;
import lombok.extern.slf4j.Slf4j;

import java.io.File;


/**
 * Db Operations for files and paths of database
 *
 * @author codenamewei
 */
@Slf4j
public class DbOps
{
    private DbOps()
    {
        throw new IllegalStateException("DbOps class");
    }

    public static void configureDatabase()
    {
        migrateDbIfExist();

        setupDb();
    }

    //hsqldb v1 -> h2 v2 database migration
    private static void migrateDbIfExist()
    {
        if(DbConfig.getHSQL().isDbExist() && !DbConfig.getH2().isDbExist())
        {
            log.info("Database migration required. Executing database migration.");

            if (!new DbMigration().migrate())
            {
                log.info("Database migration failed. Old data will not be migrated. You can choose to move on with empty database, or close Classifai now to prevent data lost.");
            }
            else
            {
                log.info("Database migration is successful!");
            }
        }
    }

    private static void setupDb()
    {
        File dataRootPath = new File(DbConfig.getDbRootPath());

        if (dataRootPath.exists())
        {
            log.info("Existing database of classifai on " + dataRootPath);

            if(DbConfig.getH2().isDbLocked())
            {
                log.info("H2 Database is locked. Likely classifai application is running. Close it to proceed.");
            }
        }
        else
        {
            log.info("Database of classifai created on " + dataRootPath);

            boolean databaseIsBuild = dataRootPath.mkdir();

            if (!databaseIsBuild)
            {
                log.debug("Root database could not created: ", dataRootPath);
            }
        }
    }

}