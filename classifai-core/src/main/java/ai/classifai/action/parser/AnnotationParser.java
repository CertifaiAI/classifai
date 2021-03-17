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
package ai.classifai.action.parser;

import ai.classifai.action.ActionOps;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AnnotationParser
{
    private static final String COLOR_PARAM = "color";

    private static final String DISTANCE_TO_IMG_PARAM = "distancetoImg";

    private static final String X_PARAM = "x";
    private static final String Y_PARAM = "y";

    private static final String X1_PARAM = "x1";
    private static final String Y1_PARAM = "y1";

    private static final String X2_PARAM = "x2";
    private static final String Y2_PARAM = "y2";

    private static final String LABEL_PARAM = "label";
    private static final String ID_PARAM = "id";

    private static final String LINE_WIDTH_PARAM = "lineWidth";

    public static JsonArray buildAnnotation(@NonNull String annotation)
    {
        if(annotation.length() < 3) return new JsonArray();

        JsonArray allAnnotation = new JsonArray();

        String[] annotationList = ActionOps.splitStringByJsonSplitter(annotation);

        for(String strAnnotation : annotationList)
        {
            JsonObject thisAnnotation = new JsonObject();
            while(strAnnotation.length() > 0)
            {
                int keyEndIndex = strAnnotation.indexOf(":");

                String key = strAnnotation.substring(0, keyEndIndex);

                int valueEndIndex = strAnnotation.indexOf(getAnnotationSplitter(key));

                keyEndIndex += 1;//shift substring to one index after colon

                if(key.equals(COLOR_PARAM))
                {
                    valueEndIndex += 1;

                    String value = strAnnotation.substring(keyEndIndex, valueEndIndex);

                    thisAnnotation.put(key, value);
                }
                else if(key.equals(DISTANCE_TO_IMG_PARAM))
                {
                    valueEndIndex += 1;

                    String value = strAnnotation.substring(keyEndIndex, valueEndIndex);

                    JsonObject valueBuffer = ActionOps.getKeyWithItem(value);

                    JsonObject valueJsonObject = new JsonObject()
                            .put(X_PARAM, Double.parseDouble((String) valueBuffer.getValue(X_PARAM)))
                            .put(Y_PARAM, Double.parseDouble((String) valueBuffer.getValue(Y_PARAM)));

                    thisAnnotation.put(key, valueJsonObject);
                }
                else if(key.equals(LABEL_PARAM))
                {
                    String value = strAnnotation.substring(keyEndIndex, valueEndIndex);
                    thisAnnotation.put(key, value);

                }
                else if(key.equals(ID_PARAM))
                {
                    String value = strAnnotation.substring(keyEndIndex);

                    thisAnnotation.put(key, Long.parseLong(value));

                }
                else
                {
                    String value = strAnnotation.substring(keyEndIndex, valueEndIndex);

                    if(key.equals(LINE_WIDTH_PARAM))
                    {
                        thisAnnotation.put(key, Integer.parseInt(value));
                    }
                    else
                    {
                        thisAnnotation.put(key, Double.parseDouble(value));

                    }
                }

                if(valueEndIndex == -1)
                {
                    break;
                }
                else
                {
                    strAnnotation = strAnnotation.substring(valueEndIndex + 1);
                }
            }
            allAnnotation.add(thisAnnotation);
        }
        return allAnnotation;
    }


    /**
     * Customize function to split annotation content
     * @param key
     * @return
     */
    private static String getAnnotationSplitter(@NonNull String key)
    {
        if(key.equals(X1_PARAM) || key.equals(X2_PARAM) || key.equals(Y1_PARAM) || key.equals(Y2_PARAM)
            || key.equals(LINE_WIDTH_PARAM) || key.equals(LABEL_PARAM))
        {
            return ",";
        }
        else if(key.equals(ID_PARAM))
        {
            return "END OF LINE";
        }
        else if(key.equals(COLOR_PARAM))
        {
            return "),";
        }
        else if(key.equals(DISTANCE_TO_IMG_PARAM))
        {
            return "},";
        }

        return "";
    }
}