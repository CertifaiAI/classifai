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

package ai.certifai.database.project;

import ai.certifai.database.portfolio.PortfolioSQLQuery;

public class ProjectSQLQuery
{
    public final static String QUEUE = "project.queue";

    public final static String CREATE_PROJECT = "create table if not exists Project (uuid integer, projectid integer, imagepath varchar(1000), bndbox varchar(5000), imgDepth integer, " +
            "imageX integer, imageY integer, imageW double, imageH double, imageOriW integer, imageOriH integer, primary key(uuid, projectid))";

    public final static String CREATE_DATA = "insert into Project values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public final static String RETRIEVE_DATA = "select imagepath, bndbox, imgDepth, imageX, imageY, imageW, imageH, imageOriW, imageOriH from Project where uuid = ? and projectid = ?";

    public final static String RETRIEVE_DATA_PATH = "select imagepath from Project where uuid = ? and projectid = ?";

    public final static String UPDATE_DATA = "update Project set bndbox = ?, imageX = ?, imageY = ?, imageW = ?, imageH = ?, imageOriW = ?, imageOriH = ? where uuid = ? and projectid = ?";

    public final static String DELETE_DATA = "delete from Project where uuid = ? and projectid = ?";

    public final static String REMOVE_OBSOLETE_UUID_LIST = PortfolioSQLQuery.REMOVE_OBSOLETE_UUID_LIST;
}