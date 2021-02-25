package ai.classifai.util.versioning;

import ai.classifai.util.ParamConfig;
import ai.classifai.util.collection.ConversionHandler;
import ai.classifai.util.data.StringHandler;
import ai.classifai.util.datetime.DateTime;
import lombok.NonNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VersionCollection {
    private Map<Integer, ProjectVersion> versionIndexDict = new HashMap<>();
    private Map<String, ProjectVersion> versionUuidDict = new HashMap<>();

    public VersionCollection(@NonNull String strVersionList)
    {
        strVersionList = StringHandler.cleanUpRegex(strVersionList, Arrays.asList("\""));

        List<String> versionList = ConversionHandler.string2StringList(strVersionList);

        for (String item : versionList)
        {
            String[] currentVersion = item.split(",");

            Integer versionIndex = Integer.parseInt(currentVersion[0].substring(ParamConfig.getVersionIndexParam().length() + 1));
            String versionUuid = currentVersion[1].substring(ParamConfig.getVersionUuidParam().length() + 1);
            DateTime dateTime = new DateTime(currentVersion[2].substring(ParamConfig.getCreatedDateParam().length() + 1));

            ProjectVersion version = new ProjectVersion(versionIndex, versionUuid, dateTime);

            versionIndexDict.put(versionIndex, version);
            versionUuidDict.put(versionUuid, version);
        }
    }

    public VersionCollection(@NonNull List<ProjectVersion> versionList) {
        for (ProjectVersion version : versionList) {
            versionIndexDict.put(version.getVersionIndex(), version);
            versionUuidDict.put(version.getVersionUuid(), version);
        }
    }
}
