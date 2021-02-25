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

public class VersionCollection {

    //index
    private Map<Integer, ProjectVersion> versionIndexDict = new HashMap<>();

    //uuid
    @Getter private Map<String, ProjectVersion> versionUuidDict = new HashMap<>();

    private Map<String, List<String>> labelDict = new HashMap<>();
    @Getter private Map<String, List<String>> uuidDict = new HashMap<>();


    public VersionCollection(@NonNull String strVersionList)
    {
        strVersionList = StringHandler.cleanUpRegex(strVersionList, Arrays.asList("\""));

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

    public void setUuidDict(@NonNull String dbString)
    {
        String rawDbString = StringHandler.cleanUpRegex(dbString, Arrays.asList("\""));

        boolean isMultiple = rawDbString.contains("},{") ? true : false;

        if(!isMultiple)
        {
            rawDbString = rawDbString.substring(2);
            rawDbString= rawDbString.substring(0, rawDbString.length() - 3);

            Integer versionPartition = rawDbString.indexOf(":");

            String version = rawDbString.substring(0, versionPartition);

            System.out.println("Version: " + version);

            String strUuidList = rawDbString.substring(versionPartition + 2);

            String[] currentUuidList = strUuidList.split(",");

            List<String> uuidList = new ArrayList<>();

            for(String uuid : currentUuidList)
            {
                uuidList.add(uuid);
                System.out.println("Uuid: " + uuid);
            }

            uuidDict.put(version, uuidList);

        }
        else
        {
            List<String> uuidVersionList = ConversionHandler.string2StringList(rawDbString);

            for(String buffer : uuidVersionList)
            {
                Integer versionPartition = buffer.indexOf(":");

                String version = buffer.substring(0, versionPartition);

                System.out.println("Version: " + version);

                String strUuidList = buffer.substring(versionPartition + 1);

                List<String> currentUuidList = ConversionHandler.string2StringList(strUuidList);

                List<String> uuidList = new ArrayList<>();

                for(String uuid : currentUuidList)
                {
                    uuidList.add(uuid);
                    System.out.println("Uuid: " + uuid);
                }

                uuidDict.put(version, uuidList);
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

    public String toString()
    {
        JsonArray jsonArray = new JsonArray();

        for(ProjectVersion version: versionIndexDict.values())
        {
            jsonArray.add(version.getJsonObject());
        }

        return StringHandler.cleanUpRegex(jsonArray.toString(), Arrays.asList("\""));
    }
}
