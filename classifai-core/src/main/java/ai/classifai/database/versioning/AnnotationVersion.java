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

import ai.classifai.util.ParamConfig;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Unit for annotation versionings
 */
@NoArgsConstructor
@Getter
@Setter
@Slf4j
public class AnnotationVersion
{
    JsonArray annotation = new JsonArray();

    Integer imgX = 0;
    Integer imgY = 0;
    Integer imgW = 0;
    Integer imgH = 0;

    /**
     * Constructor
     *
     * @param jsonAnnotationVersion {annotation:[],img_x:0,img_y:0,img_w:0,img_h:0}}]
     */
    public AnnotationVersion(@NonNull JsonObject jsonAnnotationVersion)
    {
        annotation = jsonAnnotationVersion.getJsonArray(ParamConfig.getAnnotationParam());
        imgX = jsonAnnotationVersion.getInteger(ParamConfig.getImgXParam());
        imgY = jsonAnnotationVersion.getInteger(ParamConfig.getImgYParam());
        imgW = jsonAnnotationVersion.getInteger(ParamConfig.getImgWParam());
        imgH = jsonAnnotationVersion.getInteger(ParamConfig.getImgHParam());
    }

    public JsonObject getJsonObject()
    {
        JsonObject unitJsonObject = new JsonObject();
        unitJsonObject.put(ParamConfig.getAnnotationParam(), annotation);
        unitJsonObject.put(ParamConfig.getImgXParam(), imgX);
        unitJsonObject.put(ParamConfig.getImgYParam(), imgY);
        unitJsonObject.put(ParamConfig.getImgWParam(), imgW);
        unitJsonObject.put(ParamConfig.getImgHParam(), imgH);

        return unitJsonObject;
    }
}