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

import ai.classifai.backend.utility.ActionOps;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private Map<String, List<String>> uuidListDict = new HashMap<>();

    //project version uuid <> label list
    private Map<String, List<String>> labelListDict = new HashMap<>();

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

        uuidListDict.put(version.getVersionUuid(), new ArrayList<>());
        labelListDict.put(version.getVersionUuid(), new ArrayList<>());
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
        uuidListDict.put(currentVersion.getVersionUuid(), uuidList);
    }

    public void setCurrentVersionLabelList(@NonNull List<String> labelList)
    {
        labelListDict.put(currentVersion.getVersionUuid(), labelList);
    }

    public String getUuidVersionDbFormat()
    {
        //[{version_uuid : [data_uuid, data_uuid, data_uuid]},{version_uuid : [data_uuid, data_uuid, data_uuid]}]
        return getDictDbFormat(uuidListDict);

    }

    public List<String> getCurrentUuidList()
    {
        return uuidListDict.get(currentVersion.getVersionUuid());
    }

    public List<String> getCurrentLabelList()
    {
        return labelListDict.get(currentVersion.getVersionUuid());
    }


    public String getLabelVersionDbFormat()
    {
        return getDictDbFormat(labelListDict);
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

}
