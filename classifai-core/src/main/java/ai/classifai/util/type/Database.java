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
    H2(".lock.db",".mv.db", "org.h2.Driver", "jdbc:h2:file:", "admin", "" );

    private final String lckFileExtension;
    private final String dbFileExtension;
    private final String driver;
    private final String urlHeader;
    private final String user;
    private final String password;

    Database(final String lckFileExtension, final String dbFileExtension, final String driver, final String urlHeader, final String user, final String password){

        this.lckFileExtension = lckFileExtension;
        this.dbFileExtension = dbFileExtension;
        this.driver = driver;
        this.urlHeader = urlHeader;
        this.user = user;
        this.password = password;

    }

    public String getDbFileExtension() {
        return dbFileExtension;
    }

    public String getLCK_FILE_EXTENSION() {
        return lckFileExtension;
    }

    public String getDriver() { return driver; }

    public String getUrlHeader() { return urlHeader; }

    public String getUser() { return user; }

    public String getPassword() { return password; }
}
