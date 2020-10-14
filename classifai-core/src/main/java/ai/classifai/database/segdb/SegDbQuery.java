/*
 * Copyright (c) 2020 CertifAI Sdn. Bhd.
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
package ai.classifai.database.segdb;

/***
 * Segmentation database query
 *
 * @author Chiawei Lim
 */
public class SegDbQuery
{
    private final static String QUEUE = "segmentation.queue";

    private final static String CREATE_PROJECT = "create table if not exists Project (uuid integer, project_id integer, img_path varchar(2000), polygons clob, img_depth integer, " +
            "img_x integer, img_y integer, img_w double, img_h double, file_size integer, img_ori_w integer, img_ori_h integer, primary key(uuid, project_id))";

    private final static String CREATE_DATA = "insert into Project values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private final static String RETRIEVE_DATA = "select img_path, polygons, img_x, img_y, img_w, img_h, file_size, img_ori_w, img_ori_h from Project where uuid = ? and project_id = ?";

    private final static String RETRIEVE_DATA_PATH  = "select img_path from Project where uuid = ? and project_id = ?";

    private final static String UPDATE_DATA = "update Project set polygons = ?, img_depth = ?, img_x = ?, img_y = ?, img_w = ?, img_h = ?, file_size = ?, img_ori_w = ?, img_ori_h = ? where uuid = ? and project_id = ?";

    private final static String DELETE_DATA = "delete from Project where uuid = ? and project_id = ?";

    private final static String LOAD_VALID_PROJECT_UUID = "load valid project uuid";

    public static String getQueue(){ return QUEUE; }

    public static String createProject() { return CREATE_PROJECT; }

    public static String createData() { return CREATE_DATA; }

    public static String retrieveData() { return RETRIEVE_DATA; }

    public static String retrieveDataPath() { return RETRIEVE_DATA_PATH; }

    public static String updateData() { return UPDATE_DATA; }

    public static String deleteData() { return DELETE_DATA; }

    public static String loadValidProjectUUID() { return LOAD_VALID_PROJECT_UUID; }


}