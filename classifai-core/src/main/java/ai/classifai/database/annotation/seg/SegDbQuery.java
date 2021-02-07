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
package ai.classifai.database.annotation.seg;

import ai.classifai.database.annotation.AnnotationQuery;
/***
 * Segmentation database query
 *
 * @author codenamewei
 */
public class SegDbQuery extends AnnotationQuery
{
    private static final String QUEUE = "segmentation.queue";

    private static final String CREATE_PROJECT = "CREATE TABLE IF NOT EXISTS Project (uuid INT, project_id INT, img_path VARCHAR(2000), polygons CLOB, img_depth INT, " +
            "img_x INT, img_y INT, img_w DOUBLE, img_h DOUBLE, file_size INT, img_ori_w INT, img_ori_h INT, PRIMARY KEY(uuid, project_id))";

    private static final String RETRIEVE_DATA = "SELECT img_path, polygons, img_x, img_y, img_w, img_h, file_size, img_ori_w, img_ori_h from Project WHERE uuid = ? AND project_id = ?";

    private static final String UPDATE_DATA = "UPDATE Project SET polygons = ?, img_depth = ?, img_x = ?, img_y = ?, img_w = ?, img_h = ?, file_size = ?, img_ori_w = ?, img_ori_h = ? WHERE uuid = ? AND project_id = ?";

    public static String getQueue(){ return QUEUE; }

    public static String createProject() { return CREATE_PROJECT; }

    public static String retrieveData() { return RETRIEVE_DATA; }

    public static String updateData() { return UPDATE_DATA; }
}