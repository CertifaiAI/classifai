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

import ai.classifai.action.ActionOps;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.collection.UUIDGenerator;
import ai.classifai.util.datetime.DateTime;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.NonNull;

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

    private DateTime dateTime;

    private String nextVersionUuid = null; //linked-list, to get sequential in creation

    public Version(@NonNull String currentVersionUuid, @NonNull DateTime currentDateTime)
    {
        versionUuid = currentVersionUuid;
        dateTime = currentDateTime;
    }
    public Version()
    {
        this(UUIDGenerator.generateUUID(), new DateTime());
    }

    public Version(@NonNull String strVersion)
    {
        JsonObject jsonObject = ActionOps.getKeyWithItem(strVersion);

        versionUuid = jsonObject.getString(ParamConfig.getVersionUuidParam());
        dateTime = new DateTime(jsonObject.getString(ParamConfig.getCreatedDateParam()));

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
                .put(ParamConfig.getCreatedDateParam(), dateTime.toString())
                .put(ParamConfig.getNextVersionUuidParam(), nextVersionUuid != null ? nextVersionUuid : "null");

        return jsonObject;
    }
}
