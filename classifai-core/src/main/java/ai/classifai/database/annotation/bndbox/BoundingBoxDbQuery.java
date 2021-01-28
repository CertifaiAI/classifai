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
package ai.classifai.database.annotation.bndbox;

import ai.classifai.database.annotation.AnnotationQuery;

/***
 * Bounding box database query
 *
 * @author codenamewei
 */
public class BoundingBoxDbQuery extends AnnotationQuery
{
    private final static String QUEUE = "boundingbox.queue";

    private final static String CREATE_PROJECT = "CREATE TABLE IF NOT EXISTS Project (uuid integer, project_id integer, img_path varchar(2000), bnd_box varchar(65535), img_depth integer, " +
            "img_x integer, img_y integer, img_w double, img_h double, file_size integer, img_ori_w integer, img_ori_h integer, PRIMARY KEY(uuid, project_id))";

    private final static String RETRIEVE_DATA = "SELECT img_path, bnd_box, img_x, img_y, img_w, img_h, file_size, img_ori_w, img_ori_h from Project WHERE uuid = ? AND project_id = ?";

    private final static String UPDATE_DATA = "UPDATE Project SET bnd_box = ?, img_depth = ?,  img_x = ?, img_y = ?, img_w = ?, img_h = ?, file_size = ?, img_ori_w = ?, img_ori_h = ? WHERE uuid = ? AND project_id = ?";

    public static String getQueue()
    {
        return QUEUE;
    }

    public static String createProject()
    {
        return CREATE_PROJECT;
    }

    public static String retrieveData()
    {
        return RETRIEVE_DATA;
    }

    public static String updateData()
    {
        return UPDATE_DATA;
    }
}