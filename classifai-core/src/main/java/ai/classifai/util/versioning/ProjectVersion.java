package ai.classifai.util.versioning;

import ai.classifai.util.ParamConfig;
import ai.classifai.util.collection.UUIDGenerator;
import ai.classifai.util.datetime.DateTime;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class ProjectVersion
{
    Integer versionIndex;

    //key identifier
    String versionUuid;

    DateTime dateTime;

    public ProjectVersion(@NonNull Integer currentIndex, @NonNull String currentVersionUuid, @NonNull DateTime currentDateTime)
    {
        versionIndex = currentIndex;
        versionUuid = currentVersionUuid;
        dateTime = currentDateTime;

        System.out.println("Version index: " + versionIndex);

        System.out.println("Version uuid: " + versionUuid);

        System.out.println("Date time: " + dateTime.toString());

    }

    public ProjectVersion(Integer index)
    {
        this(index, UUIDGenerator.generateUUID(), new DateTime());
    }

    public ProjectVersion()
    {
        this(1);
    }

    public JsonObject getJsonObject()
    {
        return new JsonObject().put(ParamConfig.getVersionIndexParam(), versionIndex)
                        .put(ParamConfig.getVersionUuidParam(), versionUuid)
                        .put(ParamConfig.getCreatedDateParam(), dateTime.toString());
    }

    public JsonArray getJsonArray()
    {
        return new JsonArray().add(versionIndex)
                              .add(versionUuid)
                              .add(dateTime.toString());
    }






}
