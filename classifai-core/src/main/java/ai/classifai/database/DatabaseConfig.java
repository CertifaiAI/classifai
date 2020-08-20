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

    public final static String LCK_FILE_EXTENSION;

    public final static String PORTFOLIO_DB;
    public final static String BNDBOX_DB;
    public final static String SEGMENTATION_DB;


    public final static String PORTFOLIO_DB_LCKFILE;
    public final static String BNDBOX_DB_LCKFILE;
    public final static String SEGMENTATION_DB_LCKFILE;

    static
    {
        LCK_FILE_EXTENSION = ".lck";

        DB_ROOT_PATH = System.getProperty("user.home") + "/.classifai";

        PORTFOLIO_DB = DB_ROOT_PATH + "/" +  "portfolio/portfoliodb";
        BNDBOX_DB = DB_ROOT_PATH + "/" + "boundingbox/boundingboxdb";
        SEGMENTATION_DB = DB_ROOT_PATH + "/" + "segmentation/segmentationdb";

        PORTFOLIO_DB_LCKFILE = PORTFOLIO_DB + LCK_FILE_EXTENSION;
        BNDBOX_DB_LCKFILE = BNDBOX_DB + LCK_FILE_EXTENSION;
        SEGMENTATION_DB_LCKFILE = SEGMENTATION_DB + LCK_FILE_EXTENSION;

    }
}
