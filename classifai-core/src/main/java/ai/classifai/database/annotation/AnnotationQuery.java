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
    protected final static String CREATE_DATA = "INSERT INTO Project VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    protected final static String RETRIEVE_DATA_PATH = "SELECT img_path FROM Project WHERE uuid = ? AND project_id = ?";

    protected final static String LOAD_VALID_PROJECT_UUID = "SELECT img_path FROM Project WHERE project_id = ? AND uuid = ?";

    protected final static String DELETE_PROJECT_UUID_LIST_WITH_PROJECTID = "DELETE FROM Project WHERE project_id = ?";

    protected final static String DELETE_PROJECT_UUID_LIST = "DELETE FROM Project WHERE project_id = ? AND uuid = ?";

    public static String createData() { return CREATE_DATA; }

    public static String retrieveDataPath() { return RETRIEVE_DATA_PATH; }

    public static String loadValidProjectUUID() { return LOAD_VALID_PROJECT_UUID; }

    public static String deleteProjectUUIDListwithProjectID() { return DELETE_PROJECT_UUID_LIST_WITH_PROJECTID; }

    public static String deleteProjectUUIDList(){ return DELETE_PROJECT_UUID_LIST; }
}