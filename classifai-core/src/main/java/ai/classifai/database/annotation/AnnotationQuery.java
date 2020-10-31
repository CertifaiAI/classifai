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
package ai.classifai.database.annotation;

/**
 * Common SQL Query for Each Annotation Type
 *
 * @author codenamewei
 */
public abstract class AnnotationQuery
{
    protected static String CREATE_PROJECT;

    protected static String RETRIEVE_DATA;

    protected static String UPDATE_DATA;

    protected final static String CREATE_DATA = "insert into Project values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    protected final static String RETRIEVE_DATA_PATH = "select img_path from Project where uuid = ? and project_id = ?";

    protected final static String DELETE_DATA = "delete from Project where uuid = ? and project_id = ?";

    protected final static String LOAD_VALID_PROJECT_UUID = "load valid project uuid";

    public static String createData()
    {
        return CREATE_DATA;
    }

    public static String retrieveDataPath() { return RETRIEVE_DATA_PATH; }

    public static String loadValidProjectUUID() { return LOAD_VALID_PROJECT_UUID; }

    public static String deleteData() { return DELETE_DATA; }

}
