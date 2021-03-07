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

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * Project Version
 *
 * which stores uuid list and label list per version
 */
@Getter
@Setter
@Slf4j
public class ProjectVersion
{

    //project version uuid <> version (uuid, datetime)
    private Map<String, Version> versionUuidDict = new HashMap<>();

    //project version uuid <> uuid list
    private Map<String, List<String>> uuidDict = new HashMap<>();

    //project version uuid <> label list
    private Map<String, List<String>> labelDict = new HashMap<>();

    private Version currentVersion = null;

    public ProjectVersion(boolean createCurrentVersion)
    {
        if(createCurrentVersion)
        {
            currentVersion = new Version();

            setVersion(currentVersion);
        }
    }

    public ProjectVersion()
    {
        this(true);
    }

    public void setVersion(@NonNull Version version)
    {
        versionUuidDict.put(version.getVersionUuid(), version);

        uuidDict.put(version.getVersionUuid(), new ArrayList<>());
        labelDict.put(version.getVersionUuid(), new ArrayList<>());
    }

    /**
     * Set currrent version based on uuid
     *
     * @param versionUuid
     */
    public void setCurrentVersion(@NonNull String versionUuid)
    {
        if(versionUuidDict.containsKey(versionUuid))
        {
            currentVersion = versionUuidDict.get(versionUuid);
        }
        else
        {
            log.debug("Version to set does not exist in the dictionary of Project Version");
        }
    }

    public void setCurrentVersionUuidList(@NonNull List<String> uuidList)
    {
        uuidDict.put(currentVersion.getVersionUuid(), uuidList);
    }

    public String getUuidVersionDbFormat()
    {
        //[{version_uuid : [data_uuid, data_uuid, data_uuid]},{version_uuid : [data_uuid, data_uuid, data_uuid]}]
        return getDictDbFormat(uuidDict);

    }

    public List<String> getCurrentUuidList()
    {
        return uuidDict.get(currentVersion.getVersionUuid());
    }


    public List<String> getCurrentLabelList()
    {
        return labelDict.get(currentVersion.getVersionUuid());
    }


    public String getLabelVersionDbFormat()
    {
        return getDictDbFormat(labelDict);
    }

    private String getDictDbFormat(Map<String, List<String>> dict)
    {
        JsonArray arr = new JsonArray();

        dict.forEach((key, value) ->
        {
            JsonObject item = new JsonObject().put(key, value.toString());
            arr.add(ActionOps.encode(item));
        });

        return ActionOps.encode(arr);
    }

    public String getDbFormat()
    {
        JsonArray versionList = new JsonArray();

        for(Version version : versionUuidDict.values())
        {
            versionList.add(version.getJsonObject());
        }

        return ActionOps.removeDoubleQuote(versionList.encode());
    }

    /*


    public void updateUuidList(@NonNull Version version, @NonNull List<String> uuidList)
    {
        uuidDict.put(version.getVersionUuid(), uuidList);
    }


    public ProjectVersion(@NonNull List<Version> versionList)
    {
        for (Version version : versionList)
        {
            setVersion(version);
        }
    }

    private void setVersionCollection(@NonNull String rawVersionUuid, @NonNull String rawDateTime)
    {
        String versionUuid = rawVersionUuid.substring(ParamConfig.getVersionUuidParam().length() + 1);
        DateTime dateTime = new DateTime(rawDateTime.substring(ParamConfig.getCreatedDateParam().length() + 1));

        Version version = new Version(versionUuid, dateTime);

        versionUuidDict.put(versionUuid, version);

        uuidDict.put(versionUuid, new ArrayList<>());
        labelDict.put(versionUuid, new ArrayList<>());
    }

    public void updateLabelList(@NonNull Version version, @NonNull List<String> labelList)
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
     */

}
