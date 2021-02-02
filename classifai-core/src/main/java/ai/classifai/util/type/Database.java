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
package ai.classifai.util.type;

/**
 * Database info
 *
 * @author YCCertifai
 */
public enum Database {
    HSQL(".lck", ".script","org.hsqldb.jdbcDriver", "jdbc:hsqldb:file:",null,null),
    H2(null,".mv.db", "org.h2.Driver", "jdbc:h2:file:", "admin", "" );

    private final String LCK_FILE_EXTENSION;
    private final String DB_FILE_EXTENSION;
    private final String DRIVER;
    private final String URL_HEADER;
    private final String USER;
    private final String PASSWORD;

    Database(final String LCK_FILE_EXTENSION, final String DB_FILE_EXTENSION, final String DRIVER, final String URL_HEADER, final String USER, final String PASSWORD)
    {
        this.LCK_FILE_EXTENSION = LCK_FILE_EXTENSION;
        this.DB_FILE_EXTENSION = DB_FILE_EXTENSION;
        this.DRIVER = DRIVER;
        this.URL_HEADER = URL_HEADER;
        this.USER = USER;
        this.PASSWORD = PASSWORD;
    }

    public String getDbFileExtension() {
        return DB_FILE_EXTENSION;
    }

    public String getLckFileExtension() {
        return LCK_FILE_EXTENSION;
    }

    public String getDriver() { return DRIVER; }

    public String getUrlHeader() { return URL_HEADER; }

    public String getUser() { return USER; }

    public String getPassword() { return PASSWORD; }
}
