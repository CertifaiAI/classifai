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
package ai.classifai.backend.versioning;

import ai.classifai.backend.utility.ParamConfig;
import ai.classifai.backend.utility.UuidGenerator;
import ai.classifai.backend.utility.action.ActionOps;
import ai.classifai.backend.utility.datetime.DateTime;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * Version single unit
 * contains
 * - version uuid
 * - date time
 * - Version (to next)
 *
 * @author codenamewei
 */
@Getter
public class Version
{
    //key identifier
    private String versionUuid;

    private DateTime createdDate;

    @Setter
    private DateTime lastModifiedDate;

    private String nextVersionUuid = null; //linked-list, to get sequential in creation

    public Version(@NonNull String currentVersionUuid, @NonNull DateTime currentDateTime)
    {
        this(currentVersionUuid, currentDateTime, currentDateTime);
    }

    public Version(@NonNull String currentVersionUuid, @NonNull DateTime currentDateTime, DateTime lastModifiedDate)
    {
        versionUuid = currentVersionUuid;
        createdDate = currentDateTime;

        if (lastModifiedDate == null)
        {
            lastModifiedDate = new DateTime(currentDateTime.toString());
        }

        this.lastModifiedDate = lastModifiedDate;
    }

    public Version()
    {
        this(UuidGenerator.generateUuid(), new DateTime());
    }

    public Version(@NonNull String strVersion)
    {
        JsonObject jsonObject = ActionOps.getKeyWithItem(strVersion);

        versionUuid = jsonObject.getString(ParamConfig.getVersionUuidParam());
        createdDate = new DateTime();
        lastModifiedDate = new DateTime(createdDate.toString());

        String nextVersionBuffer = jsonObject.getString(ParamConfig.getNextVersionUuidParam());

        if(!nextVersionBuffer.equals("null"))
        {
            nextVersionUuid = nextVersionBuffer;
        }
    }

    public String getDbFormat()
    {
        return ActionOps.removeDoubleQuote(getJsonObject().encode());
    }

    public JsonObject getJsonObject()
    {
        JsonObject jsonObject = new JsonObject()
                .put(ParamConfig.getVersionUuidParam(), versionUuid)
                .put(ParamConfig.getCreatedDateParam(), createdDate.toString())
                .put(ParamConfig.getLastModifiedDate(), lastModifiedDate.toString())
                .put(ParamConfig.getNextVersionUuidParam(), nextVersionUuid != null ? nextVersionUuid : "null");

        return jsonObject;
    }
}
