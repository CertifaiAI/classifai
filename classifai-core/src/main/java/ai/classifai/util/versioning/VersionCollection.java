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
import ai.classifai.util.collection.ConversionHandler;
import ai.classifai.util.data.StringHandler;
import ai.classifai.util.datetime.DateTime;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.NonNull;

import java.util.*;

@Getter
public class VersionCollection {

    //index
    private Map<Integer, ProjectVersion> versionIndexDict = new HashMap<>();

    //uuid
    private Map<String, ProjectVersion> versionUuidDict = new HashMap<>();

    private Map<String, List<String>> labelDict = new HashMap<>();
    private Map<String, List<String>> uuidDict = new HashMap<>();


    public VersionCollection(@NonNull String strVersionList)
    {
        strVersionList = StringHandler.cleanUpRegex(strVersionList);

        boolean isMultiple = strVersionList.contains("},{") ? true : false;

        List<String> versionList = ConversionHandler.string2StringList(strVersionList);

        if(!isMultiple)
        {
            setVersionCollection(versionList.get(0), versionList.get(1), versionList.get(2));
        }
        else
        {
            for (String item : versionList)
            {
                String[] currentVersion = item.split(",");

                setVersionCollection(currentVersion[0], currentVersion[1], currentVersion[2]);
            }
        }
    }

    private void setVersionCollection(@NonNull String rawVersionIndex, @NonNull String rawVersionUuid, @NonNull String rawDateTime)
    {
        Integer versionIndex = Integer.parseInt(rawVersionIndex.substring(ParamConfig.getVersionIndexParam().length() + 1));
        String versionUuid = rawVersionUuid.substring(ParamConfig.getVersionUuidParam().length() + 1);
        DateTime dateTime = new DateTime(rawDateTime.substring(ParamConfig.getCreatedDateParam().length() + 1));

        ProjectVersion version = new ProjectVersion(versionIndex, versionUuid, dateTime);

        versionIndexDict.put(versionIndex, version);
        versionUuidDict.put(versionUuid, version);

        uuidDict.put(versionUuid, new ArrayList<>());
        labelDict.put(versionUuid, new ArrayList<>());
    }

    public VersionCollection(@NonNull List<ProjectVersion> versionList)
    {
        for (ProjectVersion version : versionList)
        {
            versionIndexDict.put(version.getVersionIndex(), version);
            versionUuidDict.put(version.getVersionUuid(), version);

            uuidDict.put(version.getVersionUuid(), new ArrayList<>());
            labelDict.put(version.getVersionUuid(), new ArrayList<>());
        }
    }

    public VersionCollection(@NonNull ProjectVersion projVersion)
    {
        this(Arrays.asList(projVersion));
    }

    public void setLabelDict(@NonNull String dbString)
    {
        getElements(dbString, labelDict);
    }

    public void setUuidDict(@NonNull String dbString)
    {
        getElements(dbString, uuidDict);
    }

    public void getElements(@NonNull String dbString, Map<String, List<String>> arrayDict)
    {
        if(dbString.length() < 5) return;

        String rawDbString = StringHandler.cleanUpRegex(dbString);

        boolean isMultiple = rawDbString.contains("},{") ? true : false;

        if(!isMultiple)
        {
            rawDbString = rawDbString.substring(2);
            rawDbString= rawDbString.substring(0, rawDbString.length() - 3);

            Integer versionPartition = rawDbString.indexOf(":");

            String version = rawDbString.substring(0, versionPartition);

            String strContentList = rawDbString.substring(versionPartition + 2);

            String delimiter = strContentList.contains(", ") ? ", " : ",";

            String[] contentArray = strContentList.split(delimiter);

            arrayDict.put(version, Arrays.asList(contentArray));

        }
        else
        {
            List<String> versionList = ConversionHandler.string2StringList(rawDbString);

            for(String buffer : versionList)
            {
                Integer versionPartition = buffer.indexOf(":");

                String version = buffer.substring(0, versionPartition);

                String strContentList = buffer.substring(versionPartition + 1);

                List<String> contentList = ConversionHandler.string2StringList(strContentList);

                arrayDict.put(version, contentList);
            }
        }
    }

    public void updateUuidList(@NonNull ProjectVersion version, @NonNull List<String> uuidList)
    {
        uuidDict.put(version.getVersionUuid(), uuidList);
    }

    public void updateLabelList(@NonNull ProjectVersion version, @NonNull List<String> labelList)
    {
        labelDict.put(version.getVersionUuid(), labelList);
    }


    public String getUuidDictObject2Db()
    {
        JsonArray jsonArray = new JsonArray();

        uuidDict.forEach((key, value) -> jsonArray.add(new JsonObject().put(key, value.toString())));

        return jsonArray.toString();
    }

    public String getLabelDictObject2Db()
    {
        JsonArray jsonArray = new JsonArray();

        labelDict.forEach((key, value) -> jsonArray.add(new JsonObject().put(key, value.toString())));

        return jsonArray.toString();
    }

    public String toString()
    {
        JsonArray jsonArray = new JsonArray();

        for(ProjectVersion version: versionIndexDict.values())
        {
            jsonArray.add(version.getJsonObject());
        }

        return StringHandler.cleanUpRegex(jsonArray.toString());
    }
}
