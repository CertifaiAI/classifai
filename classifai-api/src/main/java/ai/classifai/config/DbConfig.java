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
package ai.classifai.config;

import ai.classifai.database.DatabaseConfig;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

/**
 * Database config before starting the server
 *
 * @author Chiawei Lim
 */
@Slf4j
public class DbConfig
{
    public static boolean checkDatabase(boolean unlockDatabase)
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
                log.info("Database is locked. Try with --unlockdb=true. \n" +
                        "WARNING: This might be hazardous by allowing multiple access to the database.");

                return false;
            }
        }

        return true;
    }
}
