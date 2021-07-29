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
package ai.classifai.util.collection;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Handler for conversion of different formats
 *
 * @author codenamewei
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConversionHandler
{
    public static <T> List<T> set2List(Set<T> setList)
    {
        return new ArrayList<>(setList);
    }

    public static String arrayString2String(String[] input)
    {
        StringBuilder sb = new StringBuilder();

        Arrays.stream(input)
                .forEach(sb::append);

        return sb.toString();
    }

    public static boolean string2Boolean(String input) {
        boolean value = false;

        if (input.equals("true") || input.equals("false"))
        {
            value = Boolean.parseBoolean(input);
        }
        else
        {
            throw new IllegalArgumentException("String to boolean failed as input: " + input + ". Only allowed string input of true / false.");
        }

        return value;
    }



    //JSONObject -> .json
    public static void saveJson2File(JSONObject jsonInput, File outputFilePath)
    {

        try (FileWriter fileWriter = new FileWriter(outputFilePath))
        {
            fileWriter.write(jsonInput.toJSONString());
        }
        catch (Exception e)
        {
            log.debug("FileWriter Exception while writing org.json.simple.JSONObject to File: ", e);
        }

        log.info("org.json.simple.JSONObject saved to " + outputFilePath.getAbsolutePath());
    }


    public static JsonObject json2JSONObject(Object input)
    {
        try
        {
            JSONParser parser = new JSONParser();

            JSONObject json = (JSONObject) parser.parse(input.toString());

            JsonObject vertxJson = new JsonObject();

            for (Object o : json.keySet()) {
                String key = (String) o;
                vertxJson.put(key, json.get(key));
            }

            return vertxJson;

        }
        catch (IllegalArgumentException | ParseException e)
        {
            log.error("Error in parsing Object to io.vertx.core.json.JsonObject", e);
        }

        return null;
    }

    public static List<String> string2StringList(String input)
    {
        String content = preprocessStringToArray(input);

        if (content.isEmpty()) return new ArrayList<>();

        String delimiter = ", ";

        if(content.contains("],["))
        {
            delimiter = "],\\[";
        }
        else if(content.contains("},{"))
        {
            delimiter = "}.\\{";
        }
        else if(content.contains(","))
        {
            delimiter = ",";
        }

        return new ArrayList<>(Arrays.asList(content.split(delimiter)));
    }

    public static String preprocessStringToArray(String input)
    {
        if ( (input.equals("[{") || input.equals("[]") || input.equals("[ ]") || input.length() == 0))
        {
            return "";
        }

        input = input.replace("\"","");

        String content;

        if (input.startsWith("[[") || input.startsWith("[{") || input.startsWith("[ "))
        {
            content = input.substring(2);
            content = content.substring(0, content.length() - 2);
        }
        else if (input.charAt(0) == '[')
        {
            content = input.substring(1);
            content = content.substring(0, content.length() - 1);

        }
        else
        {
            return "";
        }

        return content;
    }

    public static List<String> integerList2StringList(List<Integer> oldList)
    {
        List<String> newList = new ArrayList<>();
        for (Integer myInt : oldList)
        {
            newList.add(String.valueOf(myInt));
        }

        return newList;
    }

    public static List<Integer> string2IntegerList(String input)
    {
            String content = preprocessStringToArray(input);

            if (content.isEmpty()) return new ArrayList<>();

            if (content.length() == 1)
            {
                return new ArrayList<>(Collections.singletonList(Integer.parseInt(content)));
            }

            String delimiter = content.contains(", ") ? ", " : ",";

            List<String> subString = Arrays.asList(content.split(delimiter));

        return subString.stream()
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    public static List<Integer> jsonArray2IntegerList(JsonArray json)
    {
        if ((json == null) || (json.size() == 0)) return new ArrayList<>();

        List<Integer> array = new ArrayList<>();

        for (int i = 0 ; i < json.size(); ++i)
        {
            array.add(json.getInteger(i));
        }

        return array;
    }

    public static List<String> jsonArray2StringList(JsonArray json)
    {
        if ((json == null) || (json.size() == 0)) return new ArrayList<>();

        List<String> array = new ArrayList<>();

        for (int i = 0 ; i < json.size(); ++i)
        {
            array.add(json.getString(i));
        }

        return array;
    }


    //.json -> JSONObject
    public static JSONObject loadFile2Json(File inputFilePath)
    {
        JSONObject jsonObject = new JSONObject();

        JSONParser parser = new JSONParser();
        try
        {
            Object obj = parser.parse(new FileReader(inputFilePath));

            if (obj != null)
            {
                jsonObject = (JSONObject) obj;
            }
            else
            {
                jsonObject = null;
                log.debug("File loading from File to org.json.simple.JSONObject failed");
            }


        }
        catch (Exception e)
        {
            log.debug("Exception when loading from File to org.json.simple.JSONObject", e);
        }

        return jsonObject;
    }


    public static JSONObject map2JSONObjectParser(Map<Integer, String> inputDict)
    {
        if ((inputDict == null) || (inputDict.isEmpty()))
        {
            log.error("Input Map is null/empty. Could not parse to JSONObject");

            return new JSONObject();
        }

        JSONObject jsonOutput = new JSONObject();

        Iterator<Map.Entry<Integer, String>> entries = inputDict.entrySet().iterator();

        while (entries.hasNext())
        {
            Map.Entry<Integer, String> entry = entries.next();
            jsonOutput.put(entry.getKey(), entry.getValue());
        }
        return jsonOutput;
    }
}