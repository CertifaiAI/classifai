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

import ai.classifai.util.type.AnnotationType;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import lombok.NonNull;


/***
 * Common Functionalities for Each Annotation Verticle
 *
 * @author codenamewei
 */
public interface AnnotationServiceable
{
    void retrieveData(Message<JsonObject> message, @NonNull JDBCClient jdbcClient, @NonNull String query, AnnotationType annotationType);

    void retrieveDataPath(Message<JsonObject> message, @NonNull JDBCClient jdbcClient, @NonNull String query);

    void loadValidProjectUUID(Message<JsonObject> message, @NonNull JDBCClient jdbcClient, @NonNull String query);

    void updateData(Message<JsonObject> message, @NonNull JDBCClient jdbcClient, @NonNull String query, AnnotationType annotationType);

    void deleteProjectUUIDList(Message<JsonObject> message, @NonNull JDBCClient jdbcClient, @NonNull String query);

}
