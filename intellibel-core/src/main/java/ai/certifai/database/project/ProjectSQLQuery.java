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

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;


enum ProjectSQLQueryCode
{
    CREATE_PROJECT,
    CREATE_DATA,
    RETRIEVE_DATA,
    RETRIEVE_DATA_PATH,
    UPDATE_DATA
}

public class ProjectSQLQuery
{
    @Getter final static String queue = "project.queue";

    private final static String CREATE_PROJECT = "create table if not exists Project (uuid integer, projectid integer, imagepath varchar(255), bndbox varchar(5000), " +
            "imageX integer, imageY integer, imageW double, imageH double, imageOriW integer, imageOriH integer, primary key(uuid, projectid))";

    private final static String CREATE_DATA = "insert into Project values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private final static String RETRIEVE_DATA = "select imagepath, bndbox, imageX, imageY, imageW, imageH, imageOriW, imageOriH from Project where uuid = ? and projectid = ?";

    private final static String RETRIEVE_DATA_PATH = "select imagepath from Project where uuid = ? and projectid = ?";

    private final static String UPDATE_DATA = "update Project set bndbox = ?, imageX = ?, imageY = ?, imageW = ?, imageH = ?, imageOriW = ?, imageOriH = ? where uuid = ? and projectid = ?";

    private final static Map<ProjectSQLQueryCode, String> queryLUT = new HashMap();

    // Instantiating the static map
    static
    {
        queryLUT.put(ProjectSQLQueryCode.CREATE_PROJECT, CREATE_PROJECT);
        queryLUT.put(ProjectSQLQueryCode.CREATE_DATA, CREATE_DATA);
        queryLUT.put(ProjectSQLQueryCode.RETRIEVE_DATA, RETRIEVE_DATA);
        queryLUT.put(ProjectSQLQueryCode.RETRIEVE_DATA_PATH, RETRIEVE_DATA_PATH);
        queryLUT.put(ProjectSQLQueryCode.UPDATE_DATA, UPDATE_DATA);

    }

    public static String createProject()
    {
        return CREATE_PROJECT;
    }

    public static String createData()
    {
        return CREATE_DATA;
    }

    public static String retrieveData()
    {
        return RETRIEVE_DATA;
    }

    public static String retrieveDataPath()
    {
        return RETRIEVE_DATA_PATH;
    }

    public static String updateData()
    {
        return UPDATE_DATA;
    }


}