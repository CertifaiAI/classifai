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
package ai.classifai.database.versioning;

import ai.classifai.action.ActionOps;
import ai.classifai.util.ParamConfig;
import io.vertx.core.json.JsonObject;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Unit for annotation versionings
 */
@NoArgsConstructor
@Getter
@Setter
public class AnnotationVersion
{
    String annotation = ParamConfig.getEmptyArray();
    Integer imgX = 0;
    Integer imgY = 0;
    Integer imgW = 0;
    Integer imgH = 0;

    public AnnotationVersion(@NonNull JsonObject jsonObject)
    {
        annotation = jsonObject.getString(ParamConfig.getAnnotationParam());

        imgX = Integer.parseInt(jsonObject.getString(ParamConfig.getImgXParam()));
        imgY = Integer.parseInt(jsonObject.getString(ParamConfig.getImgYParam()));

        imgW = Integer.parseInt(jsonObject.getString(ParamConfig.getImgWParam()));
        imgH = Integer.parseInt(jsonObject.getString(ParamConfig.getImgHParam()));
    }

    private JsonObject getJsonObject()
    {
        JsonObject unitJsonObject = new JsonObject();
        unitJsonObject.put(ParamConfig.getAnnotationParam(), annotation);
        unitJsonObject.put(ParamConfig.getImgXParam(), imgX);
        unitJsonObject.put(ParamConfig.getImgYParam(), imgY);
        unitJsonObject.put(ParamConfig.getImgWParam(), imgW);
        unitJsonObject.put(ParamConfig.getImgHParam(), imgH);

        return unitJsonObject;
    }

    public String getDbFormat()
    {
        return ActionOps.removeDoubleQuote(getJsonObject().encode());
    }
}