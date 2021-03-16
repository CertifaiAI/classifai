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
import ai.classifai.action.parser.AnnotationParser;
import ai.classifai.util.ParamConfig;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

/**
 * Unit for annotation versionings
 */
@NoArgsConstructor
@Getter
@Setter
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
     * @param strAnnotationVersion {annotation:[],img_x:0,img_y:0,img_w:0,img_h:0}}]
     */
    public AnnotationVersion(@NonNull String strAnnotationVersion)
    {
        String trimmedString = ActionOps.removeOuterBrackets(strAnnotationVersion);

        Integer annotationStart = ParamConfig.getAnnotationParam().length() + 1;
        Integer annotationEnd = trimmedString.indexOf(ParamConfig.getImgXParam()) - 1;

        //annotation
        String strAnnotation = trimmedString.substring(annotationStart,  annotationEnd);

        annotation = AnnotationParser.buildAnnotation(strAnnotation);

        Integer imgXStart = annotationEnd + ParamConfig.getImgXParam().length() + 2;
        Integer imgXEnd = trimmedString.indexOf(ParamConfig.getImgYParam()) - 1;

        //imgX
        imgX = getValueFromString(trimmedString, imgXStart, imgXEnd);

        Integer imgYStart = imgXEnd + ParamConfig.getImgYParam().length() + 2;
        Integer imgYEnd = trimmedString.indexOf(ParamConfig.getImgWParam()) - 1;

        //imgY
        imgY = getValueFromString(trimmedString, imgYStart, imgYEnd);

        Integer imgWStart = imgYEnd + ParamConfig.getImgWParam().length() + 2;
        Integer imgWEnd = trimmedString.indexOf(ParamConfig.getImgHParam()) - 1;

        //imgW
        imgW = getValueFromString(trimmedString, imgWStart, imgWEnd);

        Integer imgHStart = imgWEnd + ParamConfig.getImgHParam().length() + 2;

        //imgH
        imgH = getValueFromString(trimmedString, imgHStart, trimmedString.length());

    }

    private Integer getValueFromString(@NonNull String input, @NonNull Integer start, @NonNull Integer end)
    {
        return Integer.parseInt(input.substring(start, end));
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