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
 *  Configurations for files and paths of database for H2 Database
 *
 * @author YCCertifai
 */
@Slf4j
public class H2DatabaseConfig extends DatabaseConfig{

    static {
        LCK_FILE_EXTENSION = ".lock.db";
        DB_FILE_EXTENSION = ".mv.db";

        PORTFOLIO_DB_LCKPATH = new File(PORTFOLIO_DB_PATH + LCK_FILE_EXTENSION);
        BNDBOX_DB_LCKPATH = new File(BNDBOX_DB_PATH + LCK_FILE_EXTENSION);
        SEG_DB_LCKPATH = new File(SEG_DB_PATH + LCK_FILE_EXTENSION);

        PORTFOLIO_DB_DBFILE = new File(PORTFOLIO_DB_PATH + DB_FILE_EXTENSION);
        BNDBOX_DB_DBFILE = new File(BNDBOX_DB_PATH + DB_FILE_EXTENSION);
        SEG_DB_DBFILE = new File(SEG_DB_PATH + DB_FILE_EXTENSION);
    }
    public static boolean isDatabaseExist()
    {
        return (PORTFOLIO_DB_DBFILE.exists() && BNDBOX_DB_DBFILE.exists() && SEG_DB_DBFILE.exists());
    }
}
