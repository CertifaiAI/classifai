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
package ai.classifai.action;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Operations used in parsing object amont string <> json array <> json object
 *
 * @author codenamewei
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ActionOps
{
    private static final String REPLACEMENT = "";
    private static final String JSON_OBJECT_SPLITTER = "},\\{";
    private static final String COLON_SEPARATOR = ":";

    private static final String COMMA_SEPARATOR = ",";

    private static final String QUOTATION = "\"";

    /**
     * Remove quotation mark "
     *
     * @param input
     * @return
     */
    public static String removeDoubleQuote(@NonNull String input)
    {
        return input.replace(QUOTATION, REPLACEMENT);
    }

    private static String removeBracket(@NonNull String input)
    {
        return removePair(input, '{', '}');
    }

    private static String removeSquareBracket(@NonNull String input)
    {
        return removePair(input, '[', ']');
    }

    public static String removeOuterBrackets(@NonNull String input)
    {
        return removeBracket(removeSquareBracket(input));
    }

    public static Map<String, List<String>> getKeyWithArray(@NonNull String input)
    {
        String[] jsonObjectArray = removeOuterBrackets(input).split(JSON_OBJECT_SPLITTER);

        Map<String, List<String>> lookUpTable = new HashMap<>();

        for(String eachJsonObject : jsonObjectArray)
        {
            String[] keyValuePair = eachJsonObject.split(COLON_SEPARATOR);

            String[] uuidList = ActionOps.removeSquareBracket(keyValuePair[1]).split(", ");

            lookUpTable.put(keyValuePair[0], Arrays.asList(uuidList));
        }

        return lookUpTable;
    }

    public static String[] getArrayOfJsonObject(@NonNull String input)
    {
        return removeOuterBrackets(input).split(JSON_OBJECT_SPLITTER);
    }

    public static String encode(@NonNull Object object)
    {
        if(object instanceof JsonArray)
        {
            return removeDoubleQuote(((JsonArray) object).encode());
        }
        else if(object instanceof JsonObject)
        {
            return removeDoubleQuote(((JsonObject) object).encode());
        }
        else if(object instanceof List)
        {
            return object.toString();
        }

        return null;
    }


    public static JsonObject getKeyWithItem(@NonNull String input)
    {
        String[] jsonObjectArray = removeOuterBrackets(input).split(COMMA_SEPARATOR);

        JsonObject jsonObject = new JsonObject();

        for(String eachJsonObject : jsonObjectArray)
        {
            if(!eachJsonObject.contains(COLON_SEPARATOR))
            {
                log.info("jsonObject separator ill-defined. No colon separator");
                break;
            }

            int keyValueSeparator = eachJsonObject.indexOf(COLON_SEPARATOR);

            String key = eachJsonObject.substring(0, keyValueSeparator);
            String value = eachJsonObject.substring(keyValueSeparator + 1);

            jsonObject.put(key, value);
        }

        return jsonObject;
    }

    private static String removePair(@NonNull String input, char openBracket, char closeBracket)
    {
        if(input.length() > 2 && (input.charAt(0) != openBracket)  || (input.charAt(input.length() - 1) != closeBracket)) return input;

        return input.substring(1, input.length() - 1);
    }
}