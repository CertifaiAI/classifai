/*
 * Copyright (c) 2021 CertifAI Sdn. Bhd.
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
package ai.classifai.util.versioning;

import ai.classifai.util.ParamConfig;
import ai.classifai.util.collection.UUIDGenerator;
import ai.classifai.util.datetime.DateTime;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class ProjectVersion
{
    Integer versionIndex;

    //key identifier
    String versionUuid;

    DateTime dateTime;

    public ProjectVersion(@NonNull Integer currentIndex, @NonNull String currentVersionUuid, @NonNull DateTime currentDateTime)
    {
        versionIndex = currentIndex;
        versionUuid = currentVersionUuid;
        dateTime = currentDateTime;
    }

    public ProjectVersion(Integer index)
    {
        this(index, UUIDGenerator.generateUUID(), new DateTime());
    }

    public ProjectVersion()
    {
        this(1);
    }

    public JsonObject getJsonObject()
    {
        return new JsonObject().put(ParamConfig.getVersionIndexParam(), versionIndex)
                        .put(ParamConfig.getVersionUuidParam(), versionUuid)
                        .put(ParamConfig.getCreatedDateParam(), dateTime.toString());
    }
}
