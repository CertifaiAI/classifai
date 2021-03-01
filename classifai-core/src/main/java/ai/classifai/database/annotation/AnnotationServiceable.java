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
package ai.classifai.database.annotation;

import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import lombok.NonNull;


/***
 * Common Functionalities for Each Annotation Verticle
 *
 * @author codenamewei
 */
public interface AnnotationServiceable
{
    void queryData(Message<JsonObject> message, @NonNull String annotationKey);

    void updateData(Message<JsonObject> message, @NonNull String annotationKey);

    void retrieveDataPath(Message<JsonObject> message);

    void loadValidProjectUuid(Message<JsonObject> message);

    void deleteProject(Message<JsonObject> message);

    void deleteSelectionUuidList(Message<JsonObject> message);
}
